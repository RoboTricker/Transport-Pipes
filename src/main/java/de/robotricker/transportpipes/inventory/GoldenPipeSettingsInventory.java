package de.robotricker.transportpipes.inventory;

import org.bukkit.Bukkit;

public class GoldenPipeSettingsInventory extends DuctSettingsInventory {

    @Override
    public void populate() {
        this.inv = Bukkit.createInventory(null, 6 * 9, duct.getDuctType().getFormattedTypeName() + " Pipe §rInventory");
    }

}
