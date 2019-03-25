package de.robotricker.transportpipes.duct.pipe.filter;

import de.robotricker.transportpipes.config.LangConf;

public enum FilterMode {

    NORMAL(LangConf.Key.FILTER_MODE_NORMAL.get()),
    INVERTED(LangConf.Key.FILTER_MODE_INVERTED.get()),
    BLOCK_ALL(LangConf.Key.FILTER_MODE_BLOCKALL.get());

    private String displayName;

    FilterMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public FilterMode next() {
        int ordinal = ordinal();
        ordinal++;
        ordinal %= values().length;
        return values()[ordinal];
    }

}
