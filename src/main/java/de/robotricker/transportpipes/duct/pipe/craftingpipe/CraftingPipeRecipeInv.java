package de.robotricker.transportpipes.duct.pipe.craftingpipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctPlayerInv;
import de.robotricker.transportpipes.duct.DuctSharedInv;
import de.robotricker.transportpipes.duct.pipe.CraftingPipe;
import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.utils.config.LocConf;

public class CraftingPipeRecipeInv extends DuctPlayerInv {

	public CraftingPipeRecipeInv(Duct duct) {
		super(duct, true);
	}

	protected Inventory openCustomInventory(Player p) {
		return p.openWorkbench(null, true).getTopInventory();
	}

	@Override
	public void populateInventory(Player p, Inventory inventory) {
		CraftingPipe cp = (CraftingPipe) duct;
		for (int i = 0; i < 9; i++) {
			inventory.setItem(i + 1, cp.getRecipeItems()[i] == null ? null : cp.getRecipeItems()[i].toItemStack());
		}
	}

	@Override
	protected boolean notifyInvClick(Player p, int rawSlot, Inventory inventory) {
		return rawSlot == 0;
	}

	@Override
	protected void notifyInvSave(Player p, Inventory inventory) {
		super.notifyInvSave(p, inventory);

		CraftingPipe cp = (CraftingPipe) duct;
		for (int i = 0; i < 9; i++) {
			if (inventory.getItem(i + 1) != null) {
				int oldAmount = inventory.getItem(i + 1).getAmount();
				cp.getRecipeItems()[i] = new ItemData(inventory.getItem(i + 1));
				if (oldAmount > 1) {
					final ItemStack dropItem = inventory.getItem(i + 1).clone();
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
		if (inventory.getItem(0) != null) {
			cp.setRecipeResult(inventory.getItem(0).clone());
		} else {
			cp.setRecipeResult(null);
		}
	}
	
	@Override
	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		if (e.getInventory() != null && containsInventory(e.getInventory()) && e.getPlayer() instanceof Player) {
			final Player p = (Player) e.getPlayer();
			final List<ItemStack> removeItems = new ArrayList<>();
			for (int i = 1; i < 10; i++) {
				if (e.getInventory().getItem(i) != null) {
					removeItems.add(e.getInventory().getItem(i).clone());
				}
			}
			
			super.onClose(e);

			Bukkit.getScheduler().runTask(TransportPipes.instance, new Runnable() {
				
				@Override
				public void run() {
					p.getInventory().removeItem(removeItems.toArray(new ItemStack[0]));
					p.updateInventory();
				}
			});
		}
	}

}
