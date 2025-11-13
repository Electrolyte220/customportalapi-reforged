package net.kyrptonaught.customportalapi.compat.kjs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import net.kyrptonaught.customportalapi.api.CustomPortalBuilder;

public class CustomPortalAPIKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void registerEvents() {
        super.registerEvents();
        CustomPortalAPIStartupEvents.GROUP.register();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("CustomPortalBuilder", CustomPortalBuilder.class);
    }

    public static void loadKubePortals() {
        CustomPortalAPIStartupEvents.REGISTER.post(new RegisterPortalEvent());
    }
}
