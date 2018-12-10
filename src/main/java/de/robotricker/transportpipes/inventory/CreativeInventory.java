package de.robotricker.transportpipes.inventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.inject.Inject;

import de.robotricker.transportpipes.ducts.DuctRegister;
import de.robotricker.transportpipes.items.ItemService;
import de.robotricker.transportpipes.ducts.Duct;
import de.robotricker.transportpipes.ducts.types.BaseDuctType;
import de.robotricker.transportpipes.ducts.types.DuctType;

public class CreativeInventory extends IndividualInventory {

    @Inject
    ItemService itemService;
    @Inject
    DuctRegister ductRegister;

    @Override
    Inventory create(Player p){
        Inventory inv = Bukkit.createInventory(null, 9 * 3, "Creative Inventory");

        int i = 0;
        for (BaseDuctType<? extends Duct> bdt : ductRegister.baseDuctTypes()) {
            for (DuctType dt : bdt.ductTypes()) {
                ItemStack ductItem = bdt.getItemManager().cloneItem(dt);
                ductItem.setAmount(16);
                inv.setItem(i++, ductItem);
            }
        }
        inv.setItem(i, itemService.getWrench().clone());

        return inv;
    }

}
