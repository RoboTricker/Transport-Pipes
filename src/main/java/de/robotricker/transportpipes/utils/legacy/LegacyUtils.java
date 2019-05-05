package de.robotricker.transportpipes.utils.legacy;

import org.bukkit.Material;

public abstract class LegacyUtils {

    private static LegacyUtils instance;

    public abstract Material getRedDye();

    public abstract Material getYellowDye();

    public abstract Material getGreenDye();

    public static LegacyUtils getInstance() {
        return instance;
    }

    public static void setInstance(LegacyUtils instance) {
        LegacyUtils.instance = instance;
    }
}
