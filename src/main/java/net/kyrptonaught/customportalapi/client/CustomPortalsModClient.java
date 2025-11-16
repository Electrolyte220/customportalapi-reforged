package net.kyrptonaught.customportalapi.client;

import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.api.CustomPortalBuilder;
import net.kyrptonaught.customportalapi.compat.kjs.CustomPortalAPIKubeJSPlugin;
import net.kyrptonaught.customportalapi.init.ParticleInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = CustomPortalsMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CustomPortalsModClient {

	@SubscribeEvent
	public static void onBlockColors(RegisterColorHandlersEvent.Block event) {
		for(CustomPortalBuilder portal : CustomPortalAPIKubeJSPlugin.PORTALS) {
			event.register((state, world, pos, tintIndex) -> {
				if(portal.portalLink != null) return portal.portalLink.colorID;
				else return 1908001;
			}, ForgeRegistries.BLOCKS.getValue(portal.portalBlockId));
		}
	}

	@SubscribeEvent
	public static void onParticleFactoryRegistry(final RegisterParticleProvidersEvent event) {
		Minecraft.getInstance().particleEngine.register(ParticleInit.CUSTOMPORTALPARTICLE.get(), CustomPortalParticle.Factory::new);
	}
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> CustomPortalAPIKubeJSPlugin.PORTALS.forEach(portal -> ItemBlockRenderTypes.setRenderLayer(BuiltInRegistries.BLOCK.get(portal.portalBlockId), RenderType.translucent())));
    }
}