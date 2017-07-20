package de.robotricker.transportpipes.container;

import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.pipes.PipeDirection;
import de.robotricker.transportpipes.pipeutils.InventoryUtils;
import de.robotricker.transportpipes.protocol.ReflectionManager;

public class FurnaceContainer extends BlockContainer {

	private Furnace furnace;

	public FurnaceContainer(Block block) {
		this.furnace = (Furnace) block.getState();
	}

	@Override
	public ItemData extractItem(PipeDirection extractDirection) {
		FurnaceInventory inv = furnace.getInventory();
		if (inv.getResult() != null) {
			ItemStack taken = InventoryUtils.createOneAmountItemStack(inv.getResult());
			inv.setResult(InventoryUtils.decreaseAmountWithOne(inv.getResult()));
			return new ItemData(taken);
		}
		return null;
	}

	@Override
	public boolean insertItem(PipeDirection insertDirection, ItemData insertion) {
		FurnaceInventory inv = furnace.getInventory();

		ItemStack itemStack = insertion.toItemStack();
		if (ReflectionManager.isFurnaceBurnableItem(itemStack)) {
			if (insertDirection.isSide() || insertDirection == PipeDirection.UP) {
				ItemStack oldSmelting = inv.getSmelting();
				if (canPutItemInSlot(insertion, oldSmelting)) {
					inv.setSmelting(putItemInSlot(insertion, oldSmelting));
					return true;
				} else {
					return false;
				}
			} else if (ReflectionManager.isFurnaceFuelItem(itemStack)) {
				ItemStack oldFuel = inv.getFuel();
				if (canPutItemInSlot(insertion, oldFuel)) {
					inv.setFuel(putItemInSlot(insertion, oldFuel));
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else if (ReflectionManager.isFurnaceFuelItem(itemStack)) {
			ItemStack oldFuel = inv.getFuel();
			if (canPutItemInSlot(insertion, oldFuel)) {
				inv.setFuel(putItemInSlot(insertion, oldFuel));
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
		FurnaceInventory inv = furnace.getInventory();

		ItemStack itemStack = insertion.toItemStack();

		if (ReflectionManager.isFurnaceBurnableItem(itemStack)) {
			if (insertDirection.isSide() || insertDirection == PipeDirection.UP) {
				return canPutItemInSlot(insertion, inv.getSmelting());
			} else if (ReflectionManager.isFurnaceFuelItem(itemStack)) {
				return canPutItemInSlot(insertion, inv.getFuel());
			} else {
				return false;
			}
		} else if (ReflectionManager.isFurnaceFuelItem(itemStack)) {
			return canPutItemInSlot(insertion, inv.getFuel());
		} else {
			return false;
		}
	}

}
