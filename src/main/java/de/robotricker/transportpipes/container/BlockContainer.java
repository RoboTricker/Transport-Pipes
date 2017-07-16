package de.robotricker.transportpipes.container;

import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.pipeitems.ItemData;

public abstract class BlockContainer implements TransportPipesContainer {

	protected ItemStack putItemInSlot(ItemData toPut, ItemStack before) {
		ItemStack putItemStack = toPut.toItemStack();
		if (before == null) {
			return putItemStack;
		}
		if (before.isSimilar(putItemStack)) {
			int amountBefore = before.getAmount();
			if (amountBefore < before.getMaxStackSize()) {
				before.setAmount(amountBefore + 1);
				return before;
			} else {
				return before;
			}
		} else {
			return before;
		}
	}

}
