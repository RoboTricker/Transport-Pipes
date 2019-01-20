package de.robotricker.transportpipes.duct.pipe.filter;

public enum FilterMode {

    NORMAL("Normal"),
    INVERTED("Inverted"),
    BLOCK_ALL("Block all");

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
