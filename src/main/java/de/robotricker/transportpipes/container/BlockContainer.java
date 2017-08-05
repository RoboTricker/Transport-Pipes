package de.robotricker.transportpipes.container;

import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.api.TransportPipesContainer;

public abstract class BlockContainer implements TransportPipesContainer {

	private static boolean lockableExists = false;

	static {
		try {
			Class.forName("org.bukkit.block.Lockable");
			lockableExists = true;
		} catch (ClassNotFoundException e) {
			lockableExists = false;
		}
	}

	protected Block block;

	public BlockContainer(Block block) {
		this.block = block;
	}

	/**
	 * tries to add item {@link toPut} to item {@link before} and returns the result item.<br>
	 * if the item couldn't be inserted, the result item is equal to {@link before}.
	 */
	protected ItemStack putItemInSlot(ItemStack toPut, ItemStack before) {
		if (toPut == null) {
			return before;
		}
		if (before == null) {
			toPut.setAmount(0);
			return toPut;
		}
		ItemStack beforeItemStack = before.clone();
		if (beforeItemStack.isSimilar(toPut)) {
			int beforeAmount = beforeItemStack.getAmount();
			beforeItemStack.setAmount(Math.min(before.getMaxStackSize(), beforeAmount + toPut.getAmount()));
			toPut.setAmount(Math.max(0, toPut.getAmount() - (before.getMaxStackSize() - beforeAmount)));
		}
		return beforeItemStack;
	}

	protected boolean isSpaceForAtLeastOneItem(ItemStack toPut, ItemStack before) {
		if (toPut == null) {
			return false;
		}
		if (before == null) {
			return true;
		}
		if (before.isSimilar(toPut)) {
			if (before.getAmount() < before.getMaxStackSize()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	protected boolean isInvLocked(InventoryHolder ih) {
		if (lockableExists && ih instanceof org.bukkit.block.Lockable) {
			if (((org.bukkit.block.Lockable) ih).isLocked()) {
				return true;
			}
		}
		return false;
	}

	public abstract void updateBlock();

}
