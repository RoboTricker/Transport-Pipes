package de.robotricker.transportpipes.manager.settings;

import java.util.ArrayList;
import java.util.List;

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

import de.robotricker.transportpipes.TransportPipes;

public class SettingsInv implements Listener, CommandExecutor {

	public static List<String> lore = new ArrayList<String>();

	static {
		List<String> loreRaw = TransportPipes.instance.getConfig().getStringList("settingsinv.viewDistanceLores");
		for (String s : loreRaw) {
			lore.add(ChatColor.translateAlternateColorCodes('&', s));
		}
	}

	public static void updateSettingsInventory(Inventory inv, Player viewer) {
		if (inv == null) {
			inv = Bukkit.createInventory(null, 9, ChatColor.translateAlternateColorCodes('&', TransportPipes.instance.getConfig().getString("settingsinv.nameinv")));
			viewer.openInventory(inv);
		}

		ItemStack glassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
		ItemStack decreaseBtn = new ItemStack(Material.DOUBLE_PLANT, 1, (short) 0);
		SettingsUtils.changeDisplayNameAndLore(decreaseBtn, ChatColor.translateAlternateColorCodes('&', TransportPipes.instance.getConfig().getString("settingsinv.decrease")));
		ItemStack increaseBtn = new ItemStack(Material.DOUBLE_PLANT, 1, (short) 0);
		SettingsUtils.changeDisplayNameAndLore(increaseBtn, ChatColor.translateAlternateColorCodes('&', TransportPipes.instance.getConfig().getString("settingsinv.increase")));
		ItemStack eye = new ItemStack(Material.EYE_OF_ENDER, SettingsManager.getViewDistance(viewer), (short) 0);
		SettingsUtils.changeDisplayNameAndLoreConfig(eye, ChatColor.translateAlternateColorCodes('&', TransportPipes.instance.getConfig().getString("settingsinv.viewDistanceName")) + SettingsManager.getViewDistance(viewer), lore);

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
		if (e.getClickedInventory() != null && e.getClickedInventory().getName().equals(ChatColor.translateAlternateColorCodes('&', TransportPipes.instance.getConfig().getString("settingsinv.nameinv")))) {
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
