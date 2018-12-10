package de.robotricker.transportpipes.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public abstract class IndividualInventory {

    abstract Inventory create(Player p);

    public void openInv(Player p) {
        p.openInventory(create(p));
    }

}
