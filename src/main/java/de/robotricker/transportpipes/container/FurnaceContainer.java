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
				ItemStack newSmelting = putItemInSlot(insertion, oldSmelting);
				if (oldSmelting != null && oldSmelting.getAmount() == newSmelting.getAmount()) {
					return false;
				} else {
					inv.setSmelting(newSmelting);
				}
			} else if (ReflectionManager.isFurnaceFuelItem(itemStack)) {
				ItemStack oldFuel = inv.getFuel();
				ItemStack newFuel = putItemInSlot(insertion, oldFuel);
				if (oldFuel != null && oldFuel.getAmount() == newFuel.getAmount()) {
					return false;
				} else {
					inv.setFuel(newFuel);
				}
			}
		} else if (ReflectionManager.isFurnaceFuelItem(itemStack)) {
			ItemStack oldFuel = inv.getFuel();
			ItemStack newFuel = putItemInSlot(insertion, oldFuel);
			if (oldFuel != null && oldFuel.getAmount() == newFuel.getAmount()) {
				return false;
			} else {
				inv.setFuel(newFuel);
			}
		} else {
			return false;
		}

		return true;

	}

	@Override
	public boolean isSpaceForItem(PipeDirection insertDirection, ItemData insertion) {
		return false; s
	}

}
