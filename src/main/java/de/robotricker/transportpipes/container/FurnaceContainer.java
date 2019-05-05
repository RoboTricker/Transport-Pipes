package de.robotricker.transportpipes.container;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.duct.pipe.filter.ItemFilter;
import de.robotricker.transportpipes.location.TPDirection;
import de.robotricker.transportpipes.utils.NMSUtils;

public class FurnaceContainer extends BlockContainer {

    private Chunk chunk;
    private Furnace cachedFurnace;
    private FurnaceInventory cachedInv;

    public FurnaceContainer(Block block) {
        super(block);
        this.chunk = block.getChunk();
        this.cachedFurnace = (Furnace) block.getState();
        this.cachedInv = cachedFurnace.getInventory();
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
        if (isInvLocked(cachedFurnace)) {
            return null;
        }
        if (itemFilter.applyFilter(cachedInv.getResult()) > 0) {
            ItemStack resultItem = cachedInv.getResult().clone();
            ItemStack returnItem = resultItem.clone();

            int resultItemAmount = resultItem.getAmount();
            resultItem.setAmount(Math.max(resultItemAmount - amount, 0));
            cachedInv.setResult(resultItem.getAmount() >= 1 ? resultItem : null);

            returnItem.setAmount(Math.min(amount, resultItemAmount));

            return returnItem;
        }
        return null;
    }

    @Override
    public ItemStack insertItem(TPDirection insertDirection, ItemStack insertion) {
        if (!isInLoadedChunk()) {
            return insertion;
        }
        if (isInvLocked(cachedFurnace)) {
            return insertion;
        }
        if (insertDirection == TPDirection.DOWN) {
            if (NMSUtils.isFurnaceBurnableItem(insertion)) {
                ItemStack oldSmelting = cachedInv.getSmelting();
                cachedInv.setSmelting(accumulateItems(oldSmelting, insertion));
                if (insertion == null || insertion.getAmount() == 0) {
                    insertion = null;
                }
            }
        } else if (insertDirection == TPDirection.UP) {
            if (NMSUtils.isFurnaceFuelItem(insertion)) {
                ItemStack oldFuel = cachedInv.getFuel();
                cachedInv.setFuel(accumulateItems(oldFuel, insertion));
                if (insertion == null || insertion.getAmount() == 0) {
                    insertion = null;
                }
            }
        } else {
            if (NMSUtils.isFurnaceBurnableItem(insertion)) {
                ItemStack oldSmelting = cachedInv.getSmelting();
                cachedInv.setSmelting(accumulateItems(oldSmelting, insertion));
                if (insertion == null || insertion.getAmount() == 0) {
                    insertion = null;
                }
            } else if (NMSUtils.isFurnaceFuelItem(insertion)) {
                ItemStack oldFuel = cachedInv.getFuel();
                cachedInv.setFuel(accumulateItems(oldFuel, insertion));
                if (insertion == null || insertion.getAmount() == 0) {
                    insertion = null;
                }
            }
        }

        return insertion;
    }

    @Override
    public int spaceForItem(TPDirection insertDirection, ItemStack insertion) {
        if (isInvLocked(cachedFurnace)) {
            return 0;
        }
        if (NMSUtils.isFurnaceBurnableItem(insertion)) {
            if (insertDirection.isSide() || insertDirection == TPDirection.DOWN) {
                return spaceForItem(cachedInv.getSmelting(), insertion);
            } else if (NMSUtils.isFurnaceFuelItem(insertion)) {
                return spaceForItem(cachedInv.getFuel(), insertion);
            }
        } else if (NMSUtils.isFurnaceFuelItem(insertion)) {
            return spaceForItem(cachedInv.getFuel(), insertion);
        }
        return 0;
    }

    @Override
    public void updateBlock() {
        this.cachedFurnace = ((Furnace) block.getState());
        this.cachedInv = cachedFurnace.getInventory();
    }

}
