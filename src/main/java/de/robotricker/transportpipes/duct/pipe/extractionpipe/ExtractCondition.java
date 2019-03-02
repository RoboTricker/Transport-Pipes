package de.robotricker.transportpipes.duct.pipe.extractionpipe;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum ExtractCondition {

    NEEDS_REDSTONE("Needs redstone", Material.REDSTONE),
    ALWAYS_EXTRACT("Always extract", Material.LIME_DYE),
    NEVER_EXTRACT("Never extract", Material.BARRIER);

    private String displayName;
    private ItemStack displayItem;

    ExtractCondition(String displayName, Material type) {
        this.displayName = displayName;
        this.displayItem = new ItemStack(type);
    }

    public String getDisplayName() {
        return displayName;
    }

    public ExtractCondition next() {
        int ordinal = ordinal();
        ordinal++;
        ordinal %= values().length;
        return values()[ordinal];
    }

    public ItemStack getDisplayItem() {
        return displayItem.clone();
    }

}
