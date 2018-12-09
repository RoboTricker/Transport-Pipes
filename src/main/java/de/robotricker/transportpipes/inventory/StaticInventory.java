package de.robotricker.transportpipes.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public abstract class StaticInventory {

    Inventory inv;

    public void openInv(Player p) {
        p.openInventory(inv);
    }

}
