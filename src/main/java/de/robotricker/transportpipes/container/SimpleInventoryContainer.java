package de.robotricker.transportpipes.container;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

import de.robotricker.transportpipes.duct.pipe.filter.ItemFilter;
import de.robotricker.transportpipes.location.TPDirection;

public class SimpleInventoryContainer extends BlockContainer {

    private Chunk chunk;
    private InventoryHolder cachedInvHolder;
    private Inventory cachedInv;

    public SimpleInventoryContainer(Block block) {
        super(block);
        this.chunk = block.getChunk();
        this.cachedInvHolder = (InventoryHolder) block.getState();
        this.cachedInv = cachedInvHolder.getInventory();
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
        if (isInvLocked(cachedInvHolder)) {
            return null;
        }
        ItemStack itemTaken = null;
        for (int i = 0; i < cachedInv.getSize(); i++) {
            if (itemFilter.applyFilter(cachedInv.getItem(i)) > 0) {
                int amountBefore = itemTaken != null ? itemTaken.getAmount() : 0;
                if (itemTaken == null) {
                    itemTaken = cachedInv.getItem(i).clone();
                    itemTaken.setAmount(Math.min(amount, itemTaken.getAmount()));
                } else if (itemTaken.isSimilar(cachedInv.getItem(i))) {
                    itemTaken.setAmount(Math.min(amount, amountBefore + cachedInv.getItem(i).getAmount()));
                }
                ItemStack invItem = cachedInv.getItem(i);
                invItem.setAmount(invItem.getAmount() - (itemTaken.getAmount() - amountBefore));
                cachedInv.setItem(i, invItem.getAmount() <= 0 ? null : invItem);
            }
        }
        if (itemTaken != null) {
            //block.getState().update();
        }
        return itemTaken;
    }

    @Override
    public ItemStack insertItem(TPDirection insertDirection, ItemStack insertion) {
        if (!isInLoadedChunk()) {
            return insertion;
        }
        if (isInvLocked(cachedInvHolder)) {
            return insertion;
        }
        Collection<ItemStack> overflow = cachedInv.addItem(insertion).values();
        //block.getState().update();
        if (overflow.isEmpty()) {
            return null;
        } else {
            return overflow.stream().findFirst().get();
        }
    }

    @Override
    public int spaceForItem(TPDirection insertDirection, ItemStack insertion) {
        if (isInvLocked(cachedInvHolder)) {
            return 0;
        }

        int space = 0;

        for (int i = 0; i < cachedInv.getSize(); i++) {
            ItemStack item = cachedInv.getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                space += insertion.getMaxStackSize();
            } else if (item.isSimilar(insertion) && item.getAmount() < item.getMaxStackSize()) {
                space += item.getMaxStackSize() - item.getAmount();
            }
        }

        return space;
    }

    @Override
    public void updateBlock() {
        this.cachedInvHolder = ((InventoryHolder) block.getState()).getInventory().getHolder();
        this.cachedInv = cachedInvHolder.getInventory();
    }

}
