package de.robotricker.transportpipes.container;

import java.util.Collection;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.pipes.PipeDirection;
import de.robotricker.transportpipes.pipeutils.InventoryUtils;

public class SimpleInventoryContainer extends BlockContainer {

	private Chunk cachedChunk;
	private InventoryHolder cachedInvHolder;
	private Inventory cachedInv;

	public SimpleInventoryContainer(Block block) {
		super(block);
		this.cachedChunk = block.getChunk();
		this.cachedInvHolder = (InventoryHolder) block.getState();
		this.cachedInv = cachedInvHolder.getInventory();
	}

	@Override
	public ItemStack extractItem(PipeDirection extractDirection, int extractAmount) {
		if (!cachedChunk.isLoaded()) {
			return null;
		}
		if (isInvLocked(cachedInvHolder)) {
			return null;
		}
		for (int i = 0; i < cachedInv.getSize(); i++) {
			if (cachedInv.getItem(i) != null) {
				ItemStack is = cachedInv.getItem(i);
				cachedInv.setItem(i, InventoryUtils.changeAmount(is, -extractAmount));
				block.getState().update();
				ItemStack clonedIs = is.clone();
				clonedIs.setAmount(Math.min(is.getAmount(), extractAmount));
				return clonedIs;
			}
		}
		return null;
	}

	@Override
	public ItemStack insertItem(PipeDirection insertDirection, ItemStack insertion) {
		if (!cachedChunk.isLoaded()) {
			return insertion;
		}
		if (isInvLocked(cachedInvHolder)) {
			return insertion;
		}
		Collection<ItemStack> overflow = cachedInv.addItem(insertion).values();
		block.getState().update();
		if (overflow.isEmpty()) {
			return null;
		} else {
			return overflow.toArray(new ItemStack[0])[0];
		}
	}

	@Override
	public boolean isSpaceForItemAsync(PipeDirection insertDirection, ItemStack insertion) {
		if (!cachedChunk.isLoaded()) {
			return false;
		}
		if (isInvLocked(cachedInvHolder)) {
			return false;
		}
		for (int i = 0; i < cachedInv.getSize(); i++) {
			ItemStack is = cachedInv.getItem(i);
			if (is == null || is.getType() == Material.AIR) {
				return true;
			}
			if (is.isSimilar(insertion) && is.getAmount() < is.getMaxStackSize()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void updateBlock() {
		this.cachedChunk = block.getChunk();
		this.cachedInvHolder = ((InventoryHolder) block.getState());
		this.cachedInv = cachedInvHolder.getInventory();
	}

}
