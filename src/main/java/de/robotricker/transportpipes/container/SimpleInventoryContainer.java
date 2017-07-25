package de.robotricker.transportpipes.container;

import java.util.Collection;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.pipeitems.ItemData;
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
	public ItemData extractItem(PipeDirection extractDirection) {
		if (!cachedChunk.isLoaded()) {
			return null;
		}
		if (isInvLocked(cachedInvHolder)) {
			return null;
		}
		for (int i = 0; i < cachedInv.getSize(); i++) {
			if (cachedInv.getItem(i) != null) {
				ItemData id = new ItemData(cachedInv.getItem(i));
				cachedInv.setItem(i, InventoryUtils.decreaseAmountWithOne(cachedInv.getItem(i)));
				block.getState().update();
				return id;
			}
		}
		return null;
	}

	@Override
	public boolean insertItem(PipeDirection insertDirection, ItemData insertion) {
		if (!cachedChunk.isLoaded()) {
			return false;
		}
		if (isInvLocked(cachedInvHolder)) {
			return false;
		}
		Collection<ItemStack> overflow = cachedInv.addItem(insertion.toItemStack()).values();
		block.getState().update();
		return overflow.isEmpty();
	}

	@Override
	public boolean isSpaceForItemAsync(PipeDirection insertDirection, ItemData insertion) {
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
			if (is.isSimilar(insertion.toItemStack()) && is.getAmount() < is.getMaxStackSize()) {
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
