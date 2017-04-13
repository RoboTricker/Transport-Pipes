package de.robotricker.transportpipes.pipeitems;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jnbt.ByteTag;
import org.jnbt.CompoundTag;
import org.jnbt.IntTag;
import org.jnbt.StringTag;
import org.jnbt.Tag;

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
		map.put("Item", new StringTag("Item", itemToStringBlob(item)));
		return new CompoundTag("Item", map);
	}

	public static ItemData fromNBTTag(CompoundTag tag) {
		Map<String, Tag> map = tag.getValue();
		ItemStack item = null;
		try {
			String rawItem = ((StringTag) map.get("Item")).getValue();
			item = stringBlobToItem(rawItem);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to load pipe! (Maybe outdated NBT format?)");
		}
		return new ItemData(item);
	}

	public static String itemToStringBlob(ItemStack itemStack) {
		YamlConfiguration config = new YamlConfiguration();
		config.set("i", itemStack);
		return config.saveToString();
	}

	public static ItemStack stringBlobToItem(String stringBlob) {
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.loadFromString(stringBlob);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return config.getItemStack("i", null);
	}
}
