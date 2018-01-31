package de.robotricker.transportpipes.pipeitems;

import java.util.List;
import org.bukkit.inventory.ItemStack;

import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
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

	public int applyFilter(List<ItemData> filterItems, FilteringMode filteringMode) {
		return applyFilter(filterItems, filteringMode, false);
	}

	/**
	 * returns the weight of how many of this itemData objects should pass this
	 * filter
	 */
	public int applyFilter(List<ItemData> filterItems, FilteringMode filteringMode, boolean ignoreEmpty) {
		if (filteringMode == FilteringMode.BLOCK_ALL) {
			return 0;
		}
		if (!ignoreEmpty && filterItems.isEmpty()) {
			return 1;
		}
		if (filteringMode == FilteringMode.INVERT) {
			boolean equals = false;
			for (ItemData filterItem : filterItems) {
				equals |= filterItem.item.isSimilar(item);
			}
			return !equals ? 1 : 0;
		} else {
			int weight = 0;
			for (ItemData filterItem : filterItems) {
				if (filteringMode == FilteringMode.FILTERBY_TYPE_DAMAGE_NBT) {
					weight += filterItem.item.isSimilar(item) ? 1 : 0;
				} else if (filteringMode == FilteringMode.FILTERBY_TYPE_DAMAGE) {
					weight += (filterItem.item.getType() == item.getType() && filterItem.item.getDurability() == item.getDurability()) ? 1 : 0;
				} else if (filteringMode == FilteringMode.FILTERBY_TYPE_NBT) {
					weight += (filterItem.item.getType() == item.getType() && filterItem.item.getItemMeta().equals(item.getItemMeta())) ? 1 : 0;
				} else if (filteringMode == FilteringMode.FILTERBY_TYPE) {
					weight += filterItem.item.getType() == item.getType() ? 1 : 0;
				}
			}
			return weight;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((item == null) ? 0 : item.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ItemData other = (ItemData) obj;
		if (item == null) {
			if (other.item != null)
				return false;
		} else if (!item.isSimilar(other.item))
			return false;
		return true;
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

}
