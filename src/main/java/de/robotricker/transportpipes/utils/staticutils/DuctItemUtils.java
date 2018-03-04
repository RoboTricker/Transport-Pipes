package de.robotricker.transportpipes.utils.staticutils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.duct.DuctType;
import de.robotricker.transportpipes.protocol.ReflectionManager;
import de.robotricker.transportpipes.utils.config.LocConf;
import de.robotricker.transportpipes.utils.ductdetails.DuctDetails;

public class DuctItemUtils {

	private static Map<DuctDetails, ItemStack> ductItems = new HashMap<>();
	private static ItemStack wrenchItem;

	static {
		wrenchItem = InventoryUtils.decodeConfigItemString(TransportPipes.instance.generalConf.getWrenchItem());
		wrenchItem = addWrenchNBTTag(wrenchItem);
		if (TransportPipes.instance.generalConf.getWrenchEnchanted()) {
			wrenchItem = InventoryUtils.createGlowingItemStack(wrenchItem);
		}
		InventoryUtils.changeDisplayName(wrenchItem, "§c" + LocConf.load(LocConf.PIPES_WRENCH));
	}

	public static ItemStack getDuctItem(DuctDetails ductDetails) {
		for(DuctDetails dd : ductItems.keySet()) {
			if(dd.equals(ductDetails)) {
				return ductItems.get(dd);
			}
		}
		return null;
	}

	public static ItemStack getClonedDuctItem(DuctDetails ductDetails) {
		return getDuctItem(ductDetails).clone();
	}

	public static void registerDuctItem(DuctDetails ductDetails, ItemStack ductItem) {
		ductItems.put(ductDetails, addDuctNBTTag(ductDetails, ductItem));
	}

	public static ItemStack getWrenchItem() {
		return wrenchItem;
	}

	public static ItemStack getClonedWrenchItem() {
		return wrenchItem.clone();
	}

	public static List<ItemStack> getAllDuctItems() {
		List<ItemStack> list = new ArrayList<ItemStack>();
		list.addAll(ductItems.values());
		return list;
	}

	public static DuctDetails getDuctDetailsOfItem(ItemStack item) {
		if (item == null) {
			return null;
		}
		return readDuctNBTTag(item);
	}

	public static boolean isWrenchItem(ItemStack item) {
		if (item == null) {
			return false;
		}
		return readWrenchNBTTag(item);
	}

	private static ItemStack addDuctNBTTag(DuctDetails ductDetails, ItemStack item) {
		return ReflectionManager.manipulateItemStackNBT(item, "ductDetails", ductDetails.toString(), String.class, "String");
	}

	private static DuctDetails readDuctNBTTag(ItemStack item) {
		String ductDetailsSerialized = (String) ReflectionManager.readItemStackNBT(item, "ductDetails", "String");
		if (ductDetailsSerialized != null && !ductDetailsSerialized.isEmpty()) {
			DuctDetails dd = DuctDetails.decodeString(ductDetailsSerialized);
			return dd;
		}
		return null;
	}

	private static ItemStack addWrenchNBTTag(ItemStack item) {
		return ReflectionManager.manipulateItemStackNBT(item, "wrench", true, boolean.class, "Boolean");
	}

	private static boolean readWrenchNBTTag(ItemStack item) {
		Object wrench = ReflectionManager.readItemStackNBT(item, "wrench", "Boolean");
		if (wrench != null) {
			return ((boolean) wrench);
		}
		return false;
	}

}
