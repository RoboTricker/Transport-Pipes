package de.robotricker.transportpipes.inventory;

import org.bukkit.Bukkit;

import javax.inject.Inject;

import de.robotricker.transportpipes.items.ItemService;
import de.robotricker.transportpipes.ducts.Duct;

public abstract class DuctSettingsInventory extends GlobalInventory {

    @Inject
    protected ItemService itemService;

    protected Duct duct;

    public final void setDuct(Duct duct) {
        this.duct = duct;
    }

    public abstract void populate();

}
