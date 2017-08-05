package de.robotricker.transportpipes.container;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.pipes.PipeDirection;
import de.robotricker.transportpipes.pipeutils.InventoryUtils;
import de.robotricker.transportpipes.protocol.ReflectionManager;

public class FurnaceContainer extends BlockContainer {

	private Chunk cachedChunk;
	private Furnace cachedFurnace;
	private FurnaceInventory cachedInv;

	public FurnaceContainer(Block block) {
		super(block);
		this.cachedChunk = block.getChunk();
		this.cachedFurnace = (Furnace) block.getState();
		this.cachedInv = cachedFurnace.getInventory();
	}

	@Override
	public ItemStack extractItem(PipeDirection extractDirection, int extractAmount) {
		if (!cachedChunk.isLoaded()) {
			return null;
		}
		if (isInvLocked(cachedFurnace)) {
			return null;
		}
		if (cachedInv.getResult() != null) {
			ItemStack taken = InventoryUtils.createOneAmountItemStack(cachedInv.getResult());
			cachedInv.setResult(InventoryUtils.changeAmount(cachedInv.getResult(), -extractAmount));
			ItemStack clonedTaken = taken.clone();
			clonedTaken.setAmount(Math.min(taken.getAmount(), extractAmount));
			return clonedTaken;
		}
		return null;
	}

	@Override
	public ItemStack insertItem(PipeDirection insertDirection, ItemStack insertion) {
		if (!cachedChunk.isLoaded()) {
			return insertion;
		}
		if (isInvLocked(cachedFurnace)) {
			return insertion;
		}
		if (ReflectionManager.isFurnaceBurnableItem(insertion)) {
			if (insertDirection.isSide() || insertDirection == PipeDirection.UP) {
				ItemStack oldSmelting = cachedInv.getSmelting();
				cachedInv.setSmelting(putItemInSlot(insertion, oldSmelting));
				if (insertion == null || insertion.getAmount() == 0) {
					insertion = null;
				}
			} else if (ReflectionManager.isFurnaceFuelItem(insertion)) {
				ItemStack oldFuel = cachedInv.getFuel();
				cachedInv.setFuel(putItemInSlot(insertion, oldFuel));
				if (insertion == null || insertion.getAmount() == 0) {
					insertion = null;
				}
			}
		} else if (ReflectionManager.isFurnaceFuelItem(insertion)) {
			ItemStack oldFuel = cachedInv.getFuel();
			cachedInv.setFuel(putItemInSlot(insertion, oldFuel));
			if (insertion == null || insertion.getAmount() == 0) {
				insertion = null;
			}
		}
		return insertion;
	}

	@Override
	public boolean isSpaceForItemAsync(PipeDirection insertDirection, ItemStack insertion) {
		if (!cachedChunk.isLoaded()) {
			return false;
		}
		if (isInvLocked(cachedFurnace)) {
			return false;
		}
		if (ReflectionManager.isFurnaceBurnableItem(insertion)) {
			if (insertDirection.isSide() || insertDirection == PipeDirection.UP) {
				return isSpaceForAtLeastOneItem(insertion, cachedInv.getSmelting());
			} else if (ReflectionManager.isFurnaceFuelItem(insertion)) {
				return isSpaceForAtLeastOneItem(insertion, cachedInv.getFuel());
			} else {
				return false;
			}
		} else if (ReflectionManager.isFurnaceFuelItem(insertion)) {
			return isSpaceForAtLeastOneItem(insertion, cachedInv.getFuel());
		} else {
			return false;
		}
	}

	@Override
	public void updateBlock() {
		this.cachedChunk = block.getChunk();
		this.cachedFurnace = ((Furnace) block.getState());
		this.cachedInv = cachedFurnace.getInventory();
	}

}
