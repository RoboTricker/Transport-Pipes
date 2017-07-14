package de.robotricker.transportpipes.pipeutils.commands;

import org.bukkit.command.CommandSender;

import de.robotricker.transportpipes.TransportPipes;

public class UpdateCommandExecutor implements PipesCommandExecutor {

	@Override
	public boolean onCommand(CommandSender cs) {

		if (!cs.hasPermission(TransportPipes.instance.generalConf.getPermissionUpdate())) {
			return false;
		}

		TransportPipes.updateManager.updatePlugin(cs);

		return true;
	}

}
