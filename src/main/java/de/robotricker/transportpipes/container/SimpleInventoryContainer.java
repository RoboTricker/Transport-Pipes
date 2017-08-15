package de.robotricker.transportpipes.container;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.pipes.BlockLoc;
import de.robotricker.transportpipes.pipes.FilteringMode;
import de.robotricker.transportpipes.pipes.PipeDirection;
import de.robotricker.transportpipes.pipeutils.InventoryUtils;

public class SimpleInventoryContainer extends BlockContainer {

	private Chunk cachedChunk;
	private InventoryHolder cachedInvHolder;
	private Inventory cachedInv;

	public SimpleInventoryContainer(Block block) {
		super(block);
		this.cachedChunk = block.getChunk();
		this.cachedInvHolder = (InventoryHolder) block.getState();
		this.cachedInv = cachedInvHolder.getInventory();
		updateOtherDoubleChestBlocks();
	}

	@Override
	public ItemStack extractItem(PipeDirection extractDirection, int extractAmount, List<ItemData> filterItems, FilteringMode filteringMode) {
		if (!cachedChunk.isLoaded()) {
			return null;
		}
		if (isInvLocked(cachedInvHolder)) {
			return null;
		}
		ItemStack takenIs = null;
		for (int i = 0; i < cachedInv.getSize(); i++) {
			if (cachedInv.getItem(i) != null) {
				int amountBefore = takenIs != null ? takenIs.getAmount() : 0;
				if (takenIs == null) {
					if (new ItemData(cachedInv.getItem(i)).checkFilter(filterItems, filteringMode)) {
						takenIs = cachedInv.getItem(i).clone();
						takenIs.setAmount(Math.min(extractAmount, takenIs.getAmount()));
					} else {
						continue;
					}
				} else if (takenIs.isSimilar(cachedInv.getItem(i))) {
					takenIs.setAmount(Math.min(extractAmount, amountBefore + cachedInv.getItem(i).getAmount()));
				}
				ItemStack invItem = cachedInv.getItem(i);
				cachedInv.setItem(i, InventoryUtils.changeAmount(invItem, -(takenIs.getAmount() - amountBefore)));
			}
		}
		if (takenIs != null) {
			block.getState().update();
		}
		return takenIs;
	}

	@Override
	public ItemStack insertItem(PipeDirection insertDirection, ItemStack insertion) {
		if (!cachedChunk.isLoaded()) {
			return insertion;
		}
		if (isInvLocked(cachedInvHolder)) {
			return insertion;
		}
		Collection<ItemStack> overflow = cachedInv.addItem(insertion).values();
		block.getState().update();
		if (overflow.isEmpty()) {
			return null;
		} else {
			return overflow.toArray(new ItemStack[0])[0];
		}
	}

	@Override
	public int howMuchSpaceForItemAsync(PipeDirection insertDirection, ItemStack insertion) {
		if (!cachedChunk.isLoaded()) {
			return 0;
		}
		if (isInvLocked(cachedInvHolder)) {
			return 0;
		}
		int freeSpace = 0;
		for (int i = 0; i < cachedInv.getSize(); i++) {
			ItemStack is = cachedInv.getItem(i);
			if (is == null || is.getType() == Material.AIR) {
				freeSpace += insertion.getMaxStackSize();
			} else if (is.isSimilar(insertion) && is.getAmount() < is.getMaxStackSize()) {
				freeSpace += is.getMaxStackSize() - is.getAmount();
			}
		}
		return freeSpace;
	}

	@Override
	public void updateBlock() {
		this.cachedChunk = block.getChunk();
		this.cachedInvHolder = ((InventoryHolder) block.getState());
		this.cachedInv = cachedInvHolder.getInventory();
		updateOtherDoubleChestBlocks();
	}

	private void updateOtherDoubleChestBlocks() {
		if (cachedInv.getHolder() instanceof DoubleChest) {
			DoubleChest dc = (DoubleChest) cachedInv.getHolder();
			Location otherChestLoc = null;
			Location leftChestLoc = ((Chest) dc.getLeftSide()).getLocation();
			Location rightChestLoc = ((Chest) dc.getRightSide()).getLocation();
			if (leftChestLoc.getBlockX() == block.getX() && leftChestLoc.getBlockY() == block.getY() && leftChestLoc.getBlockZ() == block.getZ()) {
				otherChestLoc = rightChestLoc;
			} else {
				otherChestLoc = leftChestLoc;
			}
			Map<BlockLoc, TransportPipesContainer> containerMap = TransportPipes.instance.getContainerMap(block.getWorld());

			if (containerMap != null) {
				BlockLoc bl = BlockLoc.convertBlockLoc(otherChestLoc);
				if (containerMap.containsKey(bl)) {
					TransportPipesContainer tpc = containerMap.get(bl);
					if (tpc instanceof SimpleInventoryContainer) {
						SimpleInventoryContainer sic = (SimpleInventoryContainer) tpc;
						if (!(sic.cachedInv instanceof DoubleChestInventory)) {
							sic.updateBlock();
						}
					}
				}
			}
		}
	}

}
