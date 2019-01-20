package de.robotricker.transportpipes.duct.pipe.extractionpipe;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum ExtractCondition {

    NEEDS_REDSTONE("Needs redstone", Material.REDSTONE, (short) 0),
    ALWAYS_EXTRACT("Always extract", Material.INK_SACK, (short) 10),
    NEVER_EXTRACT("Never extract", Material.BARRIER, (short) 0);

    private String displayName;
    private ItemStack displayItem;

    ExtractCondition(String displayName, Material type, short damage) {
        this.displayName = displayName;
        this.displayItem = new ItemStack(type, 1, damage);
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
