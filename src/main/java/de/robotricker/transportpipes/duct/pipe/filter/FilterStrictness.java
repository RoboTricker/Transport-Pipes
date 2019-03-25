package de.robotricker.transportpipes.duct.pipe.filter;

import de.robotricker.transportpipes.config.LangConf;

public enum FilterStrictness {

    MATERIAL(LangConf.Key.FILTER_STRICTNESS_MATERIAL.get()),
    MATERIAL_METADATA(LangConf.Key.FILTER_STRICTNESS_MATERIAL_METADATA.get());

    private String displayName;

    FilterStrictness(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public FilterStrictness next() {
        int ordinal = ordinal();
        ordinal++;
        ordinal %= values().length;
        return values()[ordinal];
    }

}
