package de.robotricker.transportpipes.duct.pipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.Tag;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.duct.ClickableDuct;
import de.robotricker.transportpipes.duct.DuctSharedInv;
import de.robotricker.transportpipes.duct.InventoryDuct;
import de.robotricker.transportpipes.duct.pipe.extractionpipe.ExtractionPipeInv;
import de.robotricker.transportpipes.duct.pipe.utils.FilteringMode;
import de.robotricker.transportpipes.duct.pipe.utils.PipeType;
import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.config.LocConf;
import de.robotricker.transportpipes.utils.ductdetails.DuctDetails;
import de.robotricker.transportpipes.utils.ductdetails.PipeDetails;
import de.robotricker.transportpipes.utils.hitbox.TimingCloseable;
import de.robotricker.transportpipes.utils.staticutils.DuctItemUtils;
import de.robotricker.transportpipes.utils.staticutils.InventoryUtils;
import de.robotricker.transportpipes.utils.staticutils.NBTUtils;
import io.sentry.Sentry;

public class ExtractionPipe extends Pipe implements ClickableDuct, InventoryDuct {

	private int lastOutputIndex = 0;

	private WrappedDirection extractDirection;
	private ExtractCondition extractCondition;
	private ExtractAmount extractAmount;
	private FilteringMode filteringMode;
	private ItemData[] filteringItems;

	private ExtractionPipeInv inventory;

	public ExtractionPipe(Location blockLoc) {
		super(blockLoc);
		extractDirection = null;
		extractCondition = ExtractCondition.NEEDS_REDSTONE;
		extractAmount = ExtractAmount.EXTRACT_1;
		filteringMode = FilteringMode.FILTERBY_TYPE_DAMAGE_NBT;
		filteringItems = new ItemData[GoldenPipe.ITEMS_PER_ROW];

		this.inventory = new ExtractionPipeInv(this);
	}

	@Override
	public Map<WrappedDirection, Integer> handleArrivalAtMiddle(PipeItem item, WrappedDirection before, Collection<WrappedDirection> possibleDirs) {
		Map<BlockLoc, TransportPipesContainer> containerMap = TransportPipes.instance.getContainerMap(getBlockLoc().getWorld());

		Map<WrappedDirection, Integer> maxSpaceMap = new HashMap<WrappedDirection, Integer>();
		Map<WrappedDirection, Integer> map = new HashMap<WrappedDirection, Integer>();

		// update maxSpaceMap
		for (WrappedDirection pd : WrappedDirection.values()) {
			maxSpaceMap.put(pd, Integer.MAX_VALUE);
			if (containerMap != null) {
				BlockLoc bl = BlockLoc.convertBlockLoc(getBlockLoc().clone().add(pd.getX(), pd.getY(), pd.getZ()));
				if (containerMap.containsKey(bl)) {
					TransportPipesContainer tpc = containerMap.get(bl);
					int freeSpace = tpc.howMuchSpaceForItemAsync(pd.getOpposite(), item.getItem());
					maxSpaceMap.put(pd, freeSpace);
				}
			}
		}

		for (int i = 0; i < item.getItem().getAmount(); i++) {
			WrappedDirection nextDir = getNextItemDirection(item, before, new ArrayList<>(possibleDirs), map, maxSpaceMap);
			if (nextDir != null) {
				if (map.containsKey(nextDir)) {
					map.put(nextDir, map.get(nextDir) + 1);
				} else {
					map.put(nextDir, 1);
				}
			}
		}

		return map;
	}

	private WrappedDirection getNextItemDirection(PipeItem item, WrappedDirection before, Collection<WrappedDirection> possibleDirs, Map<WrappedDirection, Integer> outputMap, Map<WrappedDirection, Integer> maxSpaceMap) {

		Iterator<WrappedDirection> it = possibleDirs.iterator();
		while (it.hasNext()) {
			WrappedDirection pd = it.next();
			if (pd.equals(before.getOpposite())) {
				it.remove();
			} else {
				int currentAmount = outputMap.containsKey(pd) ? outputMap.get(pd) : 0;
				currentAmount += getSimilarItemAmountOnDirectionWay(item, pd);
				int maxFreeSpace = maxSpaceMap.get(pd);
				if (currentAmount >= maxFreeSpace) {
					it.remove();
				}
			}
		}
		WrappedDirection[] array = possibleDirs.toArray(new WrappedDirection[0]);
		lastOutputIndex++;
		if (lastOutputIndex >= possibleDirs.size()) {
			lastOutputIndex = 0;
		}
		if (possibleDirs.size() > 0) {
			return array[lastOutputIndex];
		}
		return null;
	}

