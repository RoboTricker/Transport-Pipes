package de.robotricker.transportpipes.utils.staticutils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;

import de.robotricker.transportpipes.protocol.ReflectionManager;

public class InventoryUtils {

	public static ItemStack changeAmount(ItemStack item, int amountDelta) {
		ItemStack copy = item.clone();
		if (item.getAmount() + amountDelta > 0) {
			copy.setAmount(Math.min(item.getMaxStackSize(), item.getAmount() + amountDelta));
		} else {
			copy = null;
		}
		return copy;
	}

	public static ItemStack createOneAmountItemStack(ItemStack item) {
		ItemStack copy = item.clone();
		copy.setAmount(1);
		return copy;
	}

	public static String ItemStackToString(ItemStack itemStack) {
		YamlConfiguration yaml = new YamlConfiguration();
		yaml.set("item", itemStack);
		return yaml.saveToString();
	}

	public static ItemStack StringToItemStack(String string) {
		if (string == null) {
			return null;
		}
		YamlConfiguration yaml = new YamlConfiguration();
		try {
			yaml.loadFromString(string);
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		if (yaml.contains("item")) {
			return yaml.getItemStack("item", null);
		} else {
			return yaml.getItemStack("i", null);
		}
	}

	public static ItemStack changeDisplayName(ItemStack is, String displayName) {
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(displayName);
		is.setItemMeta(im);
		return is;
	}

	public static ItemStack changeDisplayNameAndLore(ItemStack is, String displayName, String... lore) {
		ItemMeta meta = is.getItemMeta();
		meta.setDisplayName(displayName);
		meta.setLore(Arrays.asList(lore));
		is.setItemMeta(meta);
		return is;
	}

	public static ItemStack changeDisplayNameAndLoreConfig(ItemStack is, String displayName, List<String> lore) {
		ItemMeta meta = is.getItemMeta();
		meta.setDisplayName(displayName);
		meta.setLore(lore);
		is.setItemMeta(meta);
		return is;
	}

	public static boolean hasDisplayName(ItemStack is, String displayName) {
		return is != null && is.hasItemMeta() && is.getItemMeta().hasDisplayName() && is.getItemMeta().getDisplayName().equals(displayName);
	}

	public static ItemStack createToolItemStack(int damage) {
		ItemStack is = ReflectionManager.setItemStackUnbreakable(new ItemStack(Material.WOOD_PICKAXE, 1, (short) damage));
		ItemMeta im = is.getItemMeta();
		im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		im.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		is.setItemMeta(im);
		return is;
	}

	public static ItemStack createGlowingItemStack(Material material, short data) {
		ItemStack is = new ItemStack(material, 1, data);
		ItemMeta im = is.getItemMeta();
		im.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
		im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		is.setItemMeta(im);
		return is;
	}

	public static ItemStack createSkullItemStack(String uuid, String textureValue, String textureSignature) {

		WrappedGameProfile wrappedProfile = new WrappedGameProfile(UUID.fromString(uuid), null);
		wrappedProfile.getProperties().put("textures", new WrappedSignedProperty("textures", textureValue, textureSignature));

		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
		SkullMeta sm = (SkullMeta) skull.getItemMeta();

		Field profileField = null;
		try {
			profileField = sm.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(sm, wrappedProfile.getHandle());
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
			e1.printStackTrace();
		}

		skull.setItemMeta(sm);
		return skull;
	}
	
	public static boolean isGlassItemOrBarrier(ItemStack is) {
		return InventoryUtils.hasDisplayName(is, String.valueOf(ChatColor.RESET));
	}

}
