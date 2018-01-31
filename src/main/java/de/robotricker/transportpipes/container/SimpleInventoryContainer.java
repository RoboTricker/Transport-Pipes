package de.robotricker.transportpipes.container;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.duct.pipe.utils.FilteringMode;
import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.staticutils.InventoryUtils;
import io.sentry.Sentry;

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
	public ItemStack extractItem(WrappedDirection extractDirection, int extractAmount, List<ItemData> filterItems, FilteringMode filteringMode) {
		ItemStack takenIs;
		try {
			if (!cachedChunk.isLoaded()) {
				return null;
			}
			if (isInvLocked(cachedInvHolder)) {
				return null;
			}
			takenIs = null;
			for (int i = 0; i < cachedInv.getSize(); i++) {
				if (cachedInv.getItem(i) != null) {
					int amountBefore = takenIs != null ? takenIs.getAmount() : 0;
					if (takenIs == null) {
						if (new ItemData(cachedInv.getItem(i)).applyFilter(filterItems, filteringMode) > 0) {
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
		} catch (Exception exception) {
			exception.printStackTrace();
			Sentry.capture(exception);
		}
		return null;
	}

	@Override
	public ItemStack insertItem(WrappedDirection insertDirection, ItemStack insertion) {
		try {
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
		} catch (Exception exception) {
			exception.printStackTrace();
			Sentry.capture(exception);
		}
		return insertion;
	}

	@Override
	public int howMuchSpaceForItemAsync(WrappedDirection insertDirection, ItemStack insertion) {
		try {
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
		} catch (Exception exception) {
			exception.printStackTrace();
			Sentry.capture(exception);
		}
		return 0;
	}

	@Override
	public void updateBlock() {
		try {
			this.cachedChunk = block.getChunk();
			this.cachedInvHolder = ((InventoryHolder) block.getState());
			this.cachedInv = cachedInvHolder.getInventory();
			updateOtherDoubleChestBlocks();
		} catch (Exception exception) {
			exception.printStackTrace();
			Sentry.capture(exception);
		}
	}

	private void updateOtherDoubleChestBlocks() {
		try {
			if (cachedInv.getHolder() instanceof DoubleChest) {
				Material chestMaterial = block.getType();
				Location otherChestLoc = null;
				for (WrappedDirection pd : WrappedDirection.values()) {
					if (pd.isSide()) {
						if (block.getRelative(pd.getX(), pd.getY(), pd.getZ()).getType() == chestMaterial) {
							otherChestLoc = block.getRelative(pd.getX(), pd.getY(), pd.getZ()).getLocation();
						}
					}
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
		} catch (Exception exception) {
			exception.printStackTrace();
			Sentry.capture(exception);
		}
	}

}
