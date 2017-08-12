package de.robotricker.transportpipes.pipes.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jnbt.CompoundTag;
import org.jnbt.NBTTagType;
import org.jnbt.Tag;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipes.BlockLoc;
import de.robotricker.transportpipes.pipes.ClickablePipe;
import de.robotricker.transportpipes.pipes.PipeDirection;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipes.PipeUtils;
import de.robotricker.transportpipes.pipes.goldenpipe.GoldenPipeInv;
import de.robotricker.transportpipes.pipeutils.NBTUtils;
import de.robotricker.transportpipes.pipeutils.PipeItemUtils;
import de.robotricker.transportpipes.pipeutils.config.LocConf;

public class GoldenPipe extends Pipe implements ClickablePipe {

	public static final int ITEMS_PER_ROW = 32;

	//1st dimension: output dirs in order of PipeDirection.values() | 2nd dimension: output items in this direction
	private ItemData[][] outputItems = new ItemData[6][ITEMS_PER_ROW];
	private FilteringMode[] filteringModes = new FilteringMode[6];

	public GoldenPipe(Location blockLoc) {
		super(blockLoc);
		for (int i = 0; i < 6; i++) {
			filteringModes[i] = FilteringMode.FILTERBY_TYPE_DAMAGE_NBT;
		}
	}

	@Override
	public Map<PipeDirection, Integer> handleArrivalAtMiddle(PipeItem item, PipeDirection before, Collection<PipeDirection> possibleDirs) {
		Random random = new Random();
		Map<BlockLoc, TransportPipesContainer> containerMap = TransportPipes.instance.getContainerMap(blockLoc.getWorld());

		Map<PipeDirection, Integer> maxSpaceMap = new HashMap<PipeDirection, Integer>();
		Map<PipeDirection, Integer> directionMap = new HashMap<PipeDirection, Integer>();

		//update maxSpaceMap
		for (PipeDirection pd : PipeDirection.values()) {
			maxSpaceMap.put(pd, Integer.MAX_VALUE);
			if (containerMap != null) {
				BlockLoc bl = BlockLoc.convertBlockLoc(blockLoc.clone().add(pd.getX(), pd.getY(), pd.getZ()));
				if (containerMap.containsKey(bl)) {
					TransportPipesContainer tpc = containerMap.get(bl);
					int freeSpace = tpc.howMuchSpaceForItemAsync(pd.getOpposite(), item.getItem());
					maxSpaceMap.put(pd, freeSpace);
				}
			}
		}

		for (int i = 0; i < item.getItem().getAmount(); i++) {
			List<PipeDirection> blockedDirections = new ArrayList<>();
			for (PipeDirection pd : PipeDirection.values()) {
				int currentAmount = directionMap.containsKey(pd) ? directionMap.get(pd) : 0;
				currentAmount += getSimilarItemAmountOnDirectionWay(item, pd);
				int maxFreeSpace = maxSpaceMap.get(pd);
				if (currentAmount >= maxFreeSpace) {
					blockedDirections.add(pd);
				}
			}
			List<PipeDirection> possibleDirsDueToSorting = getPossibleDirectionsForItem(new ItemData(item.getItem()), before, blockedDirections);
			if (possibleDirsDueToSorting.isEmpty()) {
				continue;
			}

			PipeDirection randomDir = possibleDirsDueToSorting.get(random.nextInt(possibleDirsDueToSorting.size()));
			if (directionMap.containsKey(randomDir)) {
				directionMap.put(randomDir, directionMap.get(randomDir) + 1);
			} else {
				directionMap.put(randomDir, 1);
			}
		}

		return directionMap;
	}

