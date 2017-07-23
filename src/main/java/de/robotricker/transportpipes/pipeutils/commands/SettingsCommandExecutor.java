package de.robotricker.transportpipes.pipeutils.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.robotricker.transportpipes.settings.SettingsInv;

public class SettingsCommandExecutor implements PipesCommandExecutor {

	@Override
	public boolean onCommand(CommandSender cs, String[] args) {

		if (cs instanceof Player) {
			SettingsInv.updateSettingsInventory(null, (Player) cs);
		} else {
			cs.sendMessage("§cYou're not a player!");
		}

		return true;
	}

}
