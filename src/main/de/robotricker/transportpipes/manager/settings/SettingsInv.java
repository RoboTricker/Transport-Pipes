package main.de.robotricker.transportpipes.manager.settings;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SettingsInv implements Listener, CommandExecutor {

	public static void updateSettingsInventory(Inventory inv, Player viewer) {
		if (inv == null) {
			inv = Bukkit.createInventory(null, 9, ChatColor.RESET + "Player Settings");
			viewer.openInventory(inv);
		}

		ItemStack glassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
		ItemStack decreaseBtn = new ItemStack(Material.DOUBLE_PLANT, 1, (short) 0);
		SettingsUtils.changeDisplayNameAndLore(decreaseBtn, ChatColor.GOLD + "Decrease");
		ItemStack increaseBtn = new ItemStack(Material.DOUBLE_PLANT, 1, (short) 0);
		SettingsUtils.changeDisplayNameAndLore(increaseBtn, ChatColor.GOLD + "Increase");
		ItemStack eye = new ItemStack(Material.EYE_OF_ENDER, SettingsManager.getViewDistance(viewer), (short) 0);
		SettingsUtils.changeDisplayNameAndLore(eye, ChatColor.GOLD + "View Distance: " + SettingsManager.getViewDistance(viewer), "", ChatColor.GRAY + "This represents the Radius in Blocks", ChatColor.GRAY + "in which you can see the pipes.", ChatColor.GRAY + "If you have too less FPS, decrease this option.");

		inv.setItem(0, glassPane);
		inv.setItem(1, glassPane);
		inv.setItem(2, decreaseBtn);
		inv.setItem(3, glassPane);
		inv.setItem(4, eye);
		inv.setItem(5, glassPane);
		inv.setItem(6, increaseBtn);
		inv.setItem(7, glassPane);
		inv.setItem(8, glassPane);

		viewer.updateInventory();

	}

	@EventHandler
	public void onInvClick(InventoryClickEvent e) {
		if (e.getClickedInventory() != null && e.getClickedInventory().getName().equals(ChatColor.RESET + "Player Settings")) {
			Player p = (Player) e.getWhoClicked();
			e.setCancelled(true);
			if (e.getAction() == InventoryAction.PICKUP_ALL || e.getAction() == InventoryAction.PICKUP_HALF) {
				if (e.getRawSlot() == 2) {
					//decrease
					int before = SettingsManager.getViewDistance(p);
					int after = before - 1;
					if (after >= 1) {
						p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
						SettingsManager.saveViewDistance(p, after);
						updateSettingsInventory(e.getClickedInventory(), p);
					} else {
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BASS, 1f, 1f);
					}
				}
				if (e.getRawSlot() == 6) {
					//increase
					int before = SettingsManager.getViewDistance(p);
					int after = before + 1;
					if (after <= 64) {
						p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
						SettingsManager.saveViewDistance(p, after);
						updateSettingsInventory(e.getClickedInventory(), p);
					} else {
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BASS, 1f, 1f);
					}
				}
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {

		if (cs instanceof Player) {
			updateSettingsInventory(null, (Player) cs);
		}

		return true;
	}

}