	public List<PipeDirection> getPossibleDirectionsForItem(ItemData itemData, PipeDirection before, List<PipeDirection> blockedDirections) {
		//all directions in which is an other pipe or inventory-block
		List<PipeDirection> blockConnections = PipeUtils.getOnlyBlockConnections(this);
		List<PipeDirection> pipeConnections = PipeUtils.getOnlyPipeConnections(this);

		//the possible directions in which the item could go
		List<PipeDirection> possibleDirections = new ArrayList<>();
		List<PipeDirection> emptyPossibleDirections = new ArrayList<>();

		for (int line = 0; line < 6; line++) {
			PipeDirection dir = PipeDirection.fromID(line);
			FilteringMode filteringMode = getFilteringMode(line);
			if (dir.getOpposite() == before) {
				continue;
			}
			//ignore the direction in which is no pipe or inv-block
			if (!blockConnections.contains(dir) && !pipeConnections.contains(dir)) {
				continue;
			} else if (filteringMode == FilteringMode.BLOCK_ALL) {
				continue;
			} else if (blockedDirections.contains(dir)) {
				continue;
			}
			boolean empty = true;
			boolean possibleDueToInvertion = filteringMode == FilteringMode.INVERT;
			for (int i = 0; i < outputItems[line].length; i++) {
				if (outputItems[line][i] != null) {
					empty = false;
				}
				if (filteringMode == FilteringMode.INVERT) {
					possibleDueToInvertion &= outputItems[line][i] == null || !itemData.toItemStack().isSimilar(outputItems[line][i].toItemStack());
				} else if (itemData.equals(outputItems[line][i], filteringMode)) {
					possibleDirections.add(dir);
				}
			}
			if (possibleDueToInvertion) {
				possibleDirections.add(dir);
			} else if (empty) {
				emptyPossibleDirections.add(dir);
			}
		}

		//drop the item if it can't go anywhere
		if (possibleDirections.isEmpty() && emptyPossibleDirections.isEmpty()) {
			return new ArrayList<>();
		}

		//if this item isn't in the list, it will take a random direction from the empty dirs
		if (possibleDirections.isEmpty()) {
			possibleDirections.addAll(emptyPossibleDirections);
		}

		return possibleDirections;
	}

	@Override
	public void saveToNBTTag(HashMap<String, Tag> tags) {
		super.saveToNBTTag(tags);

		List<Tag> linesList = new ArrayList<Tag>();

		for (int line = 0; line < 6; line++) {
			CompoundTag lineCompound = new CompoundTag("Line");

			NBTUtils.saveIntValue(lineCompound.getValue(), "Line", line);

			List<Tag> lineList = new ArrayList<>();
			for (int i = 0; i < outputItems[line].length; i++) {
				ItemData itemData = outputItems[line][i];
				if (itemData != null) {
					lineList.add(itemData.toNBTTag());
				} else {
					lineList.add(ItemData.createNullItemNBTTag());
				}
			}
			NBTUtils.saveListValue(lineCompound.getValue(), "Items", NBTTagType.TAG_COMPOUND, lineList);

			NBTUtils.saveIntValue(lineCompound.getValue(), "FilteringMode", getFilteringMode(line).getId());

			linesList.add(lineCompound);
		}

		NBTUtils.saveListValue(tags, "Lines", NBTTagType.TAG_COMPOUND, linesList);

	}

