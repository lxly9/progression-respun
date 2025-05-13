package com.progression_respun.compat;

import net.fabricmc.loader.api.FabricLoader;

public final class CompatMods {
    public static final boolean GALOSPHERE = FabricLoader.getInstance().isModLoaded("galosphere");
    public static final boolean ENDERSCAPE = FabricLoader.getInstance().isModLoaded("enderscape");

    private CompatMods() {
    }

    public static void initialize() {
        if (GALOSPHERE) {
            GalosphereItems.initialize();
        }

        if (ENDERSCAPE) {
            EnderscapeItems.initialize();
        }
    }
}
