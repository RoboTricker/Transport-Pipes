package de.robotricker.transportpipes.duct.pipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
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
import de.robotricker.transportpipes.duct.pipe.goldenpipe.GoldenPipeInv;
import de.robotricker.transportpipes.duct.pipe.utils.FilteringMode;
import de.robotricker.transportpipes.duct.pipe.utils.PipeType;
import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.ductdetails.DuctDetails;
import de.robotricker.transportpipes.utils.ductdetails.PipeDetails;
import de.robotricker.transportpipes.utils.staticutils.DuctItemUtils;
import de.robotricker.transportpipes.utils.staticutils.InventoryUtils;
import de.robotricker.transportpipes.utils.staticutils.NBTUtils;

public class GoldenPipe extends Pipe implements ClickableDuct, InventoryDuct {

	public static final int ITEMS_PER_ROW = 32;

	// 1st dimension: output dirs in order of PipeDirection.values() | 2nd
	// dimension: output items in this direction
	private ItemData[][] filteringItems;
	private FilteringMode[] filteringModes;

	private GoldenPipeInv inventory;

	public GoldenPipe(Location blockLoc) {
		super(blockLoc);
		filteringItems = new ItemData[6][ITEMS_PER_ROW];
		filteringModes = new FilteringMode[6];
		for (int i = 0; i < 6; i++) {
			filteringModes[i] = FilteringMode.FILTERBY_TYPE_DAMAGE_NBT;
		}
		inventory = new GoldenPipeInv(this);
	}

	@Override
	public Map<WrappedDirection, Integer> handleArrivalAtMiddle(PipeItem item, WrappedDirection before, Collection<WrappedDirection> possibleDirs) {
		Map<WrappedDirection, Integer> absWeights = calculateAbsWeights(new ItemData(item.getItem()), before, possibleDirs);

		return getItemDistribution().splitPipeItem(item.getItem(), possibleDirs, absWeights);
	}

	private Map<WrappedDirection, Integer> calculateAbsWeights(ItemData itemData, WrappedDirection before, Collection<WrappedDirection> possibleDirs) {
		Map<WrappedDirection, Integer> absWeights = new HashMap<>();
		Set<WrappedDirection> emptyDirections = new HashSet<>();

		for (int line = 0; line < 6; line++) {
			WrappedDirection dir = WrappedDirection.fromID(line);

			// item shouldn't go directly back
			if (dir.getOpposite() == before) {
				continue;
			}
			// ignore direction if there isn't any pipe or container
			if (!possibleDirs.contains(dir)) {
				continue;
			}

			FilteringMode filteringMode = getFilteringMode(line);

			List<ItemData> filterItems = new ArrayList<ItemData>();
			for (ItemData filterItem : filteringItems[line]) {
				if (filterItem != null) {
					filterItems.add(filterItem);
				}
			}

			int weight = itemData.applyFilter(filterItems, filteringMode, true);
			if (weight > 0) {
				absWeights.put(dir, weight);
			} else if (itemData.applyFilter(filterItems, filteringMode, false) > 0) {
				emptyDirections.add(dir);
			}

		}

		if (absWeights.isEmpty()) {
			for (WrappedDirection wd : emptyDirections) {
				absWeights.put(wd, 1);
			}
		}

		return absWeights;
	}

	@Override
	public void saveToNBTTag(CompoundMap tags) {
		super.saveToNBTTag(tags);

		List<Tag<?>> linesList = new ArrayList<>();

		for (int line = 0; line < 6; line++) {
			CompoundTag lineCompound = new CompoundTag("Line", new CompoundMap());

			NBTUtils.saveIntValue(lineCompound.getValue(), "Line", line);

			List<Tag<?>> lineList = new ArrayList<>();
			for (int i = 0; i < filteringItems[line].length; i++) {
				ItemData itemData = filteringItems[line][i];
				if (itemData != null) {
					lineList.add(itemData.toNBTTag());
				} else {
					lineList.add(InventoryUtils.createNullItemNBTTag());
				}
			}
			NBTUtils.saveListValue(lineCompound.getValue(), "Items", CompoundTag.class, lineList);
			NBTUtils.saveIntValue(lineCompound.getValue(), "FilteringMode", getFilteringMode(line).getId());

			linesList.add(lineCompound);
		}

		NBTUtils.saveListValue(tags, "Lines", CompoundTag.class, linesList);

	}

	@Override
	public void loadFromNBTTag(CompoundTag tag, long datFileVersion) {
		super.loadFromNBTTag(tag, datFileVersion);

		CompoundMap map = tag.getValue();
		if (NBTUtils.readListTag(map.get("Lines")).isEmpty()) {
			// old lines version
			for (int line = 0; line < 6; line++) {

				List<Tag<?>> lineList = NBTUtils.readListTag(map.get("Line" + line));
				for (int i = 0; i < filteringItems[line].length; i++) {
					if (lineList.size() > i) {
						ItemData itemData = ItemData.fromNBTTag((CompoundTag) lineList.get(i));
						filteringItems[line][i] = itemData;
					}
				}

				FilteringMode fm = FilteringMode.fromId(NBTUtils.readIntTag(map.get("Line" + line + "_filteringMode"), FilteringMode.FILTERBY_TYPE_DAMAGE_NBT.getId()));
				setFilteringMode(line, fm);

			}
		} else {
			// new list version
			List<Tag<?>> linesList = NBTUtils.readListTag(map.get("Lines"));
			for (Tag<?> lineTag : linesList) {
				CompoundTag lineCompound = (CompoundTag) lineTag;
				int line = NBTUtils.readIntTag(lineCompound.getValue().get("Line"), -1);
				if (line != -1) {
					List<Tag<?>> itemsList = NBTUtils.readListTag(lineCompound.getValue().get("Items"));
					int i = 0;
					for (Tag<?> itemTag : itemsList) {
						if (itemsList.size() > i) {
							ItemData itemData = ItemData.fromNBTTag((CompoundTag) itemTag);
							filteringItems[line][i] = itemData;
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
	public void click(Player p, WrappedDirection side) {
		getDuctInventory(p).openOrUpdateInventory(p);
	}

	@Override
	public DuctSharedInv getDuctInventory(Player p) {
		return inventory;
	}

	public ItemData[] getFilteringItems(WrappedDirection pd) {
		return filteringItems[pd.getId()];
	}

	public FilteringMode getFilteringMode(int line) {
		return filteringModes[line];
	}

	public void setFilteringMode(int line, FilteringMode filteringMode) {
		filteringModes[line] = filteringMode;
	}

	public void changeFilteringItems(WrappedDirection pd, ItemData[] items) {
		for (int i = 0; i < filteringItems[pd.getId()].length; i++) {
			if (i < items.length) {
				filteringItems[pd.getId()][i] = items[i];
			} else {
				filteringItems[pd.getId()][i] = null;
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
		is.add(DuctItemUtils.getClonedDuctItem(new PipeDetails(getPipeType())));
		for (int line = 0; line < 6; line++) {
			for (int i = 0; i < filteringItems[line].length; i++) {
				if (filteringItems[line][i] != null) {
					is.add(filteringItems[line][i].toItemStack());
				}
			}
		}
		return is;
	}

	@Override
	public DuctDetails getDuctDetails() {
		return new PipeDetails(getPipeType());
	}

}
