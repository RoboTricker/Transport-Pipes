package de.robotricker.transportpipes.pipes.goldenpipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

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

import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.pipes.PipeDirection;
import de.robotricker.transportpipes.pipes.types.GoldenPipe;
import de.robotricker.transportpipes.pipeutils.InventoryUtils;
import de.robotricker.transportpipes.pipeutils.config.LocConf;

public class GoldenPipeInv implements Listener {

	private static HashMap<GoldenPipe, Inventory> pipe_invs = new HashMap<>();

	public static void updateGoldenPipeInventory(Player p, GoldenPipe pipe) {
		Inventory inv;
		if (pipe_invs.containsKey(pipe)) {
			inv = pipe_invs.get(pipe);
		} else {
			inv = Bukkit.createInventory(null, 6 * 9, LocConf.load(LocConf.GOLDENPIPE_TITLE));
			pipe_invs.put(pipe, inv);
		}

		ItemStack glass_pane = InventoryUtils.changeDisplayNameAndLore(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), String.valueOf(ChatColor.RESET));
		Collection<PipeDirection> pipeConnections = pipe.getAllConnections();

		Material material;
		String filteringMode;
		if (pipe.isIgnoreNBT()) {
			material = Material.WOOL;
			filteringMode = LocConf.load(LocConf.GOLDENPIPE_FILTERING_IGNORENBT);
		} else {
			material = Material.STAINED_GLASS;
			filteringMode = LocConf.load(LocConf.GOLDENPIPE_FILTERING_CHECKNBT);
		}

		for (int i = 0; i < 6; i++) {
			GoldenPipeColor gpc = GoldenPipeColor.values()[i];
			inv.setItem(i * 9, InventoryUtils.changeDisplayNameAndLore(new ItemStack(material, 1, gpc.getGlassPaneDamage()), LocConf.load(gpc.getLocConfKey()), filteringMode, LocConf.load(LocConf.GOLDENPIPE_FILTERING_CLICKTOCHANGE)));
		}

		for (int line = 0; line < 6; line++) {
			if (!pipeConnections.contains(PipeDirection.values()[line])) {
				for (int i = 1; i < 9; i++) {
					inv.setItem(line * 9 + i, glass_pane);
				}
			} else {
				ItemData[] items = pipe.getOutputItems(PipeDirection.fromID(line));
				for (int i = 1; i < 9; i++) {
					if (items[i - 1] != null) {
						inv.setItem(line * 9 + i, items[i - 1].toItemStack());
					} else {
						inv.setItem(line * 9 + i, null);
					}
				}
			}
		}

		p.openInventory(inv);
	}

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if (e.getClickedInventory() != null && pipe_invs.containsValue(e.getClickedInventory())) {
			//clicked on glass pane
			if (InventoryUtils.hasDisplayName(e.getCurrentItem(), String.valueOf(ChatColor.RESET))) {
				e.setCancelled(true);
			}
			//clicked on wool block
			if (e.getRawSlot() >= 0 && e.getRawSlot() <= 5 * 9 && e.getRawSlot() % 9 == 0) {
				if (e.getClickedInventory() != null && pipe_invs.containsValue(e.getClickedInventory())) {
					GoldenPipe pipe = null;
					//get pipe with inventory
					for (GoldenPipe gp : pipe_invs.keySet()) {
						if (pipe_invs.get(gp).equals(e.getClickedInventory())) {
							pipe = gp;
							break;
						}
					}
					pipe.setIgnoreNBT(!pipe.isIgnoreNBT());
					// Update inv
					saveGoldenPipeInv((Player) e.getWhoClicked(), e.getClickedInventory());
					updateGoldenPipeInventory((Player) e.getWhoClicked(), pipe);
				}
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		saveGoldenPipeInv((Player) e.getPlayer(), e.getInventory());
	}

	private void saveGoldenPipeInv(Player p, Inventory inv) {
		if (inv != null && pipe_invs.containsValue(inv)) {
			GoldenPipe pipe = null;
			//get pipe with inventory
			for (GoldenPipe gp : pipe_invs.keySet()) {
				if (pipe_invs.get(gp).equals(inv)) {
					pipe = gp;
					break;
				}
			}
			//cache new items in golden pipe
			linefor: for (int line = 0; line < 6; line++) {
				List<ItemData> items = new ArrayList<>();
				for (int i = 1; i < 9; i++) {
					ItemStack is = inv.getItem(line * 9 + i);
					//make sure the glass pane won't be saved
					if (is != null && !InventoryUtils.hasDisplayName(is, String.valueOf(ChatColor.RESET))) {
						items.add(new ItemData(is));
						if (is.getAmount() > 1) {
							//drop overflow items (only 1 item of each type is saved)
							ItemStack clone = is.clone();
							clone.setAmount(is.getAmount() - 1);
							p.getWorld().dropItem(p.getLocation(), clone);

							//cloned item which will be saved
							ItemStack clone2 = is.clone();
							clone2.setAmount(1);
							inv.setItem(line * 9, clone2);
						}
					} else if (InventoryUtils.hasDisplayName(is, String.valueOf(ChatColor.RESET))) {
						//skip this save-sequenz if this line is not available (not a pipe or block as neighbor)
						continue linefor;
					}
				}
				pipe.changeOutputItems(PipeDirection.fromID(line), items);
			}
		}
	}

}