	@Override
	public void saveToNBTTag(CompoundMap tags) {
		super.saveToNBTTag(tags);
		NBTUtils.saveIntValue(tags, "ExtractDirection", extractDirection == null ? -1 : extractDirection.getId());
		NBTUtils.saveIntValue(tags, "ExtractCondition", extractCondition.getId());
		NBTUtils.saveIntValue(tags, "ExtractAmount", extractAmount.getId());

		List<Tag<?>> lineList = new ArrayList<>();
		for (int i = 0; i < filteringItems.length; i++) {
			ItemData itemData = filteringItems[i];
			if (itemData != null) {
				lineList.add(itemData.toNBTTag());
			} else {
				lineList.add(InventoryUtils.createNullItemNBTTag());
			}
		}
		NBTUtils.saveListValue(tags, "Items", CompoundTag.class, lineList);
		NBTUtils.saveIntValue(tags, "FilteringMode", filteringMode.getId());

	}

	@Override
	public void loadFromNBTTag(CompoundTag tag, long datFileVersion) {
		super.loadFromNBTTag(tag, datFileVersion);

		int extractDirectionId = NBTUtils.readIntTag(tag.getValue().get("ExtractDirection"), -1);
		if (extractDirectionId == -1) {
			setExtractDirection(null);
		} else {
			setExtractDirection(WrappedDirection.fromID(extractDirectionId));
		}
		setExtractCondition(ExtractCondition.fromId(NBTUtils.readIntTag(tag.getValue().get("ExtractCondition"), ExtractCondition.NEEDS_REDSTONE.getId())));
		setExtractAmount(ExtractAmount.fromId(NBTUtils.readIntTag(tag.getValue().get("ExtractAmount"), ExtractAmount.EXTRACT_1.getId())));

		List<Tag<?>> itemsList = NBTUtils.readListTag(tag.getValue().get("Items"));
		int i = 0;
		for (Tag<?> itemTag : itemsList) {
			if (itemsList.size() > i) {
				ItemData itemData = ItemData.fromNBTTag((CompoundTag) itemTag);
				filteringItems[i] = itemData;
			}
			i++;
		}
		setFilteringMode(FilteringMode.fromId(NBTUtils.readIntTag(tag.getValue().get("FilteringMode"), FilteringMode.FILTERBY_TYPE_DAMAGE_NBT.getId())));
	}

	@Override
	public void click(Player p, WrappedDirection side) {
		getDuctInventory(p).openOrUpdateInventory(p);
	}

	@Override
	public DuctSharedInv getDuctInventory(Player p) {
		return inventory;
	}

	@Override
	public PipeType getPipeType() {
		return PipeType.EXTRACTION;
	}

	@Override
	public List<ItemStack> getDroppedItems() {
		List<ItemStack> is = new ArrayList<>();
		is.add(DuctItemUtils.getClonedDuctItem(new PipeDetails(getPipeType())));
		return is;
	}

	public WrappedDirection getExtractDirection() {
		return extractDirection;
	}

	public void setExtractDirection(WrappedDirection extractDirection) {
		this.extractDirection = extractDirection;
	}

	public ExtractCondition getExtractCondition() {
		return extractCondition;
	}

	public void setExtractCondition(ExtractCondition extractCondition) {
		this.extractCondition = extractCondition;
	}

	public ExtractAmount getExtractAmount() {
		return extractAmount;
	}

	public void setExtractAmount(ExtractAmount extractAmount) {
		this.extractAmount = extractAmount;
	}

	public FilteringMode getFilteringMode() {
		return filteringMode;
	}

	public void setFilteringMode(FilteringMode filteringMode) {
		this.filteringMode = filteringMode;
	}

	public ItemData[] getFilteringItems() {
		return filteringItems;
	}

	public void changeFilteringItems(ItemData[] items) {
		for (int i = 0; i < filteringItems.length; i++) {
			if (i < items.length) {
				filteringItems[i] = items[i];
			} else {
				filteringItems[i] = null;
			}
		}
	}

	/**
	 * checks if the current extract direction is valid and updates it to a valid
	 * value if necessary
	 * 
	 * @param cycle
	 *            whether the direction should really cycle or just be checked for
	 *            validity
	 */
	public void checkAndUpdateExtractDirection(boolean cycle) {
		WrappedDirection oldExtractDirection = getExtractDirection();

		List<WrappedDirection> blockConnections = getOnlyBlockConnections();
		if (blockConnections.isEmpty()) {
			extractDirection = null;
		} else if (cycle || extractDirection == null || !blockConnections.contains(extractDirection)) {
			do {
				if (extractDirection == null) {
					extractDirection = WrappedDirection.NORTH;
				} else {
					extractDirection = extractDirection.getNextDirection();
				}
			} while (!blockConnections.contains(extractDirection));
		}

		if (oldExtractDirection != extractDirection) {
			TransportPipes.instance.pipeThread.runTask(new Runnable() {

				public void run() {
					TransportPipes.instance.ductManager.updateDuct(ExtractionPipe.this);
				};
			}, 0);
		}
	}

