package de.robotricker.transportpipes.duct.pipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.StringTag;
import com.flowpowered.nbt.Tag;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.duct.ClickableDuct;
import de.robotricker.transportpipes.duct.DuctInv;
import de.robotricker.transportpipes.duct.InventoryDuct;
import de.robotricker.transportpipes.duct.pipe.ExtractionPipe.ExtractAmount;
import de.robotricker.transportpipes.duct.pipe.ExtractionPipe.ExtractCondition;
import de.robotricker.transportpipes.duct.pipe.craftingpipe.CraftingPipeProcessInv;
import de.robotricker.transportpipes.duct.pipe.craftingpipe.CraftingPipeRecipeInv;
import de.robotricker.transportpipes.duct.pipe.utils.FilteringMode;
import de.robotricker.transportpipes.duct.pipe.utils.PipeType;
import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.ductdetails.DuctDetails;
import de.robotricker.transportpipes.utils.ductdetails.PipeDetails;
import de.robotricker.transportpipes.utils.staticutils.DuctItemUtils;
import de.robotricker.transportpipes.utils.staticutils.InventoryUtils;
import de.robotricker.transportpipes.utils.staticutils.NBTUtils;
import de.robotricker.transportpipes.utils.tick.TickData;

public class CraftingPipe extends Pipe implements ClickableDuct, InventoryDuct {

	// ignoring amount
	private ItemData[] recipeItems;
	private ItemStack recipeResult;
	private ItemStack[] processItems;
	private ItemStack resultCache;

	private CraftingPipeRecipeInv recipeInventory;
	private CraftingPipeProcessInv processInventory;

	private WrappedDirection outputDirection;
	// a list of PipeItems that were just crafted
	// every PipeItem in this list should be ignored on "handleArrivalAtMiddle"
	private List<PipeItem> pipeItemsJustCrafted;

	public CraftingPipe(Location blockLoc) {
		super(blockLoc);
		this.recipeItems = new ItemData[9];
		this.processItems = new ItemStack[9];

		this.recipeInventory = new CraftingPipeRecipeInv(this);
		this.processInventory = new CraftingPipeProcessInv(this);

		this.outputDirection = null;
		this.pipeItemsJustCrafted = new ArrayList<>();
	}

	public WrappedDirection getOutputDirection() {
		return outputDirection;
	}

	public void setOutputDirection(WrappedDirection outputDirection) {
		this.outputDirection = outputDirection;
	}

	/**
	 * checks if the current outputdirection is valid and updates it to a valid
	 * value if necessary
	 * 
	 * @param cycle
	 *            whether the direction should really cycle or just be checked for
	 *            validity
	 */
	public void checkAndUpdateOutputDirection(boolean cycle) {
		Collection<WrappedDirection> connections = getAllConnections();
		if (connections.isEmpty()) {
			outputDirection = null;
		} else if (cycle || outputDirection == null || !connections.contains(outputDirection)) {
			do {
				if (outputDirection == null) {
					outputDirection = WrappedDirection.NORTH;
				} else {
					outputDirection = outputDirection.getNextDirection();
				}
			} while (!connections.contains(outputDirection));
		}
	}

	/**
	 * @return overflow
	 */
	public ItemStack addProcessItem(ItemStack item) {
		for (int i = 0; i < processItems.length; i++) {
			ItemStack processItemBefore = processItems[i];
			if (processItemBefore == null) {
				processItems[i] = item;
				updateProcessInv();
				return null;
			} else {
				if (!processItemBefore.isSimilar(item)) {
					continue;
				}
				int amountBefore = processItemBefore.getAmount();
				int amountItem = item.getAmount();
				int amountMax = item.getMaxStackSize();
				int delta = Math.min(amountMax, amountBefore + amountItem) - amountBefore;
				processItems[i].setAmount(amountBefore + delta);
				updateProcessInv();
				if (delta < amountItem) {
					item.setAmount(amountItem - delta);
				} else {
					return null;
				}
			}
		}
		return item;
	}

