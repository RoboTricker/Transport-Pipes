package de.robotricker.transportpipes.ducts.pipe.goldenpipe;

import org.bukkit.inventory.ItemStack;

public class ItemData {

    private ItemStack backedItem;

    public ItemData(ItemStack item) {
        this.backedItem = item.clone();
        this.backedItem.setAmount(1);
    }

    public ItemStack toItemStack() {
        return backedItem.clone();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((backedItem == null) ? 0 : backedItem.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ItemData other = (ItemData) obj;
        if (backedItem == null) {
            if (other.backedItem != null)
                return false;
        } else if (!backedItem.isSimilar(other.backedItem))
            return false;
        return true;
    }

}

