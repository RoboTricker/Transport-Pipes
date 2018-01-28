package de.robotricker.transportpipes.container;

import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.duct.pipe.utils.FilteringMode;
import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.protocol.ReflectionManager;
import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.staticutils.InventoryUtils;
import io.sentry.Sentry;

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
	public ItemStack extractItem(WrappedDirection extractDirection, int extractAmount, List<ItemData> filterItems, FilteringMode filteringMode) {
		try {
			if (!cachedChunk.isLoaded()) {
				return null;
			}
			if (isInvLocked(cachedFurnace)) {
				return null;
			}
			if (cachedInv.getResult() != null && new ItemData(cachedInv.getResult()).checkFilter(filterItems, filteringMode)) {
				ItemStack taken = InventoryUtils.createOneAmountItemStack(cachedInv.getResult());
				cachedInv.setResult(InventoryUtils.changeAmount(cachedInv.getResult(), -extractAmount));
				ItemStack clonedTaken = taken.clone();
				clonedTaken.setAmount(Math.min(taken.getAmount(), extractAmount));
				return clonedTaken;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			Sentry.capture(exception);
		}
		return null;
	}

	@Override
	public ItemStack insertItem(WrappedDirection insertDirection, ItemStack insertion) {
		try {
			if (!cachedChunk.isLoaded()) {
				return insertion;
			}
			if (isInvLocked(cachedFurnace)) {
				return insertion;
			}
			if (ReflectionManager.isFurnaceBurnableItem(insertion)) {
				if (insertDirection.isSide() || insertDirection == WrappedDirection.UP) {
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
		} catch (Exception exception) {
			exception.printStackTrace();
			Sentry.capture(exception);
		}
		return insertion;
	}

	@Override
	public int howMuchSpaceForItemAsync(WrappedDirection insertDirection, ItemStack insertion) {
		try {
			if (!cachedChunk.isLoaded()) {
				return 0;
			}
			if (isInvLocked(cachedFurnace)) {
				return 0;
			}
			if (ReflectionManager.isFurnaceBurnableItem(insertion)) {
				if (insertDirection.isSide() || insertDirection == WrappedDirection.UP) {
					return howManyItemsFit(insertion, cachedInv.getSmelting());
				} else if (ReflectionManager.isFurnaceFuelItem(insertion)) {
					return howManyItemsFit(insertion, cachedInv.getFuel());
				} else {
					return 0;
				}
			} else if (ReflectionManager.isFurnaceFuelItem(insertion)) {
				return howManyItemsFit(insertion, cachedInv.getFuel());
			} else {
				return 0;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			Sentry.capture(exception);
		}
		return 0;
	}

	@Override
	public void updateBlock() {
		try {
			this.cachedChunk = block.getChunk();
			this.cachedFurnace = ((Furnace) block.getState());
			this.cachedInv = cachedFurnace.getInventory();
		} catch (Exception exception) {
			exception.printStackTrace();
			Sentry.capture(exception);
		}
	}

}