	private void removeProcessItems(Map<ItemData, Integer> removedItems) {
		for (int i = 0; i < 9; i++) {
			ItemData slotId = processItems[i] == null ? null : new ItemData(processItems[i]);
			int slotCount = processItems[i] == null ? 0 : processItems[i].getAmount();
			if (removedItems.containsKey(slotId)) {
				int removeCount = removedItems.get(slotId);
				if (slotCount - removeCount > 0) {
					processItems[i].setAmount(slotCount - removeCount);
					removedItems.remove(slotId);
				} else if (slotCount - removeCount == 0) {
					processItems[i] = null;
					removedItems.remove(slotId);
				} else if (slotCount - removeCount < 0) {
					processItems[i] = null;
					removedItems.put(slotId, removeCount - slotCount);
				}
			}
		}
		updateProcessInv();
	}

	private void updateProcessInv() {
		for (int i = 0; i < processItems.length; i++) {
			if (processItems[i] != null) {
				processInventory.getSharedInventory().setItem(9 + i, processItems[i]);
			} else {
				processInventory.getSharedInventory().setItem(9 + i, null);
			}
		}
	}

	@Override
	public void tick(TickData tickData) {
		super.tick(tickData);

		Map<ItemData, Integer> neededItems = new HashMap<>();
		for (int i = 0; i < 9; i++) {
			ItemData id = recipeItems[i];
			if (id != null) {
				int count = neededItems.get(id) == null ? 0 : neededItems.get(id);
				neededItems.put(id, count + 1);
			}
		}
		Map<ItemData, Integer> givenItems = new HashMap<>();
		for (int i = 0; i < 9; i++) {
			ItemData id = processItems[i] == null ? null : new ItemData(processItems[i]);
			if (id != null) {
				int count = givenItems.get(id) == null ? 0 : givenItems.get(id);
				givenItems.put(id, count + processItems[i].getAmount());
			}
		}

		// only craft if there is space for the result and a valid recipe is given
		boolean craft = resultCache == null && recipeResult != null;
		for (ItemData neededId : neededItems.keySet()) {
			if (givenItems.containsKey(neededId)) {
				int neededCount = neededItems.get(neededId);
				int givenCount = givenItems.get(neededId);
				if (givenCount < neededCount) {
					craft = false;
					break;
				}
			} else {
				craft = false;
				break;
			}
		}
		if (craft) {
			resultCache = recipeResult.clone();
			removeProcessItems(neededItems);
		}

		if (resultCache != null && outputDirection != null) {
			PipeItem pipeItem = new PipeItem(resultCache.clone(), getBlockLoc(), outputDirection);
			// let the item spawn in the center of the pipe
			pipeItem.relLoc().set(0.5f, 0.5f, 0.5f);
			tempPipeItemsWithSpawn.put(pipeItem, outputDirection);
			pipeItemsJustCrafted.add(pipeItem);
			resultCache = null;
		}
	}

	public ItemData[] getRecipeItems() {
		return recipeItems;
	}

	public ItemStack getRecipeResult() {
		return recipeResult;
	}

	public void setRecipeResult(ItemStack result) {
		this.recipeResult = result;
	}

	@Override
	public Map<WrappedDirection, Integer> handleArrivalAtMiddle(final PipeItem item, WrappedDirection before, Collection<WrappedDirection> possibleDirs) {
		//let just crafted item out
		if(recipeResult != null && item.getItem().isSimilar(recipeResult)) {
			Map<WrappedDirection, Integer> outputMap = new HashMap<>();
			outputMap.put(outputDirection, item.getItem().getAmount());
			return outputMap;
		}
		final ItemStack overflow = addProcessItem(item.getItem());
		if (overflow != null) {
			TransportPipes.runTask(new Runnable() {

				@Override
				public void run() {
					item.getBlockLoc().getWorld().dropItem(item.getBlockLoc().clone().add(0.5, 0.5, 0.5), overflow);
				}
			});
		}
		return null;
	}

