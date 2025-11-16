package net.kyrptonaught.customportalapi.compat.create;

import com.simibubi.create.api.contraption.train.PortalTrackProvider;
import com.simibubi.create.content.trains.track.AllPortalTracks;
import net.kyrptonaught.customportalapi.util.CustomTeleporter;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

public class CreateIntegration {

    public static void portalIntegration(PortalLink portalLink) {
        PortalTrackProvider provider = (l, f) ->
                    PortalTrackProvider.fromProbe(l, f, ResourceKey.create(Registries.DIMENSION, portalLink.returnDimID), ResourceKey.create(Registries.DIMENSION, portalLink.dimID),
                            (ol, e) -> CustomTeleporter.customTPTarget(ol, e, e.blockPosition(), portalLink.getPortalBlock().getPortalBase(l, e.blockPosition()), portalLink.getFrameTester()));
            AllPortalTracks.tryRegisterIntegration(BuiltInRegistries.BLOCK.getKey(portalLink.getPortalBlock()), provider);
    }
}
