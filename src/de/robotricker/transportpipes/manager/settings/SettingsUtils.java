package de.robotricker.transportpipes.manager.settings;

import java.util.Arrays;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SettingsUtils {

	public static ItemStack changeDisplayNameAndLore(ItemStack is, String displayName, String... lore) {
		ItemMeta meta = is.getItemMeta();
		meta.setDisplayName(displayName);
		meta.setLore(Arrays.asList(lore));
		is.setItemMeta(meta);
		return is;
	}

	public static boolean hasDisplayName(ItemStack is, String displayName){
		return is != null && is.hasItemMeta() && is.getItemMeta().hasDisplayName() && is.getItemMeta().getDisplayName().equals(displayName);
	}
	
}
