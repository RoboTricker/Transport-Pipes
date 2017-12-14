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

import de.robotricker.transportpipes.duct.pipe.GoldenPipe;
import de.robotricker.transportpipes.duct.pipe.utils.FilteringMode;
import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.utils.InventoryUtils;
import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.config.LocConf;

public class GoldenPipeInv implements Listener {

	private static Map<GoldenPipe, Inventory> goldenPipeInventories = new HashMap<>();
	private static Map<GoldenPipe, Map<Integer, Integer>> scrollValues = new HashMap<>();

	public static void updateGoldenPipeInventory(Player p, GoldenPipe pipe) {
		Inventory inv;
		if (goldenPipeInventories.containsKey(pipe)) {
			inv = goldenPipeInventories.get(pipe);
		} else {
			inv = Bukkit.createInventory(null, 6 * 9, LocConf.load(LocConf.GOLDENPIPE_TITLE));
			goldenPipeInventories.put(pipe, inv);
			scrollValues.put(pipe, new HashMap<Integer, Integer>());
		}

		Collection<WrappedDirection> pipeConnections = pipe.getAllConnections();

		for (int line = 0; line < 6; line++) {
			GoldenPipeColor gpc = GoldenPipeColor.values()[line];
			WrappedDirection pd = WrappedDirection.fromID(line);
			int scrollValue = scrollValues.get(pipe).containsKey(line) ? scrollValues.get(pipe).get(line) : 0;

			String filteringModeText = LocConf.load(pipe.getFilteringMode(line).getLocConfKey());
			ItemStack filteringModeWool = InventoryUtils.changeDisplayNameAndLore(new ItemStack(Material.WOOL, 1, gpc.getItemDamage()), filteringModeText, LocConf.load(LocConf.FILTERING_CLICKTOCHANGE));
			ItemStack glassPane = InventoryUtils.changeDisplayNameAndLore(new ItemStack(Material.STAINED_GLASS_PANE, 1, gpc.getItemDamage()), String.valueOf(ChatColor.RESET));
			ItemStack barrier = InventoryUtils.changeDisplayNameAndLore(new ItemStack(Material.BARRIER, 1), String.valueOf(ChatColor.RESET));
			ItemStack scrollLeft = InventoryUtils.changeDisplayName(InventoryUtils.createSkullItemStack("69b9a08d-4e89-4878-8be8-551caeacbf2a", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2ViZjkwNzQ5NGE5MzVlOTU1YmZjYWRhYjgxYmVhZmI5MGZiOWJlNDljNzAyNmJhOTdkNzk4ZDVmMWEyMyJ9fX0=", null), LocConf.load(LocConf.FILTERING_SCROLL_LEFT));
			ItemStack scrollRight = InventoryUtils.changeDisplayName(InventoryUtils.createSkullItemStack("15f49744-9b61-46af-b1c3-71c6261a0d0e", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWI2ZjFhMjViNmJjMTk5OTQ2NDcyYWVkYjM3MDUyMjU4NGZmNmY0ZTgzMjIxZTU5NDZiZDJlNDFiNWNhMTNiIn19fQ==", null), LocConf.load(LocConf.FILTERING_SCROLL_RIGHT));

			inv.setItem(line * 9, filteringModeWool);

			if (pipe.getFilteringMode(line) == FilteringMode.BLOCK_ALL) {
				for (int i = 1; i < 9; i++) {
					inv.setItem(line * 9 + i, barrier);
				}
			} else if (!pipeConnections.contains(pd)) {
				for (int i = 1; i < 9; i++) {
					inv.setItem(line * 9 + i, glassPane);
				}
			} else {
				inv.setItem(line * 9 + 1, scrollLeft);
				inv.setItem(line * 9 + 8, scrollRight);

				ItemData[] items = pipe.getFilteringItems(WrappedDirection.fromID(line));
				int indexWithScrollValue = scrollValue;
				for (int i = 2; i < 8; i++) {
					if (items[indexWithScrollValue] != null) {
						inv.setItem(line * 9 + i, items[indexWithScrollValue].toItemStack());
					} else {
						inv.setItem(line * 9 + i, null);
					}
					indexWithScrollValue++;
				}
			}
		}

		p.openInventory(inv);

	}

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if (e.getClickedInventory() != null && goldenPipeInventories.containsValue(e.getClickedInventory())) {
			GoldenPipe pipe = null;
			//get pipe with inventory
			for (GoldenPipe gp : goldenPipeInventories.keySet()) {
				if (goldenPipeInventories.get(gp).equals(e.getClickedInventory())) {
					pipe = gp;
					break;
				}
			}
			if (pipe == null) {
				return;
			}
			//clicked on glass pane
			if (InventoryUtils.isGlassItemOrBarrier(e.getCurrentItem())) {
				e.setCancelled(true);
				return;
			}
			//clicked filtering mode wool
			if (e.getRawSlot() >= 0 && e.getRawSlot() <= e.getClickedInventory().getSize() && e.getRawSlot() % 9 == 0) {
				e.setCancelled(true);

				int line = (int) (e.getRawSlot() / 9);
				pipe.setFilteringMode(line, pipe.getFilteringMode(line).getNextMode());

				// Update inv
				saveGoldenPipeInv((Player) e.getWhoClicked(), e.getClickedInventory());
				updateGoldenPipeInventory((Player) e.getWhoClicked(), pipe);
				return;
			}
			//clicked scroll left
			if (e.getRawSlot() >= 0 && e.getRawSlot() <= e.getClickedInventory().getSize() && e.getRawSlot() % 9 == 1) {
				e.setCancelled(true);

				saveGoldenPipeInv((Player) e.getWhoClicked(), e.getClickedInventory());

				int line = (int) (e.getRawSlot() / 9);
				int scrollValue = scrollValues.get(pipe).containsKey(line) ? scrollValues.get(pipe).get(line) : 0;
				if (scrollValue > 0) {
					scrollValue--;
				}
				scrollValues.get(pipe).put(line, scrollValue);

				updateGoldenPipeInventory((Player) e.getWhoClicked(), pipe);

				return;
			}
			//clicked scroll right
			if (e.getRawSlot() >= 0 && e.getRawSlot() <= e.getClickedInventory().getSize() && e.getRawSlot() % 9 == 8) {
				e.setCancelled(true);

				saveGoldenPipeInv((Player) e.getWhoClicked(), e.getClickedInventory());

				int line = (int) (e.getRawSlot() / 9);
				int scrollValue = scrollValues.get(pipe).containsKey(line) ? scrollValues.get(pipe).get(line) : 0;
				if (scrollValue < GoldenPipe.ITEMS_PER_ROW - 6) {
					scrollValue++;
				}
				scrollValues.get(pipe).put(line, scrollValue);

				updateGoldenPipeInventory((Player) e.getWhoClicked(), pipe);

				return;
			}
		}

	}

	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		saveGoldenPipeInv((Player) e.getPlayer(), e.getInventory());
	}

	private void saveGoldenPipeInv(Player p, Inventory inv) {
		if (inv != null && goldenPipeInventories.containsValue(inv)) {
			GoldenPipe pipe = null;
			//get pipe with inventory
			for (GoldenPipe gp : goldenPipeInventories.keySet()) {
				if (goldenPipeInventories.get(gp).equals(inv)) {
					pipe = gp;
					break;
				}
			}
			//cache new items in golden pipe
			linefor: for (int line = 0; line < 6; line++) {
				ItemData[] items = pipe.getFilteringItems(WrappedDirection.fromID(line));
				int scrollValue = scrollValues.get(pipe).containsKey(line) ? scrollValues.get(pipe).get(line) : 0;
				for (int i = 2; i < 8; i++) {
					ItemStack is = inv.getItem(line * 9 + i);
					//make sure the glass pane won't be saved
					if (!InventoryUtils.isGlassItemOrBarrier(is)) {
						if (is != null && is.getAmount() > 1) {
							ItemStack drop = is.clone();
							drop.setAmount(is.getAmount() - 1);
							p.getWorld().dropItem(p.getLocation(), drop);
							is.setAmount(1);
						}
						items[scrollValue] = is != null ? new ItemData(is) : null;
					} else {
						//skip this save-sequenz if this line is not available (not a pipe or block as neighbor)
						continue linefor;
					}
					scrollValue++;
				}
			}
		}
	}

}
