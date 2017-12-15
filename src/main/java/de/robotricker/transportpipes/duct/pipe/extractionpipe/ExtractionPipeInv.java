package de.robotricker.transportpipes.duct.pipe.extractionpipe;

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

import de.robotricker.transportpipes.duct.pipe.ExtractionPipe;
import de.robotricker.transportpipes.duct.pipe.GoldenPipe;
import de.robotricker.transportpipes.duct.pipe.utils.FilteringMode;
import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.utils.config.LocConf;
import de.robotricker.transportpipes.utils.staticutils.InventoryUtils;

public class ExtractionPipeInv implements Listener {

	private static HashMap<ExtractionPipe, Inventory> extractionPipeInventories = new HashMap<>();
	private static Map<ExtractionPipe, Integer> scrollValues = new HashMap<>();

	public static void updateExtractionPipeInventory(Player p, ExtractionPipe pipe) {
		Inventory inv;
		if (extractionPipeInventories.containsKey(pipe)) {
			inv = extractionPipeInventories.get(pipe);
		} else {
			inv = Bukkit.createInventory(null, 27, LocConf.load(LocConf.EXTRACTIONPIPE_TITLE));
			extractionPipeInventories.put(pipe, inv);
			scrollValues.put(pipe, 0);
		}

		int scrollValue = scrollValues.get(pipe);

		String extractDirectionDisplayName = null;
		if (pipe.getExtractDirection() == null) {
			extractDirectionDisplayName = LocConf.load(LocConf.EXTRACTIONPIPE_DIRECTION_DONTEXTRACT);
		} else {
			extractDirectionDisplayName = String.format(LocConf.load(LocConf.EXTRACTIONPIPE_DIRECTION_TEXT), pipe.getExtractDirection().name());
		}
		ItemStack extractDirection = InventoryUtils.changeDisplayNameAndLore(new ItemStack(Material.TRIPWIRE_HOOK), extractDirectionDisplayName, LocConf.load(LocConf.EXTRACTIONPIPE_DIRECTION_CLICKTOCHANGE));
		ItemStack extractCondition = InventoryUtils.changeDisplayNameAndLore(pipe.getExtractCondition().getDisplayItem(), LocConf.load(pipe.getExtractCondition().getLocConfKey()), LocConf.load(LocConf.EXTRACTIONPIPE_CONDITION_CLICKTOCHANGE));
		ItemStack extractAmount = InventoryUtils.changeDisplayNameAndLore(pipe.getExtractAmount().getDisplayItem(), LocConf.load(pipe.getExtractAmount().getLocConfKey()), LocConf.load(LocConf.EXTRACTIONPIPE_AMOUNT_CLICKTOCHANGE));
		ItemStack glassPane = InventoryUtils.changeDisplayNameAndLore(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15), String.valueOf(ChatColor.RESET));
		String filteringModeText = LocConf.load(pipe.getFilteringMode().getLocConfKey());
		ItemStack filteringModeBlock = InventoryUtils.changeDisplayNameAndLore(new ItemStack(Material.WOOL, 1, (short) 0), filteringModeText, LocConf.load(LocConf.FILTERING_CLICKTOCHANGE));
		ItemStack barrier = InventoryUtils.changeDisplayNameAndLore(new ItemStack(Material.BARRIER, 1), String.valueOf(ChatColor.RESET));
		ItemStack scrollLeft = InventoryUtils.changeDisplayName(InventoryUtils.createSkullItemStack("69b9a08d-4e89-4878-8be8-551caeacbf2a", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2ViZjkwNzQ5NGE5MzVlOTU1YmZjYWRhYjgxYmVhZmI5MGZiOWJlNDljNzAyNmJhOTdkNzk4ZDVmMWEyMyJ9fX0=", null), LocConf.load(LocConf.FILTERING_SCROLL_LEFT));
		ItemStack scrollRight = InventoryUtils.changeDisplayName(InventoryUtils.createSkullItemStack("15f49744-9b61-46af-b1c3-71c6261a0d0e", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWI2ZjFhMjViNmJjMTk5OTQ2NDcyYWVkYjM3MDUyMjU4NGZmNmY0ZTgzMjIxZTU5NDZiZDJlNDFiNWNhMTNiIn19fQ==", null), LocConf.load(LocConf.FILTERING_SCROLL_RIGHT));

		//basic settings
		for (int i = 0; i < 18; i++) {
			if (i == 2) {
				inv.setItem(i, extractDirection);
			} else if (i == 4) {
				inv.setItem(i, extractAmount);
			} else if (i == 6) {
				inv.setItem(i, extractCondition);
			} else {
				inv.setItem(i, glassPane);
			}
		}

		inv.setItem(18, filteringModeBlock);

		//filtering stuff
		if (pipe.getFilteringMode() == FilteringMode.BLOCK_ALL) {
			for (int i = 1; i < 9; i++) {
				inv.setItem(18 + i, barrier);
			}
		} else {
			inv.setItem(18 + 1, scrollLeft);
			inv.setItem(18 + 8, scrollRight);

			ItemData[] items = pipe.getFilteringItems();
			int indexWithScrollValue = scrollValue;
			for (int i = 2; i < 8; i++) {
				if (items[indexWithScrollValue] != null) {
					inv.setItem(18 + i, items[indexWithScrollValue].toItemStack());
				} else {
					inv.setItem(18 + i, null);
				}
				indexWithScrollValue++;
			}
		}

		p.openInventory(inv);

	}

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if (e.getInventory() != null && extractionPipeInventories.containsValue(e.getInventory())) {
			ExtractionPipe pipe = null;
			//get pipe with inventory
			for (ExtractionPipe ep : extractionPipeInventories.keySet()) {
				if (extractionPipeInventories.get(ep).equals(e.getInventory())) {
					pipe = ep;
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
			//clicked change extract direction
			if (e.getRawSlot() == 2) {
				e.setCancelled(true);

				saveExtractionPipeInv((Player) e.getWhoClicked(), e.getInventory());
				
				pipe.checkAndUpdateExtractDirection(true);

				// Update inv
				updateExtractionPipeInventory((Player) e.getWhoClicked(), pipe);
				return;
			}
			//clicked change extract amount
			if (e.getRawSlot() == 4) {
				e.setCancelled(true);

				saveExtractionPipeInv((Player) e.getWhoClicked(), e.getInventory());
				
				pipe.setExtractAmount(pipe.getExtractAmount().getNextAmount());

				// Update inv
				updateExtractionPipeInventory((Player) e.getWhoClicked(), pipe);
				return;
			}
			//clicked change extract condition
			if (e.getRawSlot() == 6) {
				e.setCancelled(true);

				saveExtractionPipeInv((Player) e.getWhoClicked(), e.getInventory());
				
				pipe.setExtractCondition(pipe.getExtractCondition().getNextCondition());

				// Update inv
				updateExtractionPipeInventory((Player) e.getWhoClicked(), pipe);
				return;
			}
			//clicked filtering mode wool
			if (e.getRawSlot() == 18) {
				e.setCancelled(true);

				pipe.setFilteringMode(pipe.getFilteringMode().getNextMode());

				// Update inv
				saveExtractionPipeInv((Player) e.getWhoClicked(), e.getInventory());
				updateExtractionPipeInventory((Player) e.getWhoClicked(), pipe);

				return;
			}
			//clicked scroll left
			if (e.getRawSlot() == 19) {
				e.setCancelled(true);

				saveExtractionPipeInv((Player) e.getWhoClicked(), e.getInventory());

				int scrollValue = scrollValues.get(pipe);
				if (scrollValue > 0) {
					scrollValue--;
				}
				scrollValues.put(pipe, scrollValue);

				updateExtractionPipeInventory((Player) e.getWhoClicked(), pipe);

				return;
			}
			//clicked scroll right
			if (e.getRawSlot() == 26) {
				e.setCancelled(true);

				saveExtractionPipeInv((Player) e.getWhoClicked(), e.getInventory());

				int scrollValue = scrollValues.get(pipe);
				if (scrollValue < GoldenPipe.ITEMS_PER_ROW - 6) {
					scrollValue++;
				}
				scrollValues.put(pipe, scrollValue);

				updateExtractionPipeInventory((Player) e.getWhoClicked(), pipe);

				return;
			}
		}
	}

	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		saveExtractionPipeInv((Player) e.getPlayer(), e.getInventory());
	}

	private void saveExtractionPipeInv(Player p, Inventory inv) {
		if (inv != null && extractionPipeInventories.containsValue(inv)) {
			ExtractionPipe pipe = null;
			//get pipe with inventory
			for (ExtractionPipe ep : extractionPipeInventories.keySet()) {
				if (extractionPipeInventories.get(ep).equals(inv)) {
					pipe = ep;
					break;
				}
			}
			ItemData[] items = pipe.getFilteringItems();
			int scrollValue = scrollValues.get(pipe);
			for (int i = 2; i < 8; i++) {
				ItemStack is = inv.getItem(18 + i);
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
					return;
				}
				scrollValue++;
			}
		}
	}

}
