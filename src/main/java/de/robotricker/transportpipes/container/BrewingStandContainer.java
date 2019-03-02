package de.robotricker.transportpipes.container;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.duct.pipe.filter.ItemFilter;
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
    public ItemStack extractItem(TPDirection extractDirection, int amount, ItemFilter itemFilter) {
        if (!isInLoadedChunk()) {
            return null;
        }
        if (isInvLocked(cachedBrewingStand)) {
            return null;
        }
        if (extractDirection != TPDirection.DOWN && ((BrewingStand) block.getState()).getBrewingTime() == 0) {
            ItemStack takeItem = null;
            if (itemFilter.applyFilter(cachedInv.getItem(0)) > 0) {
                takeItem = cachedInv.getItem(0);
                cachedInv.setItem(0, null);
            } else if (itemFilter.applyFilter(cachedInv.getItem(1)) > 0) {
                takeItem = cachedInv.getItem(1);
                cachedInv.setItem(1, null);
            } else if (itemFilter.applyFilter(cachedInv.getItem(2)) > 0) {
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
            cachedInv.setFuel(accumulateItems(oldFuel, insertion));
            if (insertion == null || insertion.getAmount() == 0) {
                insertion = null;
            }
        } else if (isBrewingIngredient(insertion.getType())) {
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
            return spaceForItem(cachedInv.getFuel(), insertion);
        } else if (isBrewingIngredient(insertion.getType())) {
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

    private static boolean isBrewingIngredient(Material material) {
        switch(material) {
            case NETHER_WART:
            case REDSTONE:
            case GLOWSTONE_DUST:
            case FERMENTED_SPIDER_EYE:
            case GUNPOWDER:
            case DRAGON_BREATH:
            case GHAST_TEAR:
            case GLISTERING_MELON_SLICE:
            case GOLDEN_CARROT:
            case RABBIT_FOOT:
            case PUFFERFISH:
            case BLAZE_POWDER:
            case MAGMA_CREAM:
            case PHANTOM_MEMBRANE:
            case TURTLE_HELMET:
            case SPIDER_EYE:
            case SUGAR:
                return true;
            default: return false;
        }
    }

}
