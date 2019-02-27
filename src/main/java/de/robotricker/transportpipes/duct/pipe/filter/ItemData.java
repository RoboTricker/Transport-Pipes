package de.robotricker.transportpipes.duct.pipe.filter;

import org.bukkit.inventory.ItemStack;

public class ItemData {

    private ItemStack backedItem;

    public ItemData(ItemStack item) {
        this(item, false);
    }

    public ItemData(ItemStack item, boolean ignoreMeta) {
        if (ignoreMeta) {
            this.backedItem = new ItemStack(item.getType(), 1, item.getData().getData());
        } else {
            this.backedItem = item.clone();
            this.backedItem.setAmount(1);
        }
    }

    public ItemStack toItemStack() {
        return backedItem.clone();
    }

    @Override
    public int hashCode() {
        int hash = 1;

        hash = hash * 31 + backedItem.getTypeId();
        hash = hash * 31 + (backedItem.getDurability() & 0xffff);
        hash = hash * 31 + (backedItem.hasItemMeta() ? backedItem.getItemMeta().hashCode() : 0);

        return hash;
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
            return other.backedItem == null;
        } else if (backedItem.isSimilar(other.backedItem)) {
            return true;
        } else {
            if (backedItem.getData().getData() == -1) {
                return backedItem.getType().equals(other.backedItem.getType());
            } else if (other.backedItem.getData().getData() == -1) {
                return other.backedItem.getType().equals(backedItem.getType());
            }
            return false;
        }
    }

    @Override
    public String toString() {
        return backedItem.toString();
    }
}