	@Override
	public int[] getBreakParticleData() {
		return new int[] { 58, 0 };
	}

	@Override
	public void click(Player p, WrappedDirection side) {
		getDuctInventory(p).openOrUpdateInventory(p);
	}

	@Override
	public DuctInv getDuctInventory(Player p) {
		if (p.isSneaking()) {
			return recipeInventory;
		} else {
			return processInventory;
		}
	}

	@Override
	public PipeType getPipeType() {
		return PipeType.CRAFTING;
	}

	@Override
	public List<ItemStack> getDroppedItems() {
		List<ItemStack> is = new ArrayList<>();
		is.add(DuctItemUtils.getClonedDuctItem(new PipeDetails(getPipeType())));
		for (int i = 0; i < 9; i++) {
			ItemData recipeItem = recipeItems[i];
			if (recipeItem != null) {
				is.add(recipeItem.toItemStack());
			}
		}
		for (int i = 0; i < 9; i++) {
			ItemStack processItem = processItems[i];
			if (processItem != null) {
				is.add(processItem.clone());
			}
		}
		return is;
	}

	@Override
	public DuctDetails getDuctDetails() {
		return new PipeDetails(getPipeType());
	}

	@Override
	public void notifyConnectionsChange() {
		super.notifyConnectionsChange();
		checkAndUpdateOutputDirection(false);
	}

	@Override
	public void saveToNBTTag(CompoundMap tags) {
		super.saveToNBTTag(tags);
		NBTUtils.saveIntValue(tags, "OutputDirection", outputDirection == null ? -1 : outputDirection.getId());

		List<Tag<?>> processItemsList = new ArrayList<>();
		for (int i = 0; i < processItems.length; i++) {
			ItemStack is = processItems[i];
			processItemsList.add(InventoryUtils.toNBTTag(is));
		}
		NBTUtils.saveListValue(tags, "ProcessItems", CompoundTag.class, processItemsList);

		List<Tag<?>> recipeItemsList = new ArrayList<>();
		for (int i = 0; i < recipeItems.length; i++) {
			ItemData id = recipeItems[i];
			if (id != null) {
				recipeItemsList.add(id.toNBTTag());
			} else {
				recipeItemsList.add(InventoryUtils.createNullItemNBTTag());
			}
		}
		NBTUtils.saveListValue(tags, "RecipeItems", CompoundTag.class, recipeItemsList);
		NBTUtils.saveStringValue(tags, "RecipeItemResult", InventoryUtils.ItemStackToString(recipeResult));

	}

	@Override
	public void loadFromNBTTag(CompoundTag tag, long datFileVersion) {
		super.loadFromNBTTag(tag, datFileVersion);

		int outputDirectionId = NBTUtils.readIntTag(tag.getValue().get("OutputDirection"), -1);
		if (outputDirectionId == -1) {
			setOutputDirection(null);
		} else {
			setOutputDirection(WrappedDirection.fromID(outputDirectionId));
		}

		List<Tag<?>> processItemsList = NBTUtils.readListTag(tag.getValue().get("ProcessItems"));
		int i = 0;
		for (Tag<?> itemTag : processItemsList) {
			if (processItemsList.size() > i) {
				processItems[i] = InventoryUtils.fromNBTTag((CompoundTag) itemTag);
			}
			i++;
		}
		
		List<Tag<?>> recipeItemsList = NBTUtils.readListTag(tag.getValue().get("RecipeItems"));
		i = 0;
		for (Tag<?> itemTag : recipeItemsList) {
			if (recipeItemsList.size() > i) {
				recipeItems[i] = ItemData.fromNBTTag((CompoundTag) itemTag);
			}
			i++;
		}
		recipeResult = InventoryUtils.StringToItemStack(NBTUtils.readStringTag(tag.getValue().get("RecipeItemResult"), null));
		
		updateProcessInv();
	}

}
