package de.robotricker.transportpipes.pipes.extractionpipe;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.pipes.types.ExtractionPipe;
import de.robotricker.transportpipes.pipeutils.InventoryUtils;
import de.robotricker.transportpipes.pipeutils.config.LocConf;

public class ExtractionPipeInv implements Listener {

	private static HashMap<ExtractionPipe, Inventory> extractionPipeInventories = new HashMap<>();

	public static void updateExtractionPipeInventory(Player p, ExtractionPipe pipe) {
		Inventory inv;
		if (extractionPipeInventories.containsKey(pipe)) {
			inv = extractionPipeInventories.get(pipe);
		} else {
			inv = Bukkit.createInventory(null, 9, LocConf.load(LocConf.EXTRACTIONPIPE_TITLE));
			extractionPipeInventories.put(pipe, inv);
		}

		String extractDirectionDisplayName = null;
		if (pipe.getExtractDirection() == null) {
			extractDirectionDisplayName = LocConf.load(LocConf.EXTRACTIONPIPE_DIRECTION_DONTEXTRACT);
		} else {
			extractDirectionDisplayName = String.format(LocConf.load(LocConf.EXTRACTIONPIPE_DIRECTION_TEXT), pipe.getExtractDirection().name());
		}
		ItemStack extractDirection = InventoryUtils.changeDisplayNameAndLore(new ItemStack(Material.TRIPWIRE_HOOK), extractDirectionDisplayName, LocConf.load(LocConf.EXTRACTIONPIPE_DIRECTION_CLICKTOCHANGE));
		ItemStack extractCondition = InventoryUtils.changeDisplayNameAndLore(pipe.getExtractCondition().getDisplayItem(), LocConf.load(pipe.getExtractCondition().getLocConfKey()), LocConf.load(LocConf.EXTRACTIONPIPE_CONDITION_CLICKTOCHANGE));
		ItemStack glassPane = InventoryUtils.changeDisplayNameAndLore(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15), String.valueOf(ChatColor.RESET));

		for (int i = 0; i < 9; i++) {
			if (i == 2) {
				inv.setItem(i, extractDirection);
			} else if (i == 6) {
				inv.setItem(i, extractCondition);
			} else {
				inv.setItem(i, glassPane);
			}
		}

		p.openInventory(inv);

	}

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if (e.getClickedInventory() != null && extractionPipeInventories.containsValue(e.getClickedInventory())) {
			ExtractionPipe pipe = null;
			//get pipe with inventory
			for (ExtractionPipe ep : extractionPipeInventories.keySet()) {
				if (extractionPipeInventories.get(ep).equals(e.getClickedInventory())) {
					pipe = ep;
					break;
				}
			}
			if (pipe == null) {
				return;
			}
			//clicked on glass pane
			if (isGlassItemOrBarrier(e.getCurrentItem())) {
				e.setCancelled(true);
				return;
			}
			//clicked change extract direction
			if (e.getRawSlot() == 2) {
				e.setCancelled(true);

				pipe.checkAndUpdateExtractDirection(true);

				// Update inv
				updateExtractionPipeInventory((Player) e.getWhoClicked(), pipe);
				return;
			}
			//clicked change extract condition
			if (e.getRawSlot() == 6) {
				e.setCancelled(true);

				pipe.setExtractCondition(pipe.getExtractCondition().getNextCondition());

				// Update inv
				updateExtractionPipeInventory((Player) e.getWhoClicked(), pipe);
				return;
			}
		}
	}

	private boolean isGlassItemOrBarrier(ItemStack is) {
		return InventoryUtils.hasDisplayName(is, String.valueOf(ChatColor.RESET));
	}

}
