package de.robotricker.transportpipes.items;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.types.DuctType;

public abstract class ItemManager<T extends Duct> {

    protected Map<DuctType, ItemStack> items;

    public ItemManager() {
        this.items = new HashMap<>();
    }

    public ItemStack getItem(DuctType ductType) {
        return items.get(ductType);
    }

    public ItemStack getClonedItem(DuctType ductType) {
        return getItem(ductType).clone();
    }

    public abstract void registerItems();

}
