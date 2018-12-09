package de.robotricker.transportpipes.inventory;

import org.bukkit.Bukkit;

import javax.inject.Inject;

import de.robotricker.transportpipes.ItemService;
import de.robotricker.transportpipes.ducts.Duct;

public class DuctSettingsInventory extends StaticInventory {

    @Inject
    private ItemService itemService;

    private Duct duct;

    public DuctSettingsInventory(Duct duct) {
        this.duct = duct;
        this.inv = Bukkit.createInventory(null, 9 * 3, duct.getDuctType() + " Inventory");
    }

}
