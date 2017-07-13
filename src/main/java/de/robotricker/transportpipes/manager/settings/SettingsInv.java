package de.robotricker.transportpipes.manager.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.TransportPipes.BlockLoc;
import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipeutils.commands.PipesCommandExecutor;
import de.robotricker.transportpipes.protocol.pipemodels.PipeManager;
import de.robotricker.transportpipes.protocol.pipemodels.vanilla.utils.VanillaPipeManager;

public class SettingsInv implements Listener, PipesCommandExecutor {

	public static List<String> lore = new ArrayList<String>();

	static {
		List<String> loreRaw = TransportPipes.instance.getConfig().getStringList("settingsinv.viewDistanceLores");
		for (String s : loreRaw) {
			lore.add(ChatColor.translateAlternateColorCodes('&', s));
		}
	}

	public static void updateSettingsInventory(Inventory inv, Player viewer) {
		if (inv == null) {
			inv = Bukkit.createInventory(null, 2 * 9, TransportPipes.getFormattedConfigString("settingsinv.nameinv"));
			viewer.openInventory(inv);
		}

		ItemStack glassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
		ItemStack decreaseBtn = new ItemStack(Material.DOUBLE_PLANT, 1, (short) 0);
		SettingsUtils.changeDisplayNameAndLore(decreaseBtn, TransportPipes.getFormattedConfigString("settingsinv.decrease"));
		ItemStack increaseBtn = new ItemStack(Material.DOUBLE_PLANT, 1, (short) 0);
		SettingsUtils.changeDisplayNameAndLore(increaseBtn, TransportPipes.getFormattedConfigString("settingsinv.increase"));
		ItemStack eye = new ItemStack(Material.EYE_OF_ENDER, SettingsManager.getViewDistance(viewer), (short) 0);
		SettingsUtils.changeDisplayNameAndLoreConfig(eye, TransportPipes.getFormattedConfigString("settingsinv.viewDistanceName") + SettingsManager.getViewDistance(viewer), lore);

		populateInventoryLine(inv, 0, glassPane, glassPane, decreaseBtn, glassPane, eye, glassPane, increaseBtn, glassPane, glassPane);

		PipeManager pm = TransportPipes.armorStandProtocol.getPlayerPipeManager(viewer);
		ItemStack currentSystem = pm.getRepresentationItem();
		SettingsUtils.changeDisplayNameAndLore(currentSystem, "§6Current Pipe Render System: §b" + pm.getPipeRenderSystemName(), "§7Click to switch between Vanilla", "§7and Modelled Render Systems", "§7The Modelled Render System uses a resourcepack", "§7but looks much better. The Vanilla Render System", "§7uses the Vanilla Minecraft textures.");

		populateInventoryLine(inv, 1, glassPane, glassPane, glassPane, glassPane, currentSystem, glassPane, glassPane, glassPane, glassPane);

		viewer.updateInventory();

	}

	private static void populateInventoryLine(Inventory inv, int row, ItemStack... items) {
		for (int i = 0; i < 9; i++) {
			if (items.length > i && items[i] != null) {
				ItemStack is = items[i];
				inv.setItem(row * 9 + i, is);
			}
		}
	}

	@EventHandler
	public void onInvClick(InventoryClickEvent e) {
		if (e.getClickedInventory() != null && e.getClickedInventory().getName().equals(TransportPipes.getFormattedConfigString("settingsinv.nameinv"))) {
			Player p = (Player) e.getWhoClicked();
			e.setCancelled(true);
			if (e.getAction() == InventoryAction.PICKUP_ALL || e.getAction() == InventoryAction.PICKUP_HALF) {
				if (e.getRawSlot() == 2) {
					//decrease render distance
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
					//increase render distance
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
				if (e.getRawSlot() == 13) {
					//change render system

					Map<BlockLoc, Pipe> pipeMap = TransportPipes.getPipeMap(p.getWorld());
					if (pipeMap != null) {
						synchronized (pipeMap) {
							for (Pipe pipe : pipeMap.values()) {
								TransportPipes.pipePacketManager.despawnPipe(p, pipe);
							}
						}
					}

					PipeManager beforePm = TransportPipes.armorStandProtocol.getPlayerPipeManager(p);
					if (beforePm instanceof VanillaPipeManager) {
						TransportPipes.armorStandProtocol.setPlayerPipeManager(p, TransportPipes.modelledPipeManager);
						TransportPipes.modelledPipeManager.initPlayer(p);
					} else {
						TransportPipes.armorStandProtocol.setPlayerPipeManager(p, TransportPipes.vanillaPipeManager);
						TransportPipes.vanillaPipeManager.initPlayer(p);
					}
					
					if (pipeMap != null) {
						synchronized (pipeMap) {
							for (Pipe pipe : pipeMap.values()) {
								TransportPipes.pipePacketManager.spawnPipe(p, pipe);
							}
						}
					}

					updateSettingsInventory(e.getClickedInventory(), p);
					p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
				}
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender cs) {
		if (cs instanceof Player) {
			updateSettingsInventory(null, (Player) cs);
		}
		return true;
	}

}
