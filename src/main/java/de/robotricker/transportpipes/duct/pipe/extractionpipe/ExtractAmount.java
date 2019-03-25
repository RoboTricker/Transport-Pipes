package de.robotricker.transportpipes.duct.pipe.extractionpipe;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.config.LangConf;

public enum ExtractAmount {

    EXTRACT_1(LangConf.Key.EXTRACT_AMOUNT_EXTRACT_1.get(), 1),
    EXTRACT_16(LangConf.Key.EXTRACT_AMOUNT_EXTRACT_16.get(), 16);

    private String displayName;
    private ItemStack displayItem;

    ExtractAmount(String displayName, int amount) {
        this.displayName = displayName;
        this.displayItem = new ItemStack(Material.BRICKS, amount);
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
