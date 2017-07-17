package de.robotricker.transportpipes.container;

import java.util.Collection;

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
		Inventory inv = inventoryHolder.getInventory();
		Collection<ItemStack> overflow = inv.addItem(insertion.toItemStack()).values();
		return overflow.isEmpty();
	}

}
