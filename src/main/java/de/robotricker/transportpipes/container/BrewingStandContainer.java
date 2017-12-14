package de.robotricker.transportpipes.container;

import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.duct.pipe.utils.FilteringMode;
import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.utils.WrappedDirection;

public class BrewingStandContainer extends BlockContainer {

	private Chunk cachedChunk;
	private BrewingStand cachedBrewingStand;
	private BrewerInventory cachedInv;

	public BrewingStandContainer(Block block) {
		super(block);
		this.cachedChunk = block.getChunk();
		this.cachedBrewingStand = (BrewingStand) block.getState();
		this.cachedInv = cachedBrewingStand.getInventory();
	}

	@Override
	public ItemStack extractItem(WrappedDirection extractDirection, int extractAmount, List<ItemData> filterItems, FilteringMode filteringMode) {
		if (!cachedChunk.isLoaded()) {
			return null;
		}
		if (isInvLocked(cachedBrewingStand)) {
			return null;
		}
		if (extractDirection != WrappedDirection.UP && cachedBrewingStand.getBrewingTime() == 0) {
			ItemStack taken = null;
			if (cachedInv.getItem(0) != null && new ItemData(cachedInv.getItem(0)).checkFilter(filterItems, filteringMode)) {
				taken = cachedInv.getItem(0);
				cachedInv.setItem(0, null);
			} else if (cachedInv.getItem(1) != null && new ItemData(cachedInv.getItem(1)).checkFilter(filterItems, filteringMode)) {
				taken = cachedInv.getItem(1);
				cachedInv.setItem(1, null);
			} else if (cachedInv.getItem(2) != null && new ItemData(cachedInv.getItem(2)).checkFilter(filterItems, filteringMode)) {
				taken = cachedInv.getItem(2);
				cachedInv.setItem(2, null);
			}
			if (taken != null) {
				return taken;
			}
		}

		return null;
	}

	@Override
	public ItemStack insertItem(WrappedDirection insertDirection, ItemStack insertion) {
		if (!cachedChunk.isLoaded()) {
			return insertion;
		}
		if (isInvLocked(cachedBrewingStand)) {
			return insertion;
		}
		if (insertion.getType() == Material.POTION || insertion.getType() == Material.SPLASH_POTION || insertion.getType() == Material.LINGERING_POTION) {
			if (cachedInv.getItem(0) == null) {
				cachedInv.setItem(0, insertion);
				return null;
			} else if (cachedInv.getItem(1) == null) {
				cachedInv.setItem(1, insertion);
				return null;
			} else if (cachedInv.getItem(2) == null) {
				cachedInv.setItem(2, insertion);
				return null;
			}
		} else if (insertDirection.isSide() && insertion.getType() == Material.BLAZE_POWDER) {
			ItemStack oldFuel = cachedInv.getFuel();
			cachedInv.setFuel(putItemInSlot(insertion, oldFuel));
			if (insertion == null || insertion.getAmount() == 0) {
				insertion = null;
			}
		} else if (isBrewingIngredient(insertion)) {
			ItemStack oldIngredient = cachedInv.getIngredient();
			cachedInv.setIngredient(putItemInSlot(insertion, oldIngredient));
			if (insertion == null || insertion.getAmount() == 0) {
				insertion = null;
			}
		}
		return insertion;

	}

	@Override
	public int howMuchSpaceForItemAsync(WrappedDirection insertDirection, ItemStack insertion) {
		if (!cachedChunk.isLoaded()) {
			return 0;
		}
		if (isInvLocked(cachedBrewingStand)) {
			return 0;
		}
		if (insertion.getType() == Material.POTION || insertion.getType() == Material.SPLASH_POTION || insertion.getType() == Material.LINGERING_POTION) {
			if (cachedInv.getItem(0) != null && cachedInv.getItem(1) != null && cachedInv.getItem(2) != null) {
				return 0;
			} else {
				return 1;
			}
		} else if (insertDirection.isSide() && insertion.getType() == Material.BLAZE_POWDER) {
			return howManyItemsFit(insertion, cachedInv.getFuel());
		} else if (isBrewingIngredient(insertion)) {
			return howManyItemsFit(insertion, cachedInv.getIngredient());
		} else {
			return 0;
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

	@Override
	public void updateBlock() {
		this.cachedChunk = block.getChunk();
		this.cachedBrewingStand = ((BrewingStand) block.getState());
		this.cachedInv = cachedBrewingStand.getInventory();
	}

}
