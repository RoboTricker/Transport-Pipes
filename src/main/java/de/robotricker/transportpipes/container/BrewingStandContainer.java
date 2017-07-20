package de.robotricker.transportpipes.container;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.pipes.PipeDirection;

public class BrewingStandContainer extends BlockContainer {

	private BrewingStand brewingStand;

	public BrewingStandContainer(Block block) {
		this.brewingStand = (BrewingStand) block.getState();
	}

	@Override
	public ItemData extractItem(PipeDirection extractDirection) {
		BrewerInventory inv = brewingStand.getInventory();

		if (extractDirection != PipeDirection.UP && brewingStand.getBrewingTime() == 0) {
			ItemStack taken = null;
			if (inv.getItem(0) != null) {
				taken = inv.getItem(0);
				inv.setItem(0, null);
			} else if (inv.getItem(1) != null) {
				taken = inv.getItem(1);
				inv.setItem(1, null);
			} else if (inv.getItem(2) != null) {
				taken = inv.getItem(2);
				inv.setItem(2, null);
			}
			if (taken != null) {
				return new ItemData(taken);
			}
		}

		return null;
	}

	@Override
	public boolean insertItem(PipeDirection insertDirection, ItemData insertion) {
		BrewerInventory inv = brewingStand.getInventory();

		ItemStack itemStack = insertion.toItemStack();

		if (itemStack.getType() == Material.POTION || itemStack.getType() == Material.SPLASH_POTION || itemStack.getType() == Material.LINGERING_POTION) {
			if (inv.getItem(0) == null) {
				inv.setItem(0, itemStack);
				return true;
			} else if (inv.getItem(1) == null) {
				inv.setItem(1, itemStack);
				return true;
			} else if (inv.getItem(2) == null) {
				inv.setItem(2, itemStack);
				return true;
			} else {
				return false;
			}
		} else if (insertDirection.isSide() && itemStack.getType() == Material.BLAZE_POWDER) {
			ItemStack oldFuel = inv.getFuel();
			if (canPutItemInSlot(insertion, oldFuel)) {
				inv.setFuel(putItemInSlot(insertion, oldFuel));
				return true;
			} else {
				return false;
			}
		} else if (isBrewingIngredient(itemStack)) {
			ItemStack oldIngredient = inv.getIngredient();
			if (canPutItemInSlot(insertion, oldIngredient)) {
				inv.setIngredient(putItemInSlot(insertion, oldIngredient));
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
		BrewerInventory inv = brewingStand.getInventory();

		ItemStack itemStack = insertion.toItemStack();

		if (itemStack.getType() == Material.POTION || itemStack.getType() == Material.SPLASH_POTION || itemStack.getType() == Material.LINGERING_POTION) {
			if (inv.getItem(0) != null && inv.getItem(1) != null && inv.getItem(2) != null) {
				return false;
			} else {
				return true;
			}
		} else if (insertDirection.isSide() && itemStack.getType() == Material.BLAZE_POWDER) {
			return canPutItemInSlot(insertion, inv.getFuel());
		} else if (isBrewingIngredient(itemStack)) {
			return canPutItemInSlot(insertion, inv.getIngredient());
		} else {
			return false;
		}
	}

	private static boolean isBrewingIngredient(ItemStack item) {
		if (item.getType() == Material.NETHER_STALK) {
			return true;
		}
		if (item.getType() == Material.SPECKLED_MELON) {
			return true;
		}
		if (item.getType() == Material.GHAST_TEAR) {
			return true;
		}
		if (item.getType() == Material.RABBIT_FOOT) {
			return true;
		}
		if (item.getType() == Material.BLAZE_POWDER) {
			return true;
		}
		if (item.getType() == Material.SPIDER_EYE) {
			return true;
		}
		if (item.getType() == Material.SUGAR) {
			return true;
		}
		if (item.getType() == Material.MAGMA_CREAM) {
			return true;
		}
		if (item.getType() == Material.GLOWSTONE_DUST) {
			return true;
		}
		if (item.getType() == Material.REDSTONE) {
			return true;
		}
		if (item.getType() == Material.FERMENTED_SPIDER_EYE) {
			return true;
		}
		if (item.getType() == Material.GOLDEN_CARROT) {
			return true;
		}
		if (item.getType() == Material.RAW_FISH && item.getData().getData() == 3) {
			return true;
		}
		if (item.getType() == Material.SULPHUR) {
			return true;
		}
		return false;
	}

}
