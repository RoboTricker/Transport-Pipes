package de.robotricker.transportpipes.inventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.inject.Inject;

import de.robotricker.transportpipes.config.LangConf;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctRegister;
import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.duct.types.DuctType;
import de.robotricker.transportpipes.items.ItemService;

public class CreativeInventory extends IndividualInventory {

    @Inject
    ItemService itemService;
    @Inject
    DuctRegister ductRegister;

    @Override
    Inventory create(Player p){
        Inventory inv = Bukkit.createInventory(null, 9 * 3, LangConf.Key.DUCT_INVENTORY_CREATIVE_INVENTORY_TITLE.get());

        int i = 0;
        for (BaseDuctType<? extends Duct> bdt : ductRegister.baseDuctTypes()) {
            for (DuctType dt : bdt.ductTypes()) {
                ItemStack ductItem = bdt.getItemManager().getClonedItem(dt);
                ductItem.setAmount(16);
                inv.setItem(i++, ductItem);
            }
        }
        inv.setItem(i, itemService.getWrench().clone());

        return inv;
    }

}
