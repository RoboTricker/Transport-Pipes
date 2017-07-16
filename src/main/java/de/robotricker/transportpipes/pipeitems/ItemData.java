package de.robotricker.transportpipes.pipeitems;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.jnbt.CompoundTag;
import org.jnbt.Tag;

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

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof ItemData) {
			ItemData o = (ItemData) obj;
			return o.item.equals(item);
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
