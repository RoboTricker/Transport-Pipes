package de.robotricker.transportpipes.container;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.pipeitems.ItemData;
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
	public ItemData extractItem(PipeDirection extractDirection) {
		if (!cachedChunk.isLoaded()) {
			return null;
		}
		if (isInvLocked(cachedFurnace)) {
			return null;
		}
		if (cachedInv.getResult() != null) {
			ItemStack taken = InventoryUtils.createOneAmountItemStack(cachedInv.getResult());
			cachedInv.setResult(InventoryUtils.decreaseAmountWithOne(cachedInv.getResult()));
			return new ItemData(taken);
		}
		return null;
	}

	@Override
	public boolean insertItem(PipeDirection insertDirection, ItemData insertion) {
		if (!cachedChunk.isLoaded()) {
			return false;
		}
		if (isInvLocked(cachedFurnace)) {
			return false;
		}
		ItemStack itemStack = insertion.toItemStack();
		if (ReflectionManager.isFurnaceBurnableItem(itemStack)) {
			if (insertDirection.isSide() || insertDirection == PipeDirection.UP) {
				ItemStack oldSmelting = cachedInv.getSmelting();
				if (canPutItemInSlot(insertion, oldSmelting)) {
					cachedInv.setSmelting(putItemInSlot(insertion, oldSmelting));
					return true;
				} else {
					return false;
				}
			} else if (ReflectionManager.isFurnaceFuelItem(itemStack)) {
				ItemStack oldFuel = cachedInv.getFuel();
				if (canPutItemInSlot(insertion, oldFuel)) {
					cachedInv.setFuel(putItemInSlot(insertion, oldFuel));
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else if (ReflectionManager.isFurnaceFuelItem(itemStack)) {
			ItemStack oldFuel = cachedInv.getFuel();
			if (canPutItemInSlot(insertion, oldFuel)) {
				cachedInv.setFuel(putItemInSlot(insertion, oldFuel));
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}

	}

	@Override
	public boolean isSpaceForItemAsync(PipeDirection insertDirection, ItemData insertion) {
		if (!cachedChunk.isLoaded()) {
			return false;
		}
		if (isInvLocked(cachedFurnace)) {
			return false;
		}
		ItemStack itemStack = insertion.toItemStack();
		if (ReflectionManager.isFurnaceBurnableItem(itemStack)) {
			if (insertDirection.isSide() || insertDirection == PipeDirection.UP) {
				return canPutItemInSlot(insertion, cachedInv.getSmelting());
			} else if (ReflectionManager.isFurnaceFuelItem(itemStack)) {
				return canPutItemInSlot(insertion, cachedInv.getFuel());
			} else {
				return false;
			}
		} else if (ReflectionManager.isFurnaceFuelItem(itemStack)) {
			return canPutItemInSlot(insertion, cachedInv.getFuel());
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
