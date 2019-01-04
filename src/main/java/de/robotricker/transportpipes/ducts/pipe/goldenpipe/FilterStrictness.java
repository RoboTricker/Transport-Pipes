package de.robotricker.transportpipes.ducts.pipe.goldenpipe;

public enum FilterStrictness {

    TYPE("Item Type"),
    TYPE_DAMAGE("Item Type and Damage"),
    TYPE_METADATA("Item Type and Metadata"),
    TYPE_DAMAGE_METADATA("Item Type, Damage and Metadata");

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
