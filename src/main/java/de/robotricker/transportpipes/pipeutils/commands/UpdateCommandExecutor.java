package de.robotricker.transportpipes.pipeutils.commands;

import org.bukkit.command.CommandSender;

import de.robotricker.transportpipes.TransportPipes;

public class UpdateCommandExecutor implements PipesCommandExecutor {

	@Override
	public boolean onCommand(CommandSender cs) {

		if (!cs.hasPermission(TransportPipes.instance.getConfig().getString("permissions.update", "tp.update"))) {
			return false;
		}

		TransportPipes.updateManager.updatePlugin(cs);

		return true;
	}

}
