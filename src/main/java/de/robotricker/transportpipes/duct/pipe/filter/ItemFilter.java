package de.robotricker.transportpipes.duct.pipe.filter;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemFilter {

    public static final int MAX_ITEMS_PER_ROW = 32;

    private ItemData[] filterItems;
    private FilterMode filterMode;
    private FilterStrictness filterStrictness;

    public ItemFilter() {
        filterItems = new ItemData[MAX_ITEMS_PER_ROW];
        filterMode = FilterMode.NORMAL;
        filterStrictness = FilterStrictness.TYPE_DAMAGE_METADATA;
    }

    public ItemData[] getFilterItems() {
        return filterItems;
    }

    public FilterMode getFilterMode() {
        return filterMode;
    }

    public void setFilterMode(FilterMode filterMode) {
        this.filterMode = filterMode;
    }

    public FilterStrictness getFilterStrictness() {
        return filterStrictness;
    }

    public void setFilterStrictness(FilterStrictness filterStrictness) {
        this.filterStrictness = filterStrictness;
    }

    public int applyFilter(ItemStack item) {
        if (getFilterMode() == FilterMode.BLOCK_ALL) {
            return 0;
        }
        if (getFilterMode() == FilterMode.NORMAL) {
            int weight = 0;
            for (ItemData id : filterItems) {
                if (id != null && matchesItemStrictness(id.toItemStack(), item)) {
                    weight++;
                }
            }
            return weight;
        }
        if (getFilterMode() == FilterMode.INVERTED) {
            for (ItemData id : filterItems) {
                if (id != null && matchesItemStrictness(id.toItemStack(), item)) {
                    return 0;
                }
            }
            return 1;
        }
        return 0;
    }

    private boolean matchesItemStrictness(ItemStack mask, ItemStack itemStack) {
        switch (getFilterStrictness()) {
            case TYPE:
                return mask.getType() == itemStack.getType();
            case TYPE_DAMAGE:
                return mask.getType() == itemStack.getType() && mask.getData().equals(itemStack.getData());
            case TYPE_METADATA:
                return mask.getType() == itemStack.getType() && mask.getItemMeta().equals(itemStack.getItemMeta());
            case TYPE_DAMAGE_METADATA:
                return mask.getType() == itemStack.getType() && mask.getData().equals(itemStack.getData()) && mask.getItemMeta().equals(itemStack.getItemMeta());
            default:
                return false;
        }
    }

    public List<ItemStack> getAsItemStacks() {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (int i = 0; i < MAX_ITEMS_PER_ROW; i++) {
            if (filterItems[i] != null) {
                itemStacks.add(filterItems[i].toItemStack());
            }
        }
        return itemStacks;
    }

}
