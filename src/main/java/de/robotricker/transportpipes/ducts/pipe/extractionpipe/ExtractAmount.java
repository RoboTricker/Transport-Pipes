package de.robotricker.transportpipes.ducts.pipe.extractionpipe;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum ExtractAmount {

    EXTRACT_1("1", 1),
    EXTRACT_16("16", 16);

    private String displayName;
    private ItemStack displayItem;

    ExtractAmount(String displayName, int amount) {
        this.displayName = displayName;
        this.displayItem = new ItemStack(Material.BRICK, amount);
    }

    public int getAmount() {
        return displayItem.getAmount();
    }

    public String getDisplayName() {
        return displayName;
    }

    public ExtractAmount next() {
        int ordinal = ordinal();
        ordinal++;
        ordinal %= values().length;
        return values()[ordinal];
    }

    public ItemStack getDisplayItem() {
        return displayItem.clone();
    }

}
