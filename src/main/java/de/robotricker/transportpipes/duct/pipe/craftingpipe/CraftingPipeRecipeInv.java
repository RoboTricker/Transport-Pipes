package de.robotricker.transportpipes.duct.pipe.craftingpipe;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctInv;
import de.robotricker.transportpipes.duct.pipe.CraftingPipe;
import de.robotricker.transportpipes.utils.config.LocConf;

public class CraftingPipeRecipeInv extends DuctInv {

	public CraftingPipeRecipeInv(Duct duct) {
		super(duct, LocConf.load(LocConf.CRAFTINGPIPE_TITLE), InventoryType.DISPENSER);
	}

	@Override
	protected void populateInventory(Player p) {

	}

	@Override
	protected boolean notifyInvClick(Player p, int rawSlot) {
		return false;
	}

	@Override
	protected void notifyInvSave(Player p) {
		CraftingPipe cp = (CraftingPipe) duct;
		for (int i = 0; i < 9; i++) {
			if (inventory.getItem(i) != null) {
				int oldAmount = inventory.getItem(i).getAmount();
				cp.getRecipeItems()[i] = inventory.getItem(i);
				cp.getRecipeItems()[i].setAmount(1);
				if (oldAmount > 1) {
					final ItemStack dropItem = inventory.getItem(i).clone();
					dropItem.setAmount(oldAmount - 1);
					Bukkit.getScheduler().runTask(TransportPipes.instance, new Runnable() {

						@Override
						public void run() {
							duct.getBlockLoc().getWorld().dropItem(duct.getBlockLoc().clone().add(0.5, 0.5, 0.5), dropItem);
						}
					});
				}
			} else {
				cp.getRecipeItems()[i] = null;
			}
		}
	}

}
