package de.robotricker.transportpipes.duct.pipe.filter;

public enum FilterStrictness {

    MATERIAL("Item Material"),
    MATERIAL_METADATA("Item Material and Metadata");

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
