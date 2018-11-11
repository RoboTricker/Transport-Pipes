package de.robotricker.transportpipes.inventory;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.inject.Inject;

import de.robotricker.transportpipes.ItemService;
import de.robotricker.transportpipes.ducts.types.BaseDuctType;
import de.robotricker.transportpipes.ducts.types.DuctType;

public class CreativeDuctInventory {

    @Inject
    ItemService itemService;

    public Inventory createInventory(){
        Inventory inv = Bukkit.createInventory(null, 9 * 3, "Creative Inventory");

        int i = 0;
        for (BaseDuctType bdt : BaseDuctType.values()) {
            for (DuctType dt : bdt.ductTypeValues()) {
                ItemStack ductItem = dt.getItem().clone();
                ductItem.setAmount(16);
                inv.setItem(i++, ductItem);
            }
        }
        inv.setItem(i++, itemService.getWrench().clone());

        return inv;
    }

}
