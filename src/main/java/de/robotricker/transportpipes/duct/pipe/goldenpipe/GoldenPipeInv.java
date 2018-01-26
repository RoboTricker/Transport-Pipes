package de.robotricker.transportpipes.duct.pipe.goldenpipe;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctSharedInv;
import de.robotricker.transportpipes.duct.pipe.GoldenPipe;
import de.robotricker.transportpipes.duct.pipe.utils.FilteringMode;
import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.config.LocConf;
import de.robotricker.transportpipes.utils.staticutils.InventoryUtils;

public class GoldenPipeInv extends DuctSharedInv {

	private Map<Integer, Integer> scrollValues;

	public GoldenPipeInv(Duct duct) {
		super(duct, LocConf.load(LocConf.GOLDENPIPE_TITLE), 6 * 9);
		scrollValues = new HashMap<>();
	}

	@Override
	protected void populateInventory(Player p, Inventory inventory) {

		GoldenPipe gp = (GoldenPipe) duct;

		Collection<WrappedDirection> pipeConnections = duct.getAllConnections();

		for (int line = 0; line < 6; line++) {
			GoldenPipeColor gpc = GoldenPipeColor.values()[line];
			WrappedDirection pd = WrappedDirection.fromID(line);
			int scrollValue = scrollValues.containsKey(line) ? scrollValues.get(line) : 0;

			String filteringModeText = LocConf.load(gp.getFilteringMode(line).getLocConfKey());
			ItemStack filteringModeWool = InventoryUtils.changeDisplayNameAndLore(new ItemStack(Material.WOOL, 1, gpc.getItemDamage()), filteringModeText, LocConf.load(LocConf.FILTERING_CLICKTOCHANGE));
			ItemStack glassPane = InventoryUtils.changeDisplayNameAndLore(new ItemStack(Material.STAINED_GLASS_PANE, 1, gpc.getItemDamage()), String.valueOf(ChatColor.RESET));
			ItemStack barrier = InventoryUtils.changeDisplayNameAndLore(new ItemStack(Material.BARRIER, 1), String.valueOf(ChatColor.RESET));
			ItemStack scrollLeft = InventoryUtils.changeDisplayName(InventoryUtils.createSkullItemStack("69b9a08d-4e89-4878-8be8-551caeacbf2a", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2ViZjkwNzQ5NGE5MzVlOTU1YmZjYWRhYjgxYmVhZmI5MGZiOWJlNDljNzAyNmJhOTdkNzk4ZDVmMWEyMyJ9fX0=", null), LocConf.load(LocConf.FILTERING_SCROLL_LEFT));
			ItemStack scrollRight = InventoryUtils.changeDisplayName(InventoryUtils.createSkullItemStack("15f49744-9b61-46af-b1c3-71c6261a0d0e", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWI2ZjFhMjViNmJjMTk5OTQ2NDcyYWVkYjM3MDUyMjU4NGZmNmY0ZTgzMjIxZTU5NDZiZDJlNDFiNWNhMTNiIn19fQ==", null), LocConf.load(LocConf.FILTERING_SCROLL_RIGHT));

			inventory.setItem(line * 9, filteringModeWool);

			if (gp.getFilteringMode(line) == FilteringMode.BLOCK_ALL) {
				for (int i = 1; i < 9; i++) {
					inventory.setItem(line * 9 + i, barrier);
				}
			} else if (!pipeConnections.contains(pd)) {
				for (int i = 1; i < 9; i++) {
					inventory.setItem(line * 9 + i, glassPane);
				}
			} else {
				inventory.setItem(line * 9 + 1, scrollLeft);
				inventory.setItem(line * 9 + 8, scrollRight);

				ItemData[] items = gp.getFilteringItems(WrappedDirection.fromID(line));
				int indexWithScrollValue = scrollValue;
				for (int i = 2; i < 8; i++) {
					if (items[indexWithScrollValue] != null) {
						inventory.setItem(line * 9 + i, items[indexWithScrollValue].toItemStack());
					} else {
						inventory.setItem(line * 9 + i, null);
					}
					indexWithScrollValue++;
				}
			}
		}

	}

	@Override
	protected boolean notifyInvClick(Player p, int rawSlot, Inventory inventory) {
		GoldenPipe gp = (GoldenPipe) duct;
		boolean cancelled = false;

		// clicked filtering mode wool
		if (rawSlot >= 0 && rawSlot <= inventory.getSize() && rawSlot % 9 == 0) {
			cancelled = true;

			int line = (int) (rawSlot / 9);
			gp.setFilteringMode(line, gp.getFilteringMode(line).getNextMode());

			// Save and update inv
			notifyInvSave(p, inventory);
			openOrUpdateInventory(p);
			return cancelled;
		}
		// clicked scroll left
		if (rawSlot >= 0 && rawSlot <= inventory.getSize() && rawSlot % 9 == 1) {
			cancelled = true;

			notifyInvSave(p, inventory);

			int line = (int) (rawSlot / 9);
			int scrollValue = scrollValues.containsKey(line) ? scrollValues.get(line) : 0;
			if (scrollValue > 0) {
				scrollValue--;
			}
			scrollValues.put(line, scrollValue);

			openOrUpdateInventory(p);

			return cancelled;
		}
		// clicked scroll right
		if (rawSlot >= 0 && rawSlot <= inventory.getSize() && rawSlot % 9 == 8) {
			cancelled = true;

			notifyInvSave(p, inventory);

			int line = (int) (rawSlot / 9);
			int scrollValue = scrollValues.containsKey(line) ? scrollValues.get(line) : 0;
			if (scrollValue < GoldenPipe.ITEMS_PER_ROW - 6) {
				scrollValue++;
			}
			scrollValues.put(line, scrollValue);

			openOrUpdateInventory(p);

			return cancelled;
		}

		return cancelled;
	}

	@Override
	protected void notifyInvSave(Player p, Inventory inventory) {
		GoldenPipe gp = (GoldenPipe) duct;

		// cache new items in golden pipe
		linefor: for (int line = 0; line < 6; line++) {
			ItemData[] items = gp.getFilteringItems(WrappedDirection.fromID(line));
			int scrollValueTemp = scrollValues.containsKey(line) ? scrollValues.get(line) : 0;
			for (int i = 2; i < 8; i++) {
				ItemStack is = inventory.getItem(line * 9 + i);
				// make sure the glass pane won't be saved
				if (!InventoryUtils.isGlassItemOrBarrier(is)) {
					if (is != null && is.getAmount() > 1) {
						ItemStack drop = is.clone();
						drop.setAmount(is.getAmount() - 1);
						p.getWorld().dropItem(p.getLocation(), drop);
						is.setAmount(1);
					}
					items[scrollValueTemp] = is != null ? new ItemData(is) : null;
				} else {
					// skip this save-sequenz if this line is not available (not a pipe or block as
					// neighbor)
					continue linefor;
				}
				scrollValueTemp++;
			}
		}
	}

}
