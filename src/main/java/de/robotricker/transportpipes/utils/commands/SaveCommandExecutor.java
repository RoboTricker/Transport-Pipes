package de.robotricker.transportpipes.utils.commands;

import org.bukkit.command.CommandSender;
import de.robotricker.transportpipes.TransportPipes;

public class SaveCommandExecutor implements PipesCommandExecutor {

	@Override
	public boolean onCommand(final CommandSender cs, final String[] args) {

		if (!cs.hasPermission("transportpipes.save")) {
			return false;
		}

		TransportPipes.instance.savingManager.saveDuctsAsync(true);

		cs.sendMessage("§cPipes saved");

		return true;
	}

}
