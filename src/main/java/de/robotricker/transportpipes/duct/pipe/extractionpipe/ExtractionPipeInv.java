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

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctInv;
import de.robotricker.transportpipes.duct.pipe.ExtractionPipe;
import de.robotricker.transportpipes.duct.pipe.GoldenPipe;
import de.robotricker.transportpipes.duct.pipe.utils.FilteringMode;
import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.utils.config.LocConf;
import de.robotricker.transportpipes.utils.staticutils.InventoryUtils;

public class ExtractionPipeInv extends DuctInv {

	private int scrollValue = 0;

	public ExtractionPipeInv(Duct duct) {
		super(duct, LocConf.load(LocConf.EXTRACTIONPIPE_TITLE), 27);
	}

	@Override
	protected void populateInventory(Player p) {
		ExtractionPipe ep = (ExtractionPipe) duct;

		String extractDirectionDisplayName = null;
		if (ep.getExtractDirection() == null) {
			extractDirectionDisplayName = LocConf.load(LocConf.EXTRACTIONPIPE_DIRECTION_DONTEXTRACT);
		} else {
			extractDirectionDisplayName = String.format(LocConf.load(LocConf.EXTRACTIONPIPE_DIRECTION_TEXT), ep.getExtractDirection().name());
		}
		ItemStack extractDirection = InventoryUtils.changeDisplayNameAndLore(new ItemStack(Material.TRIPWIRE_HOOK), extractDirectionDisplayName, LocConf.load(LocConf.EXTRACTIONPIPE_DIRECTION_CLICKTOCHANGE));
		ItemStack extractCondition = InventoryUtils.changeDisplayNameAndLore(ep.getExtractCondition().getDisplayItem(), LocConf.load(ep.getExtractCondition().getLocConfKey()), LocConf.load(LocConf.EXTRACTIONPIPE_CONDITION_CLICKTOCHANGE));
		ItemStack extractAmount = InventoryUtils.changeDisplayNameAndLore(ep.getExtractAmount().getDisplayItem(), LocConf.load(ep.getExtractAmount().getLocConfKey()), LocConf.load(LocConf.EXTRACTIONPIPE_AMOUNT_CLICKTOCHANGE));
		ItemStack glassPane = InventoryUtils.changeDisplayNameAndLore(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15), String.valueOf(ChatColor.RESET));
		String filteringModeText = LocConf.load(ep.getFilteringMode().getLocConfKey());
		ItemStack filteringModeBlock = InventoryUtils.changeDisplayNameAndLore(new ItemStack(Material.WOOL, 1, (short) 0), filteringModeText, LocConf.load(LocConf.FILTERING_CLICKTOCHANGE));
		ItemStack barrier = InventoryUtils.changeDisplayNameAndLore(new ItemStack(Material.BARRIER, 1), String.valueOf(ChatColor.RESET));
		ItemStack scrollLeft = InventoryUtils.changeDisplayName(InventoryUtils.createSkullItemStack("69b9a08d-4e89-4878-8be8-551caeacbf2a", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2ViZjkwNzQ5NGE5MzVlOTU1YmZjYWRhYjgxYmVhZmI5MGZiOWJlNDljNzAyNmJhOTdkNzk4ZDVmMWEyMyJ9fX0=", null), LocConf.load(LocConf.FILTERING_SCROLL_LEFT));
		ItemStack scrollRight = InventoryUtils.changeDisplayName(InventoryUtils.createSkullItemStack("15f49744-9b61-46af-b1c3-71c6261a0d0e", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWI2ZjFhMjViNmJjMTk5OTQ2NDcyYWVkYjM3MDUyMjU4NGZmNmY0ZTgzMjIxZTU5NDZiZDJlNDFiNWNhMTNiIn19fQ==", null), LocConf.load(LocConf.FILTERING_SCROLL_RIGHT));

		// basic settings
		for (int i = 0; i < 18; i++) {
			if (i == 2) {
				inventory.setItem(i, extractDirection);
			} else if (i == 4) {
				inventory.setItem(i, extractAmount);
			} else if (i == 6) {
				inventory.setItem(i, extractCondition);
			} else {
				inventory.setItem(i, glassPane);
			}
		}

		inventory.setItem(18, filteringModeBlock);

		// filtering stuff
		if (ep.getFilteringMode() == FilteringMode.BLOCK_ALL) {
			for (int i = 1; i < 9; i++) {
				inventory.setItem(18 + i, barrier);
			}
		} else {
			inventory.setItem(18 + 1, scrollLeft);
			inventory.setItem(18 + 8, scrollRight);

			ItemData[] items = ep.getFilteringItems();
			int indexWithScrollValue = scrollValue;
			for (int i = 2; i < 8; i++) {
				if (items[indexWithScrollValue] != null) {
					inventory.setItem(18 + i, items[indexWithScrollValue].toItemStack());
				} else {
					inventory.setItem(18 + i, null);
				}
				indexWithScrollValue++;
			}
		}
	}

	@Override
	protected boolean notifyInvClick(Player p, int rawSlot) {
		ExtractionPipe ep = (ExtractionPipe) duct;
		boolean cancelled = false;

		// clicked change extract direction
		if (rawSlot == 2) {
			cancelled = true;

			notifyInvSave(p);

			ep.checkAndUpdateExtractDirection(true);

			// Update inv
			openOrUpdateInventory(p);
			return cancelled;
		}
		// clicked change extract amount
		if (rawSlot == 4) {
			cancelled = true;

			notifyInvSave(p);

			ep.setExtractAmount(ep.getExtractAmount().getNextAmount());

			// Update inv
			openOrUpdateInventory(p);
			return cancelled;
		}
		// clicked change extract condition
		if (rawSlot == 6) {
			cancelled = true;

			notifyInvSave(p);

			ep.setExtractCondition(ep.getExtractCondition().getNextCondition());

			// Update inv
			openOrUpdateInventory(p);
			return cancelled;
		}
		// clicked filtering mode wool
		if (rawSlot == 18) {
			cancelled = true;

			ep.setFilteringMode(ep.getFilteringMode().getNextMode());

			// Update inv
			notifyInvSave(p);
			openOrUpdateInventory(p);

			return cancelled;
		}
		// clicked scroll left
		if (rawSlot == 19) {
			cancelled = true;

			notifyInvSave(p);

			if (scrollValue > 0) {
				scrollValue--;
			}

			openOrUpdateInventory(p);

			return cancelled;
		}
		// clicked scroll right
		if (rawSlot == 26) {
			cancelled = true;

			notifyInvSave(p);

			if (scrollValue < GoldenPipe.ITEMS_PER_ROW - 6) {
				scrollValue++;
			}

			openOrUpdateInventory(p);

			return cancelled;
		}

		return cancelled;
	}

	@Override
	protected void notifyInvSave(Player p) {
		ExtractionPipe ep = (ExtractionPipe) duct;

		ItemData[] items = ep.getFilteringItems();
		int scrollValueTemp = scrollValue;
		for (int i = 2; i < 8; i++) {
			ItemStack is = inventory.getItem(18 + i);
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
				return;
			}
			scrollValueTemp++;
		}
	}

}
