package de.robotricker.transportpipes.container;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robotricker.transportpipes.ducts.pipe.items.PipeItem;
import de.robotricker.transportpipes.location.TPDirection;

public abstract class BlockContainer implements TPContainer {

    /**
     * THREAD-SAFE
     * contains all the items that arrived in this container while it was inside an unloaded chunk. As this container gets loaded, it extracts the items from this list one by one and tries to put it into the container block
     */
    final List<PipeItem> unloadedItems;

    protected Block block;

    public BlockContainer(Block block) {
        this.block = block;
        this.unloadedItems = Collections.synchronizedList(new ArrayList<>());
    }

    @Override
    public List<PipeItem> getUnloadedItems() {
        return unloadedItems;
    }

    /**
     * puts "put" into "before" and returns the result. if "put" can't be accumulated with "before", "before" is returned.
     * if "put" does not fit completely in "before", "put"'s amount decreases by how much does fit and the returned item will have an amount of the max stack size
     */
    protected ItemStack accumulateItems(ItemStack before, ItemStack put) {
        if (put == null) {
            return before;
        }
        if (before == null) {
            ItemStack putCopy = put.clone();
            put.setAmount(0);
            return putCopy;
        }
        if (!before.isSimilar(put)) {
            return before;
        }
        int beforeAmount = before.getAmount();
        int putAmount = put.getAmount();
        int maxStackSize = before.getMaxStackSize();
        ItemStack returnItem = before.clone();
        returnItem.setAmount(Math.min(beforeAmount + putAmount, maxStackSize));
        put.setAmount(Math.max(putAmount - (maxStackSize - beforeAmount), 0));
        return returnItem;
    }

    /**
     * does the same as accumulateItems but instead of accumulating them, this method only calculates how much of "put" does fit into "before".
     * The actual amount of "put" is not be taken into account.
     * This method does not change any given ItemStacks
     */
    protected int spaceForItem(ItemStack before, ItemStack put) {
        if (put == null) {
            return 0;
        }
        if (before == null) {
            return put.getMaxStackSize();
        }
        if (!before.isSimilar(put)) {
            return 0;
        }
        if (before.getAmount() >= before.getMaxStackSize()) {
            return 0;
        }
        return before.getMaxStackSize() - before.getAmount();
    }

    public abstract void updateBlock();

}
