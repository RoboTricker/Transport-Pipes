package de.robotricker.transportpipes.pipeitems;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.jnbt.CompoundTag;
import org.jnbt.Tag;

import de.robotricker.transportpipes.pipes.types.GoldenPipe.FilteringMode;
import de.robotricker.transportpipes.pipeutils.InventoryUtils;
import de.robotricker.transportpipes.pipeutils.NBTUtils;

public class ItemData {

	private ItemStack item;

	public ItemData(ItemStack itemStack) {
		item = itemStack.clone();
		item.setAmount(1);
	}

	public ItemStack toItemStack() {
		return item.clone();
	}

	public boolean equals(Object obj, FilteringMode filteringMode) {
		if (obj != null && obj instanceof ItemData) {
			ItemData o = (ItemData) obj;
			if (filteringMode == FilteringMode.CHECK_TYPE_DAMAGE_NBT) {
				return o.item.equals(item);
			} else if (filteringMode == FilteringMode.CHECK_TYPE_DAMAGE) {
				return o.item.getType() == item.getType() && o.item.getDurability() == item.getDurability();
			} else if (filteringMode == FilteringMode.CHECK_TYPE_NBT) {
				return o.item.getType() == item.getType() && o.item.getItemMeta().equals(item.getItemMeta());
			} else if (filteringMode == FilteringMode.CHECK_TYPE) {
				return o.item.getType() == item.getType();
			}
		}
		return false;
	}

	public CompoundTag toNBTTag() {
		Map<String, Tag> map = new HashMap<>();
		NBTUtils.saveStringValue(map, "Item", InventoryUtils.ItemStackToString(item));
		return new CompoundTag("Item", map);
	}

	public static ItemData fromNBTTag(CompoundTag tag) {
		Map<String, Tag> map = tag.getValue();
		String rawItem = NBTUtils.readStringTag(map.get("Item"), null);
		ItemStack item = InventoryUtils.StringToItemStack(rawItem);
		return item != null ? new ItemData(item) : null;
	}
}
