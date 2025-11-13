package net.kyrptonaught.customportalapi;

import com.mojang.logging.LogUtils;
import com.simibubi.create.api.contraption.train.PortalTrackProvider;
import com.simibubi.create.content.trains.track.AllPortalTracks;
import net.kyrptonaught.customportalapi.compat.kjs.CustomPortalAPIKubeJSPlugin;
import net.kyrptonaught.customportalapi.init.ParticleInit;
import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.portal.PortalPlacer;
import net.kyrptonaught.customportalapi.portal.frame.FlatPortalAreaHelper;
import net.kyrptonaught.customportalapi.portal.frame.VanillaPortalAreaHelper;
import net.kyrptonaught.customportalapi.portal.linking.PortalLinkingStorage;
import net.kyrptonaught.customportalapi.util.CustomTeleporter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.HashMap;

import static net.kyrptonaught.customportalapi.CustomPortalsMod.MOD_ID;

@Mod(MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CustomPortalsMod {
	public static final String MOD_ID = "cpapireforged";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
	
    public static final RegistryObject<CustomPortalBlock> portalBlock = BLOCKS.register("custom_portal_block", () -> new CustomPortalBlock(Block.Properties.copy(Blocks.NETHER_PORTAL).noCollission().strength(-1).sound(SoundType.GLASS).lightLevel(state -> 11)));
	public static HashMap<ResourceLocation, ResourceKey<Level>> dims = new HashMap<>();
	public static ResourceLocation VANILLAPORTAL_FRAMETESTER = new ResourceLocation(MOD_ID, "vanillanether");
	public static ResourceLocation FLATPORTAL_FRAMETESTER = new ResourceLocation(MOD_ID, "flat");
	public static PortalLinkingStorage portalLinkingStorage;

	public CustomPortalsMod() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

		BLOCKS.register(bus);

		ParticleInit.PARTICLES.register(bus);
		onInitialize(bus);
	}

	private void onServerStart(ServerStartedEvent event) {
		for (ResourceKey<Level> registryKey : event.getServer().levelKeys())
			dims.put(registryKey.location(), registryKey);
		portalLinkingStorage = (PortalLinkingStorage) event.getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(PortalLinkingStorage::fromNbt, PortalLinkingStorage::new, MOD_ID);
	}

	private void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
		var player = event.getEntity();
		var world = event.getLevel();
		var hand = event.getHand();
		var stack = player.getItemInHand(hand);

		if (!world.isClientSide()) {
			var item = stack.getItem();
			if (PortalIgnitionSource.isRegisteredIgnitionSourceWith(item)) {
				var hit = player.pick(6, 1, false);
				if (hit.getType() == HitResult.Type.BLOCK) {
					var blockHit = (BlockHitResult) hit;
					if (PortalPlacer.attemptPortalLight(world, blockHit.getBlockPos().relative(blockHit.getDirection()), PortalIgnitionSource.ItemUseSource(item)))
						event.setResult(Event.Result.ALLOW);
				}
			}
		}
	}

	public void onInitialize(IEventBus bus) {
		MinecraftForge.EVENT_BUS.addListener(this::onServerStart);
		CustomPortalApiRegistry.registerPortalFrameTester(VANILLAPORTAL_FRAMETESTER, VanillaPortalAreaHelper::new);
		CustomPortalApiRegistry.registerPortalFrameTester(FLATPORTAL_FRAMETESTER, FlatPortalAreaHelper::new);
		MinecraftForge.EVENT_BUS.addListener(this::onRightClickItem);
	}

	public static void logError(String message) {
		LOGGER.error(message);
	}

	public static CustomPortalBlock getDefaultPortalBlock() {
		return portalBlock.get();
	}

	@SubscribeEvent
	public static void onCommonStartUp(FMLCommonSetupEvent event) {
		CustomPortalAPIKubeJSPlugin.loadKubePortals();
		event.enqueueWork(() -> CustomPortalApiRegistry.getAllPortalLinks().forEach(portalLink -> {
            PortalTrackProvider provider = (l, f) ->
			PortalTrackProvider.fromProbe(l, f, ResourceKey.create(Registries.DIMENSION, portalLink.returnDimID), ResourceKey.create(Registries.DIMENSION, portalLink.dimID),
                        (ol, e) -> CustomTeleporter.customTPTarget(ol, e, e.blockPosition(), portalLink.getPortalBlock().getPortalBase(l, e.blockPosition()), portalLink.getFrameTester()));
            AllPortalTracks.tryRegisterIntegration(BuiltInRegistries.BLOCK.getKey(portalLink.getPortalBlock()), provider);
        }));
//		CustomPortalBuilder.beginPortal().frameBlock(Blocks.GLOWSTONE).destDimID(new ResourceLocation("the_nether")).lightWithWater().tintColor(46, 5, 25).registerPortal();
	}
}