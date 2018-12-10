package de.robotricker.transportpipes.items;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

import de.robotricker.transportpipes.ducts.Duct;
import de.robotricker.transportpipes.ducts.types.DuctType;

public abstract class ItemManager<T extends Duct> {

    protected Map<DuctType, ItemStack> items;

    public ItemManager() {
        this.items = new HashMap<>();
    }

    public ItemStack getItem(DuctType ductType) {
        return items.get(ductType);
    }

    public ItemStack cloneItem(DuctType ductType) {
        return getItem(ductType).clone();
    }

    public abstract void registerItems();

}
