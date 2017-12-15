package de.robotricker.transportpipes.pipeitems;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.Tag;

import de.robotricker.transportpipes.duct.pipe.utils.FilteringMode;
import de.robotricker.transportpipes.utils.staticutils.InventoryUtils;
import de.robotricker.transportpipes.utils.staticutils.NBTUtils;

public class ItemData {

	private ItemStack item;

	public ItemData(ItemStack itemStack) {
		item = itemStack.clone();
		item.setAmount(1);
	}

	public ItemStack toItemStack() {
		return item.clone();
	}

	public boolean checkFilter(List<ItemData> filterItems, FilteringMode filteringMode) {
		return checkFilter(filterItems, filteringMode, false);
	}

	/**
	 * returns whether the given filter accepts this item object
	 */
	public boolean checkFilter(List<ItemData> filterItems, FilteringMode filteringMode, boolean ignoreEmpty) {
		if (filteringMode == FilteringMode.BLOCK_ALL) {
			return false;
		}
		if (!ignoreEmpty && filterItems.isEmpty()) {
			return true;
		}
		if (filteringMode == FilteringMode.INVERT) {
			boolean equals = false;
			for (ItemData filterItem : filterItems) {
				equals |= filterItem.item.isSimilar(item);
			}
			return !equals;
		} else {
			boolean equals = false;
			for (ItemData filterItem : filterItems) {
				if (filteringMode == FilteringMode.FILTERBY_TYPE_DAMAGE_NBT) {
					equals |= filterItem.item.isSimilar(item);
				} else if (filteringMode == FilteringMode.FILTERBY_TYPE_DAMAGE) {
					equals |= filterItem.item.getType() == item.getType() && filterItem.item.getDurability() == item.getDurability();
				} else if (filteringMode == FilteringMode.FILTERBY_TYPE_NBT) {
					equals |= filterItem.item.getType() == item.getType() && filterItem.item.getItemMeta().equals(item.getItemMeta());
				} else if (filteringMode == FilteringMode.FILTERBY_TYPE) {
					equals |= filterItem.item.getType() == item.getType();
				}
			}
			return equals;
		}
	}

	public CompoundTag toNBTTag() {
		CompoundMap map = new CompoundMap();
		NBTUtils.saveStringValue(map, "Item", InventoryUtils.ItemStackToString(item));
		return new CompoundTag("Item", map);
	}

	public static ItemData fromNBTTag(CompoundTag tag) {
		CompoundMap map = tag.getValue();
		String rawItem = NBTUtils.readStringTag(map.get("Item"), null);
		ItemStack item = InventoryUtils.StringToItemStack(rawItem);
		return item != null ? new ItemData(item) : null;
	}

	public static CompoundTag createNullItemNBTTag() {
		CompoundMap map = new CompoundMap();
		return new CompoundTag("Item", map);
	}

}