	protected void extractItems(List<WrappedDirection> blockConnections) {

		if (extractDirection == null) {
			return;
		}

		if (blockConnections.contains(extractDirection)) {

			final Location containerLoc = getBlockLoc().clone().add(extractDirection.getX(), extractDirection.getY(), extractDirection.getZ());

			// input items
			Bukkit.getScheduler().runTask(TransportPipes.instance, new Runnable() {

				@Override
				public void run() {
					try (TimingCloseable tc = new TimingCloseable("extract item scheduler")) {
						if (!isInLoadedChunk()) {
							return;
						}
						boolean powered = getExtractCondition() == ExtractCondition.ALWAYS_EXTRACT;
						if (getExtractCondition() == ExtractCondition.NEEDS_REDSTONE) {
							for (WrappedDirection pd : WrappedDirection.values()) {
								Location relativeLoc = ExtractionPipe.this.getBlockLoc().clone().add(pd.getX(), pd.getY(), pd.getZ());

								// don't power this pipe if at least 1 block around this pipe is inside an
								// unloaded chunk
								if (!TransportPipes.instance.blockChangeListener.isInLoadedChunk(relativeLoc)) {
									break;
								}

								Block relative = relativeLoc.getBlock();
								if (relative.getType() != Material.TRAPPED_CHEST && relative.getBlockPower(pd.getOpposite().toBlockFace()) > 0) {
									powered = true;
									break;
								}

							}
						}
						if (powered) {
							BlockLoc bl = BlockLoc.convertBlockLoc(containerLoc);
							TransportPipesContainer tpc = TransportPipes.instance.getContainerMap(getBlockLoc().getWorld()).get(bl);
							WrappedDirection itemDir = extractDirection.getOpposite();
							if (tpc != null) {
								List<ItemData> filterItems = new ArrayList<ItemData>();
								for (ItemData filterItem : filteringItems) {
									if (filterItem != null) {
										filterItems.add(filterItem);
									}
								}
								ItemStack taken = tpc.extractItem(itemDir, getExtractAmount().getAmount(), filterItems, getFilteringMode());
								if (taken != null) {
									// extraction successful
									PipeItem pi = new PipeItem(taken, ExtractionPipe.this.getBlockLoc(), itemDir);
									tempPipeItemsWithSpawn.put(pi, itemDir);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

		}
	}

	@Override
	public int[] getBreakParticleData() {
		return new int[] { 5, 0 };
	}

	@Override
	public void notifyConnectionsChange() {
		super.notifyConnectionsChange();
		checkAndUpdateExtractDirection(false);
	}

	@Override
	public DuctDetails getDuctDetails() {
		return new PipeDetails(getPipeType());
	}

	public enum ExtractCondition {
		NEEDS_REDSTONE(LocConf.EXTRACTIONPIPE_CONDITION_NEEDSREDSTONE, Material.REDSTONE, (short) 0),
		ALWAYS_EXTRACT(LocConf.EXTRACTIONPIPE_CONDITION_ALWAYSEXTRACT, Material.INK_SACK, (short) 10),
		NEVER_EXTRACT(LocConf.EXTRACTIONPIPE_CONDITION_NEVEREXTRACT, Material.BARRIER, (short) 0);

		private String locConfKey;
		private ItemStack displayItem;

		private ExtractCondition(String locConfKey, Material type, short damage) {
			this.locConfKey = locConfKey;
			this.displayItem = new ItemStack(type, 1, damage);
		}

		public String getLocConfKey() {
			return locConfKey;
		}

		public int getId() {
			return this.ordinal();
		}

		public static ExtractCondition fromId(int id) {
			return ExtractCondition.values()[id];
		}

		public ExtractCondition getNextCondition() {
			if (getId() == ExtractCondition.values().length - 1) {
				return fromId(0);
			}
			return fromId(getId() + 1);
		}

		public ItemStack getDisplayItem() {
			return displayItem.clone();
		}

	}

	public enum ExtractAmount {
		EXTRACT_1(LocConf.EXTRACTIONPIPE_AMOUNT_EXTRACT1, 1),
		EXTRACT_16(LocConf.EXTRACTIONPIPE_AMOUNT_EXTRACT16, 16);

		private String locConfKey;
		private ItemStack displayItem;

		private ExtractAmount(String locConfKey, int amount) {
			this.locConfKey = locConfKey;
			this.displayItem = new ItemStack(Material.BRICK, amount);
		}

		public int getAmount() {
			return displayItem.getAmount();
		}

		public String getLocConfKey() {
			return locConfKey;
		}

		public int getId() {
			return this.ordinal();
		}

		public static ExtractAmount fromId(int id) {
			return ExtractAmount.values()[id];
		}

		public ExtractAmount getNextAmount() {
			if (getId() == ExtractAmount.values().length - 1) {
				return fromId(0);
			}
			return fromId(getId() + 1);
		}

		public ItemStack getDisplayItem() {
			return displayItem.clone();
		}
	}

}
