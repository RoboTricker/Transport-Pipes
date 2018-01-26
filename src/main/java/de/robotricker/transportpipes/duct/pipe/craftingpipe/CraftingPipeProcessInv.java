package de.robotricker.transportpipes.duct.pipe.craftingpipe;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctSharedInv;
import de.robotricker.transportpipes.duct.pipe.CraftingPipe;
import de.robotricker.transportpipes.duct.pipe.ExtractionPipe;
import de.robotricker.transportpipes.utils.config.LocConf;
import de.robotricker.transportpipes.utils.staticutils.InventoryUtils;

public class CraftingPipeProcessInv extends DuctSharedInv {

	public CraftingPipeProcessInv(Duct duct) {
		super(duct, LocConf.load(LocConf.CRAFTINGPIPE_TITLE), 18);
	}

	@Override
	protected void populateInventory(Player p, Inventory inventory) {
		CraftingPipe cp = (CraftingPipe) duct;

		String outputDirectionDisplayName = null;
		if (cp.getOutputDirection() == null) {
			outputDirectionDisplayName = LocConf.load(LocConf.CRAFTINGPIPE_DIRECTION_DONTOUTPUT);
		} else {
			outputDirectionDisplayName = String.format(LocConf.load(LocConf.CRAFTINGPIPE_DIRECTION_TEXT), cp.getOutputDirection().name());
		}
		ItemStack outputDirection = InventoryUtils.changeDisplayNameAndLore(new ItemStack(Material.TRIPWIRE_HOOK), outputDirectionDisplayName, LocConf.load(LocConf.CRAFTINGPIPE_DIRECTION_CLICKTOCHANGE));
		ItemStack glassPane = InventoryUtils.changeDisplayNameAndLore(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15), String.valueOf(ChatColor.RESET));

		// basic settings
		for (int i = 0; i < 9; i++) {
			if (i == 4) {
				inventory.setItem(i, outputDirection);
			} else {
				inventory.setItem(i, glassPane);
			}
		}
	}

	@Override
	protected boolean notifyInvClick(Player p, int rawSlot, Inventory inventory) {
		// clicked change output direction
		if (rawSlot == 4) {

			((CraftingPipe) duct).checkAndUpdateOutputDirection(true);

			// Update inv
			openOrUpdateInventory(p);
		}
		return true;
	}

	@Override
	protected void notifyInvSave(Player p, Inventory inventory) {

	}

}
