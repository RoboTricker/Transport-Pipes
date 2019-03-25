package de.robotricker.transportpipes.duct.pipe.extractionpipe;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.config.LangConf;

public enum ExtractCondition {

    NEEDS_REDSTONE(LangConf.Key.EXTRACT_CONDITION_NEEDS_REDSTONE.get(), Material.REDSTONE),
    ALWAYS_EXTRACT(LangConf.Key.EXTRACT_CONDITION_ALWAYS_EXTRACT.get(), Material.LIME_DYE),
    NEVER_EXTRACT(LangConf.Key.EXTRACT_CONDITION_NEVER_EXTRACT.get(), Material.BARRIER);

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
