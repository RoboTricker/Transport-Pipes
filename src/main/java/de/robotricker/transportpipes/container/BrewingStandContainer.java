package de.robotricker.transportpipes.container;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.pipes.PipeDirection;

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
	public ItemData extractItem(PipeDirection extractDirection) {
		if (!cachedChunk.isLoaded()) {
			return null;
		}
		if (isInvLocked(cachedBrewingStand)) {
			return null;
		}
		if (extractDirection != PipeDirection.UP && cachedBrewingStand.getBrewingTime() == 0) {
			ItemStack taken = null;
			if (cachedInv.getItem(0) != null) {
				taken = cachedInv.getItem(0);
				cachedInv.setItem(0, null);
			} else if (cachedInv.getItem(1) != null) {
				taken = cachedInv.getItem(1);
				cachedInv.setItem(1, null);
			} else if (cachedInv.getItem(2) != null) {
				taken = cachedInv.getItem(2);
				cachedInv.setItem(2, null);
			}
			if (taken != null) {
				return new ItemData(taken);
			}
		}

		return null;
	}

	@Override
	public boolean insertItem(PipeDirection insertDirection, ItemData insertion) {
		if (!cachedChunk.isLoaded()) {
			return false;
		}
		if (isInvLocked(cachedBrewingStand)) {
			return false;
		}
		ItemStack itemStack = insertion.toItemStack();
		if (itemStack.getType() == Material.POTION || itemStack.getType() == Material.SPLASH_POTION || itemStack.getType() == Material.LINGERING_POTION) {
			if (cachedInv.getItem(0) == null) {
				cachedInv.setItem(0, itemStack);
				return true;
			} else if (cachedInv.getItem(1) == null) {
				cachedInv.setItem(1, itemStack);
				return true;
			} else if (cachedInv.getItem(2) == null) {
				cachedInv.setItem(2, itemStack);
				return true;
			} else {
				return false;
			}
		} else if (insertDirection.isSide() && itemStack.getType() == Material.BLAZE_POWDER) {
			ItemStack oldFuel = cachedInv.getFuel();
			if (canPutItemInSlot(insertion, oldFuel)) {
				cachedInv.setFuel(putItemInSlot(insertion, oldFuel));
				return true;
			} else {
				return false;
			}
		} else if (isBrewingIngredient(itemStack)) {
			ItemStack oldIngredient = cachedInv.getIngredient();
			if (canPutItemInSlot(insertion, oldIngredient)) {
				cachedInv.setIngredient(putItemInSlot(insertion, oldIngredient));
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
		if (isInvLocked(cachedBrewingStand)) {
			return false;
		}
		ItemStack itemStack = insertion.toItemStack();
		if (itemStack.getType() == Material.POTION || itemStack.getType() == Material.SPLASH_POTION || itemStack.getType() == Material.LINGERING_POTION) {
			if (cachedInv.getItem(0) != null && cachedInv.getItem(1) != null && cachedInv.getItem(2) != null) {
				return false;
			} else {
				return true;
			}
		} else if (insertDirection.isSide() && itemStack.getType() == Material.BLAZE_POWDER) {
			return canPutItemInSlot(insertion, cachedInv.getFuel());
		} else if (isBrewingIngredient(itemStack)) {
			return canPutItemInSlot(insertion, cachedInv.getIngredient());
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

	@Override
	public void updateBlock() {
		this.cachedChunk = block.getChunk();
		this.cachedBrewingStand = ((BrewingStand) block.getState());
		this.cachedInv = cachedBrewingStand.getInventory();
	}

}
