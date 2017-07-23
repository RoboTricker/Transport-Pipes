package de.robotricker.transportpipes.pipeutils.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipes.colored.PipeColor;
import de.robotricker.transportpipes.pipeutils.PipeItemUtils;
import de.robotricker.transportpipes.pipeutils.config.LocConf;

public class CreativeCommandExecutor implements PipesCommandExecutor {

	@Override
	public boolean onCommand(CommandSender cs, String[] args) {

		if (!cs.hasPermission(TransportPipes.instance.generalConf.getPermissionCreative())) {
			return false;
		}

		if (cs instanceof Player) {
			Inventory inv = Bukkit.createInventory(null, 9 * 3, LocConf.load(LocConf.CREATIVE_TITLE));

			int i = 0;
			for (PipeColor pc : PipeColor.values()) {
				ItemStack is = PipeItemUtils.getPipeItem(PipeType.COLORED, pc);
				inv.setItem(i, is);
				i++;
			}

			i = 9;
			for (PipeType pt : PipeType.values()) {
				if (pt == PipeType.COLORED) {
					continue;
				}
				ItemStack is = PipeItemUtils.getPipeItem(pt, null);
				inv.setItem(i, is);
				i++;
			}

			inv.setItem(18, PipeItemUtils.getWrenchItem());

			((Player) cs).openInventory(inv);
		} else {
			cs.sendMessage("§cYou're not a player!");
		}

		return true;
	}

}