	@Override
	public void loadFromNBTTag(CompoundTag tag) {
		super.loadFromNBTTag(tag);

		Map<String, Tag> map = tag.getValue();
		if (NBTUtils.readListTag(map.get("Lines")).isEmpty()) {
			//old lines version
			for (int line = 0; line < 6; line++) {

				List<Tag> lineList = NBTUtils.readListTag(map.get("Line" + line));
				for (int i = 0; i < outputItems[line].length; i++) {
					if (lineList.size() > i) {
						ItemData itemData = ItemData.fromNBTTag((CompoundTag) lineList.get(i));
						outputItems[line][i] = itemData;
					}
				}

				FilteringMode fm = FilteringMode.fromId(NBTUtils.readIntTag(map.get("Line" + line + "_filteringMode"), FilteringMode.FILTERBY_TYPE_DAMAGE_NBT.getId()));
				setFilteringMode(line, fm);

			}
		} else {
			//new list version
			List<Tag> linesList = NBTUtils.readListTag(map.get("Lines"));
			for (Tag lineTag : linesList) {
				CompoundTag lineCompound = (CompoundTag) lineTag;
				int line = NBTUtils.readIntTag(lineCompound.getValue().get("Line"), -1);
				if (line != -1) {
					List<Tag> itemsList = NBTUtils.readListTag(lineCompound.getValue().get("Items"));
					int i = 0;
					for (Tag itemTag : itemsList) {
						if (itemsList.size() > i) {
							ItemData itemData = ItemData.fromNBTTag((CompoundTag) itemTag);
							outputItems[line][i] = itemData;
						}
						i++;
					}

					FilteringMode fm = FilteringMode.fromId(NBTUtils.readIntTag(lineCompound.getValue().get("FilteringMode"), FilteringMode.FILTERBY_TYPE_DAMAGE_NBT.getId()));
					setFilteringMode(line, fm);
				}
			}
		}

	}

	@Override
	public int[] getBreakParticleData() {
		return new int[] { 41, 0 };
	}

	@Override
	public void click(Player p, PipeDirection side) {
		GoldenPipeInv.updateGoldenPipeInventory(p, this);
	}

	public ItemData[] getOutputItems(PipeDirection pd) {
		return outputItems[pd.getId()];
	}

	public FilteringMode getFilteringMode(int line) {
		return filteringModes[line];
	}

	public void setFilteringMode(int line, FilteringMode filteringMode) {
		filteringModes[line] = filteringMode;
	}

	public void changeOutputItems(PipeDirection pd, ItemData[] items) {
		for (int i = 0; i < outputItems[pd.getId()].length; i++) {
			if (i < items.length) {
				outputItems[pd.getId()][i] = items[i];
			} else {
				outputItems[pd.getId()][i] = null;
			}
		}
	}

	@Override
	public PipeType getPipeType() {
		return PipeType.GOLDEN;
	}

	@Override
	public List<ItemStack> getDroppedItems() {
		List<ItemStack> is = new ArrayList<>();
		is.add(PipeItemUtils.getPipeItem(getPipeType(), null));
		for (int line = 0; line < 6; line++) {
			for (int i = 0; i < outputItems[line].length; i++) {
				if (outputItems[line][i] != null) {
					is.add(outputItems[line][i].toItemStack());
				}
			}
		}
		return is;
	}

	public enum FilteringMode {
		FILTERBY_TYPE(LocConf.GOLDENPIPE_FILTERING_FILTERBY_TYPE),
		FILTERBY_TYPE_DAMAGE(LocConf.GOLDENPIPE_FILTERING_FILTERBY_TYPE_DAMAGE),
		FILTERBY_TYPE_NBT(LocConf.GOLDENPIPE_FILTERING_FILTERBY_TYPE_NBT),
		FILTERBY_TYPE_DAMAGE_NBT(LocConf.GOLDENPIPE_FILTERING_FILTERBY_TYPE_DAMAGE_NBT),
		BLOCK_ALL(LocConf.GOLDENPIPE_FILTERING_BLOCKALL),
		INVERT(LocConf.GOLDENPIPE_FILTERING_INVERT);

		private String locConfKey;

		private FilteringMode(String locConfKey) {
			this.locConfKey = locConfKey;
		}

		public String getLocConfKey() {
			return locConfKey;
		}

		public int getId() {
			return this.ordinal();
		}

		public static FilteringMode fromId(int id) {
			return FilteringMode.values()[id];
		}

		public FilteringMode getNextMode() {
			if (getId() == FilteringMode.values().length - 1) {
				return fromId(0);
			}
			return fromId(getId() + 1);
		}

	}

}
