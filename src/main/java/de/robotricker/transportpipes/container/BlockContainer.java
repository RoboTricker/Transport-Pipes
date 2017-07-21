package de.robotricker.transportpipes.container;

import org.bukkit.block.Lockable;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.pipeitems.ItemData;

public abstract class BlockContainer implements TransportPipesContainer {

	/**
	 * tries to add item {@link toPut} to item {@link before} and returns the result item.<br>
	 * if the item couldn't be inserted, before is equal to the result item.
	 */
	protected ItemStack putItemInSlot(ItemData toPut, ItemStack before) {
		if (toPut == null) {
			return before;
		}
		ItemStack putItemStack = toPut.toItemStack();
		if (before == null) {
			return putItemStack;
		}
		ItemStack beforeItemStack = before.clone();
		if (beforeItemStack.isSimilar(putItemStack)) {
			int amountBefore = beforeItemStack.getAmount();
			if (amountBefore < beforeItemStack.getMaxStackSize()) {
				beforeItemStack.setAmount(amountBefore + 1);
				return beforeItemStack;
			} else {
				return beforeItemStack;
			}
		} else {
			return beforeItemStack;
		}
	}

	protected boolean canPutItemInSlot(ItemData toPut, ItemStack before) {
		if (toPut == null) {
			return false;
		}
		ItemStack putItemStack = toPut.toItemStack();
		if (before == null) {
			return true;
		}
		if (before.isSimilar(putItemStack)) {
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
		if (ih instanceof Lockable) {
			if (((Lockable) ih).isLocked()) {
				return true;
			}
		}
		return false;
	}

}
