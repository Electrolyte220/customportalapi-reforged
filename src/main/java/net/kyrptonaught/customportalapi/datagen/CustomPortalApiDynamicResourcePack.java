package net.kyrptonaught.customportalapi.datagen;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.api.CustomPortalBuilder;
import net.kyrptonaught.customportalapi.compat.kjs.CustomPortalAPIKubeJSPlugin;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.SharedConstants;
import net.minecraft.core.Direction;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.registries.ForgeRegistries;
import org.codehaus.plexus.util.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CustomPortalApiDynamicResourcePack implements PackResources {

    private static final ObjectSet<String> CLIENT_DOMAINS = new ObjectOpenHashSet<>();
    private static final ConcurrentMap<ResourceLocation, byte[]> DATA = new ConcurrentHashMap<>();
    private final String name;

    static {
        CLIENT_DOMAINS.addAll(Sets.newHashSet(CustomPortalsMod.MOD_ID, "minecraft", "forge", "c"));
    }

    public CustomPortalApiDynamicResourcePack(String name) {
        this.name = name;
    }

    public static void generateAllAssets() {
        JsonObject langObj = new JsonObject();
        for(CustomPortalBuilder portal : CustomPortalAPIKubeJSPlugin.PORTALS) {
            langObj.addProperty("block." + portal.portalBlockId.toString().replace(":", "."), formatName(portal.portalBlockId.getPath()));
            Block block = ForgeRegistries.BLOCKS.getValue(portal.portalBlockId);
            JsonElement blockStateObj = MultiVariantGenerator.multiVariant(block).with(PropertyDispatch.property(BlockStateProperties.AXIS)
                    .select(Direction.Axis.X, Variant.variant().with(VariantProperties.MODEL, new ResourceLocation(CustomPortalsMod.MOD_ID, "block/customportalblock_ns")))
                    .select(Direction.Axis.Y, Variant.variant().with(VariantProperties.MODEL, new ResourceLocation(CustomPortalsMod.MOD_ID, "block/customportalblock_flat")))
                    .select(Direction.Axis.Z, Variant.variant().with(VariantProperties.MODEL, new ResourceLocation(CustomPortalsMod.MOD_ID, "block/customportalblock_ew")))).get();
            DATA.put(new ResourceLocation(CustomPortalsMod.MOD_ID, "blockstates/" + portal.portalBlockId.getPath() + ".json"), blockStateObj.toString().getBytes(StandardCharsets.UTF_8));
        }
        DATA.put(new ResourceLocation(CustomPortalsMod.MOD_ID, "lang/en_us.json"), langObj.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String formatName(String path) {
        path = path.replace("_", " ");
        return StringUtils.capitaliseAllWords(path);
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... strings) {
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType packType, ResourceLocation resourceLocation) {
        if(packType == PackType.CLIENT_RESOURCES) {
            if(DATA.containsKey(resourceLocation)) return () -> new ByteArrayInputStream(DATA.get(resourceLocation));
        }
        return null;
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        if(packType == PackType.CLIENT_RESOURCES) {
            if(!path.endsWith("/")) path += "/";
            final String finalPath = path;
            DATA.keySet().stream().filter(Objects::nonNull).filter(loc -> loc.getPath().startsWith(finalPath))
                    .forEach(id -> {
                        IoSupplier<InputStream> resource = this.getResource(packType, id);
                        if(resource != null) {
                            resourceOutput.accept(id, resource);
                        }
                    });
        }
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        return packType == PackType.CLIENT_RESOURCES ? CLIENT_DOMAINS : Set.of();
    }

    @Override
    public @Nullable <T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) {
        if(metadataSectionSerializer == PackMetadataSection.TYPE) {
            return (T) new PackMetadataSection(Component.literal("Custom Portal's Assets"),
                    SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES));
        }
        return null;
    }

    @Override
    public String packId() {
        return name;
    }

    @Override
    public void close() {}
}
