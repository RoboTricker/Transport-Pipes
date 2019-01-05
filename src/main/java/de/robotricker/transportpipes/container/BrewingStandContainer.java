package de.robotricker.transportpipes.container;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.location.TPDirection;

public class BrewingStandContainer extends BlockContainer {

    private Chunk chunk;
    private BrewingStand cachedBrewingStand;
    private BrewerInventory cachedInv;

    public BrewingStandContainer(Block block) {
        super(block);
        this.chunk = block.getChunk();
        this.cachedBrewingStand = (BrewingStand) block.getState();
        this.cachedInv = cachedBrewingStand.getInventory();
    }

    @Override
    public boolean isInLoadedChunk() {
        return chunk.isLoaded();
    }

    @Override
    public ItemStack extractItem(TPDirection extractDirection, int amount) {
        if (!isInLoadedChunk()) {
            return null;
        }
        if (extractDirection != TPDirection.DOWN && cachedBrewingStand.getBrewingTime() == 0) {
            ItemStack takeItem = null;
            if (cachedInv.getItem(0) != null) {
                takeItem = cachedInv.getItem(0);
                cachedInv.setItem(0, null);
            } else if (cachedInv.getItem(1) != null) {
                takeItem = cachedInv.getItem(1);
                cachedInv.setItem(1, null);
            } else if (cachedInv.getItem(2) != null) {
                takeItem = cachedInv.getItem(2);
                cachedInv.setItem(2, null);
            }
            if (takeItem != null) {
                return takeItem;
            }
        }
        return null;
    }

    @Override
    public ItemStack insertItem(TPDirection insertDirection, ItemStack insertion) {
        if (!isInLoadedChunk()) {
            return null;
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
            cachedInv.setFuel(accumulateItems(oldFuel, insertion));
            if (insertion == null || insertion.getAmount() == 0) {
                insertion = null;
            }
        } else if (isBrewingIngredient(insertion)) {
            ItemStack oldIngredient = cachedInv.getIngredient();
            cachedInv.setIngredient(accumulateItems(oldIngredient, insertion));
            if (insertion == null || insertion.getAmount() == 0) {
                insertion = null;
            }
        }
        return insertion;
    }

    @Override
    public int spaceForItem(TPDirection insertDirection, ItemStack insertion) {
        if (!isInLoadedChunk()) {
            return 0;
        }
        if (insertion.getType() == Material.POTION || insertion.getType() == Material.SPLASH_POTION || insertion.getType() == Material.LINGERING_POTION) {
            if (cachedInv.getItem(0) != null && cachedInv.getItem(1) != null && cachedInv.getItem(2) != null) {
                return 0;
            } else {
                return 1;
            }
        } else if (insertDirection.isSide() && insertion.getType() == Material.BLAZE_POWDER) {
            return spaceForItem(cachedInv.getFuel(), insertion);
        } else if (isBrewingIngredient(insertion)) {
            return spaceForItem(cachedInv.getIngredient(), insertion);
        } else {
            return 0;
        }
    }

    @Override
    public void updateBlock() {
        this.cachedBrewingStand = ((BrewingStand) block.getState());
        this.cachedInv = cachedBrewingStand.getInventory();
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
