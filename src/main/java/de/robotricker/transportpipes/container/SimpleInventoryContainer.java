package de.robotricker.transportpipes.container;

import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.pipes.PipeDirection;
import de.robotricker.transportpipes.pipeutils.InventoryUtils;

public class SimpleInventoryContainer extends BlockContainer {

	private InventoryHolder inventoryHolder;

	public SimpleInventoryContainer(Block block) {
		this.inventoryHolder = (InventoryHolder) block.getState();
	}

	@Override
	public ItemData extractItem(PipeDirection extractDirection) {
		if (isInvLocked(inventoryHolder)) {
			return null;
		}
		Inventory inv = inventoryHolder.getInventory();
		for (int i = 0; i < inv.getSize(); i++) {
			if (inv.getItem(i) != null) {
				ItemData id = new ItemData(inv.getItem(i));
				inv.setItem(i, InventoryUtils.decreaseAmountWithOne(inv.getItem(i)));
				return id;
			}
		}
		return null;
	}

	@Override
	public boolean insertItem(PipeDirection insertDirection, ItemData insertion) {
		if (isInvLocked(inventoryHolder)) {
			return false;
		}
		Inventory inv = inventoryHolder.getInventory();
		Collection<ItemStack> overflow = inv.addItem(insertion.toItemStack()).values();
		return overflow.isEmpty();
	}

	@Override
	public boolean isSpaceForItemAsync(PipeDirection insertDirection, ItemData insertion) {
		if (isInvLocked(inventoryHolder)) {
			return false;
		}
		Inventory inv = inventoryHolder.getInventory();
		for (int i = 0; i < inv.getSize(); i++) {
			ItemStack is = inv.getItem(i);
			if (is == null || is.getType() == Material.AIR) {
				return true;
			}
			if (is.isSimilar(insertion.toItemStack()) && is.getAmount() < is.getMaxStackSize()) {
				return true;
			}
		}
		return false;
	}

}
