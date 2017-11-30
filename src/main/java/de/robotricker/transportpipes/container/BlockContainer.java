package de.robotricker.transportpipes.container;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.api.TransportPipesContainer;

public abstract class BlockContainer implements TransportPipesContainer {

	private static boolean vanillaLockableExists = false;
	private static boolean lwcLockableExists = false;

	static {
		try {
			Class.forName("org.bukkit.block.Lockable");
			vanillaLockableExists = true;
		} catch (ClassNotFoundException e) {
			vanillaLockableExists = false;
		}
		lwcLockableExists = Bukkit.getPluginManager().isPluginEnabled("LWC");
	}

	protected Block block;

	public BlockContainer(Block block) {
		this.block = block;
	}

	/**
	 * tries to add item {@link toPut} to item {@link before} and returns the result
	 * item.<br>
	 * if the item couldn't be inserted, the result item is equal to {@link before}.
	 */
	protected ItemStack putItemInSlot(ItemStack toPut, ItemStack before) {
		if (toPut == null) {
			return before;
		}
		if (before == null) {
			ItemStack returnCopy = toPut.clone();
			toPut.setAmount(0);
			return returnCopy;
		}
		ItemStack beforeItemStack = before.clone();
		if (beforeItemStack.isSimilar(toPut)) {
			int beforeAmount = beforeItemStack.getAmount();
			beforeItemStack.setAmount(Math.min(before.getMaxStackSize(), beforeAmount + toPut.getAmount()));
			toPut.setAmount(Math.max(0, toPut.getAmount() - (before.getMaxStackSize() - beforeAmount)));
		}
		return beforeItemStack;
	}

	protected int howManyItemsFit(ItemStack toPut, ItemStack before) {
		if (toPut == null) {
			return 0;
		}
		if (before == null) {
			return toPut.getMaxStackSize();
		}
		if (before.isSimilar(toPut)) {
			if (before.getAmount() < before.getMaxStackSize()) {
				return before.getMaxStackSize() - before.getAmount();
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}

	protected boolean isInvLocked(InventoryHolder ih) {
		// check vanilla lock
		if (vanillaLockableExists && ih instanceof org.bukkit.block.Lockable) {
			if (((org.bukkit.block.Lockable) ih).isLocked()) {
				return true;
			}
		}
		// check lwc lock
		if (lwcLockableExists) {
			//com.griefcraft.model.Protection protection = com.griefcraft.lwc.LWC.getInstance().getPhysicalDatabase().loadProtection(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
			//if (protection != null) {
				// if (com.griefcraft.lwc.LWC.getInstance().findProtection(block) != null) {
				//return true;
			//}
		}
		return false;

	}

	public abstract void updateBlock();

}
