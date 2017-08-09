package de.robotricker.transportpipes.settings;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipeutils.InventoryUtils;
import de.robotricker.transportpipes.pipeutils.config.LocConf;
import de.robotricker.transportpipes.pipeutils.config.PlayerSettingsConf;
import de.robotricker.transportpipes.rendersystem.PipeRenderSystem;

public class SettingsInv implements Listener {

	public static void updateSettingsInventory(Inventory inv, Player viewer) {
		if (inv == null) {
			inv = Bukkit.createInventory(null, 2 * 9, LocConf.load(LocConf.SETTINGS_TITLE));
			viewer.openInventory(inv);
		}

		PlayerSettingsConf psc = SettingsUtils.loadPlayerSettings(viewer);

		//Render Distance setting
		ItemStack glassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
		ItemStack decreaseBtn = new ItemStack(Material.DOUBLE_PLANT, 1, (short) 0);
		InventoryUtils.changeDisplayNameAndLore(decreaseBtn, LocConf.load(LocConf.SETTINGS_RENDERDISTANCE_DECREASE));
		ItemStack increaseBtn = new ItemStack(Material.DOUBLE_PLANT, 1, (short) 0);
		InventoryUtils.changeDisplayNameAndLore(increaseBtn, LocConf.load(LocConf.SETTINGS_RENDERDISTANCE_INCREASE));
		ItemStack eye = new ItemStack(Material.EYE_OF_ENDER, psc.getRenderDistance(), (short) 0);
		InventoryUtils.changeDisplayNameAndLoreConfig(eye, String.format(LocConf.load(LocConf.SETTINGS_RENDERDISTANCE_TITLE), psc.getRenderDistance()), LocConf.loadStringList(LocConf.SETTINGS_RENDERDISTANCE_DESCRIPTION));

		populateInventoryLine(inv, 0, glassPane, glassPane, decreaseBtn, glassPane, eye, glassPane, increaseBtn, glassPane, glassPane);

		//Render System setting
		PipeRenderSystem pm = TransportPipes.armorStandProtocol.getPlayerPipeRenderSystem(viewer);
		ItemStack currentSystem = pm.getRepresentationItem().clone();
		InventoryUtils.changeDisplayNameAndLoreConfig(currentSystem, String.format(LocConf.load(LocConf.SETTINGS_RENDERSYSTEM_TITLE), pm.getPipeRenderSystemName()), LocConf.loadStringList(LocConf.SETTINGS_RENDERSYSTEM_DESCRIPTION));

		//Show items setting
		boolean showItems = TransportPipes.armorStandProtocol.isPlayerShowItems(viewer);
		ItemStack currentShowItems;
		if (showItems) {
			currentShowItems = new ItemStack(Material.GLASS);
			InventoryUtils.changeDisplayNameAndLoreConfig(currentShowItems, String.format(LocConf.load(LocConf.SETTINGS_SHOWITEMS_TITLE), LocConf.load(LocConf.SETTINGS_SHOWITEMS_SHOW)), LocConf.loadStringList(LocConf.SETTINGS_SHOWITEMS_DESCRIPTION));
		} else {
			currentShowItems = new ItemStack(Material.BARRIER);
			InventoryUtils.changeDisplayNameAndLoreConfig(currentShowItems, String.format(LocConf.load(LocConf.SETTINGS_SHOWITEMS_TITLE), LocConf.load(LocConf.SETTINGS_SHOWITEMS_HIDE)), LocConf.loadStringList(LocConf.SETTINGS_SHOWITEMS_DESCRIPTION));
		}

		populateInventoryLine(inv, 1, glassPane, glassPane, glassPane, currentSystem, glassPane, currentShowItems, glassPane, glassPane, glassPane);

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
		if (e.getClickedInventory() != null && e.getClickedInventory().getName().equals(LocConf.load(LocConf.SETTINGS_TITLE))) {
			Player p = (Player) e.getWhoClicked();
			PlayerSettingsConf psc = SettingsUtils.loadPlayerSettings(p);

			e.setCancelled(true);
			if (e.getAction() == InventoryAction.PICKUP_ALL || e.getAction() == InventoryAction.PICKUP_HALF) {
				if (e.getRawSlot() == 2) {
					//decrease render distance
					int before = psc.getRenderDistance();
					int after = before - 1;
					if (after >= 1) {
						p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
						psc.setRenderDistance(after);
						updateSettingsInventory(e.getClickedInventory(), p);
					} else {
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BASS, 1f, 1f);
					}
				}
				if (e.getRawSlot() == 6) {
					//increase render distance
					int before = psc.getRenderDistance();
					int after = before + 1;
					if (after <= 64) {
						p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
						psc.setRenderDistance(after);
						updateSettingsInventory(e.getClickedInventory(), p);
					} else {
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BASS, 1f, 1f);
					}
				}
				if (e.getRawSlot() == 12) {
					//change render system

					int renderSystemId = TransportPipes.armorStandProtocol.getPlayerPipeRenderSystem(p).getRenderSystemId();
					renderSystemId++;
					renderSystemId %= TransportPipes.instance.getPipeRenderSystems().size();
					PipeRenderSystem newRenderSystem = PipeRenderSystem.getRenderSystemFromId(renderSystemId);

					TransportPipes.armorStandProtocol.changePipeRenderSystem(p, newRenderSystem);

					updateSettingsInventory(e.getClickedInventory(), p);
					p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
				}
				if (e.getRawSlot() == 14) {
					//change show items
					boolean showItems = TransportPipes.armorStandProtocol.isPlayerShowItems(p);
					TransportPipes.armorStandProtocol.changeShowItems(p, !showItems);

					updateSettingsInventory(e.getClickedInventory(), p);
					p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
				}
			}
		}
	}

	@EventHandler
	public void onJoin(final PlayerJoinEvent e) {
		Bukkit.getScheduler().runTaskLater(TransportPipes.instance, new Runnable() {

			@Override
			public void run() {
				TransportPipes.armorStandProtocol.getPlayerPipeRenderSystem(e.getPlayer()).initPlayer(e.getPlayer());
			}
		}, 40L);
	}

}
