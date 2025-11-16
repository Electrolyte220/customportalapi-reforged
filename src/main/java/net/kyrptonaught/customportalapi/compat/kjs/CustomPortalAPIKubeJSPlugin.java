package net.kyrptonaught.customportalapi.compat.kjs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.api.CustomPortalBuilder;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayDeque;

public class CustomPortalAPIKubeJSPlugin extends KubeJSPlugin {

    public static final ArrayDeque<CustomPortalBuilder> PORTALS = new ArrayDeque<>();

    @Override
    public void registerEvents() {
        super.registerEvents();
        CustomPortalAPIStartupEvents.GROUP.register();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("CustomPortalBuilder", CustomPortalBuilder.class);
    }

    public static void registerKubePortals() {
        PORTALS.forEach(portal -> CustomPortalApiRegistry.addPortal(ForgeRegistries.BLOCKS.getValue(portal.portalLink.block), portal.portalLink));
    }
}
