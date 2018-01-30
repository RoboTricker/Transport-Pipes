package de.robotricker.transportpipes.duct.pipe.craftingpipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
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
					TransportPipes.runTask(new Runnable() {

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
			final Map<ItemData, Integer> removeItemDatas = new HashMap<>();
			for (int i = 1; i < 10; i++) {
				if (e.getInventory().getItem(i) != null) {
					ItemStack toRemove = e.getInventory().getItem(i);
					ItemData toRemoveId = new ItemData(toRemove);
					if (!removeItemDatas.containsKey(toRemoveId)) {
						removeItemDatas.put(toRemoveId, toRemove.getAmount());
					} else {
						removeItemDatas.put(toRemoveId, removeItemDatas.get(toRemoveId) + toRemove.getAmount());
					}
				}
			}

			super.onClose(e);

			TransportPipes.runTask(new Runnable() {

				@Override
				public void run() {
					List<ItemStack> preventedDrops = lastPreventedDrops.remove(p);
					if (preventedDrops == null) {
						preventedDrops = new ArrayList<>();
					}
					for (ItemData id : removeItemDatas.keySet()) {
						for (ItemStack preventedIs : preventedDrops) {
							if (preventedIs.isSimilar(id.toItemStack())) {
								removeItemDatas.put(id, removeItemDatas.get(id) - preventedIs.getAmount());
							}
						}
					}
					
					//remove items from inv
					for(ItemData id : removeItemDatas.keySet()) {
						
						int totalSubtract = removeItemDatas.get(id);
						int totalSubtracted = 0;
						int subtractAmount;
						do {
							subtractAmount = Math.min(id.toItemStack().getMaxStackSize(), totalSubtract - totalSubtracted);

							ItemStack subtractItemStack = id.toItemStack();
							subtractItemStack.setAmount(subtractAmount);
							p.getInventory().removeItem(subtractItemStack);
							
							totalSubtracted += subtractAmount;
						} while(totalSubtracted < totalSubtract);
						
					}
					
					p.updateInventory();
				}
			});
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		if (e.getPlayer().getOpenInventory() != null && e.getPlayer().getOpenInventory().getTopInventory() != null) {
			if (getLastPlayerInventory(e.getPlayer()) != null && getLastPlayerInventory(e.getPlayer()).equals(e.getPlayer().getOpenInventory().getTopInventory())) {
				List<ItemStack> drops = lastPreventedDrops.get(e.getPlayer());
				if (drops == null) {
					drops = new ArrayList<ItemStack>();
					lastPreventedDrops.put(e.getPlayer(), drops);
				}
				drops.add(e.getItemDrop().getItemStack());
				e.setCancelled(true);
			}
		}
	}

}
