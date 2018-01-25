package de.robotricker.transportpipes.duct.pipe.craftingpipe;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctInv;
import de.robotricker.transportpipes.utils.config.LocConf;

public class CraftingPipeProcessInv extends DuctInv {

	public CraftingPipeProcessInv(Duct duct) {
		super(duct, LocConf.load(LocConf.CRAFTINGPIPE_TITLE), 9);
	}

	@Override
	protected void populateInventory(Player p) {
		
	}

	@Override
	protected boolean notifyInvClick(Player p, int rawSlot) {
		return true;
	}

	@Override
	protected void notifyInvSave(Player p) {
		
	}
	
}
