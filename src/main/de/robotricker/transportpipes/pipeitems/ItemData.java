package main.de.robotricker.transportpipes.pipeitems;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jnbt.ByteTag;
import org.jnbt.CompoundTag;
import org.jnbt.IntTag;
import org.jnbt.StringTag;
import org.jnbt.Tag;

public class ItemData {

	private Material material;
	private byte data;
	private String displayName;

	public ItemData(ItemStack itemStack) {
		this.material = itemStack.getType();
		this.data = itemStack.getData().getData();
		this.displayName = itemStack.hasItemMeta() ? itemStack.getItemMeta().getDisplayName() : null;
	}

	public ItemData(Material material, byte data, String displayName) {
		this.material = material;
		this.data = data;
		this.displayName = displayName;
	}

	public ItemData(Material material, byte data) {
		this(material, data, null);
	}

	public Material getMaterial() {
		return material;
	}

	public void setMaterial(Material material) {
		this.material = material;
	}

	public byte getData() {
		return data;
	}

	public void setData(byte data) {
		this.data = data;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public ItemStack toItemStack() {
		ItemStack itemStack = new ItemStack(material, 1, data);
		if (displayName != null) {
			ItemMeta meta = itemStack.getItemMeta();
			meta.setDisplayName(displayName);
			itemStack.setItemMeta(meta);
		}
		return itemStack;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof ItemData) {
			boolean displayNameEquals = false;
			if (((ItemData) obj).displayName == null && displayName == null) {
				displayNameEquals = true;
			} else if (((ItemData) obj).displayName != null) {
				displayNameEquals = ((ItemData) obj).displayName.equals(displayName);
			}
			return ((ItemData) obj).material.equals(material) && ((ItemData) obj).data == data && displayNameEquals;
		}
		return false;
	}

	public CompoundTag toNBTTag() {
		Map<String, Tag> map = new HashMap<>();
		map.put("Material", new IntTag("Material", getMaterial().getId()));
		map.put("Data", new ByteTag("Data", getData()));
		if (displayName != null) {
			map.put("DisplayName", new StringTag("DisplayName", getDisplayName()));
		}
		return new CompoundTag("Item", map);
	}

	public static ItemData fromNBTTag(CompoundTag tag) {
		Map<String, Tag> map = tag.getValue();
		Material m = Material.getMaterial(((IntTag) map.get("Material")).getValue());
		byte d = ((ByteTag) map.get("Data")).getValue();
		String dm = map.containsKey("DisplayName") ? ((StringTag) map.get("DisplayName")).getValue() : null;
		return new ItemData(m, d, dm);
	}

}
