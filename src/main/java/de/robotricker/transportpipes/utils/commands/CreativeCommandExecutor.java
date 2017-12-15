package de.robotricker.transportpipes.utils.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.duct.pipe.utils.PipeColor;
import de.robotricker.transportpipes.duct.pipe.utils.PipeType;
import de.robotricker.transportpipes.utils.config.LocConf;
import de.robotricker.transportpipes.utils.staticutils.DuctItemUtils;

public class CreativeCommandExecutor implements PipesCommandExecutor {

	@Override
	public boolean onCommand(CommandSender cs, String[] args) {

		if (!cs.hasPermission("transportpipes.creative")) {
			return false;
		}

		if (cs instanceof Player) {
			Inventory inv = Bukkit.createInventory(null, 9 * 3, LocConf.load(LocConf.CREATIVE_TITLE));

			int i = 0;
			List<ItemStack> ductItems = DuctItemUtils.getAllDuctItems();
			for (ItemStack is : ductItems) {
				ItemStack clonedIs = is.clone();
				clonedIs.setAmount(16);
				inv.setItem(i, clonedIs);
				i++;
			}
			inv.setItem(i++, DuctItemUtils.getClonedWrenchItem());

			((Player) cs).openInventory(inv);
		} else {
			cs.sendMessage("§cYou're not a player!");
		}

		return true;
	}

}
