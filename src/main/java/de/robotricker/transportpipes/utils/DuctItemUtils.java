package de.robotricker.transportpipes.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.utils.ductdetails.DuctDetails;

public class DuctItemUtils {

	private static Map<DuctDetails, ItemStack> ductItems = new HashMap<>();
	private static ItemStack wrenchItem;

	static {
		wrenchItem = InventoryUtils.createGlowingItemStack(Material.STICK, (short) 0);
		InventoryUtils.changeDisplayName(wrenchItem, TransportPipes.instance.getFormattedWrenchName());
	}

	public static ItemStack getDuctItem(DuctDetails ductDetails) {
		return ductItems.get(ductDetails);
	}

	public static ItemStack getClonedDuctItem(DuctDetails ductDetails) {
		return getDuctItem(ductDetails).clone();
	}

	public static void registerDuctItem(DuctDetails ductDetails, ItemStack ductItem) {
		ductItems.put(ductDetails, ductItem);
	}

	public static ItemStack getWrenchItem() {
		return wrenchItem;
	}

	public static ItemStack getClonedWrenchItem() {
		return wrenchItem.clone();
	}

	public static List<ItemStack> getAllDuctItems(){
		List<ItemStack> list = new ArrayList<ItemStack>();
		list.addAll(ductItems.values());
		return list;
	}
	
	public static DuctDetails getDuctDetailsOfItem(ItemStack item) {
		if(item == null) {
			return null;
		}
		for (DuctDetails dd : ductItems.keySet()) {
			if (getDuctItem(dd).isSimilar(item)) {
				return dd;
			}
		}
		return null;
	}

}
