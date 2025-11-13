package net.kyrptonaught.customportalapi.compat.kjs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface CustomPortalAPIStartupEvents {

    EventGroup GROUP = EventGroup.of("CustomPortalAPIStartupEvents");

    EventHandler REGISTER = GROUP.startup("register", () -> RegisterPortalEvent.class);
}
