package de.robotricker.transportpipes.pipeutils.commands;

import org.bukkit.command.CommandSender;

import de.robotricker.transportpipes.TransportPipes;

public class ReloadPipesCommandExecutor implements PipesCommandExecutor {

	@Override
	public boolean onCommand(CommandSender cs) {
		if (!cs.hasPermission(TransportPipes.instance.generalConf.getPermissionReload())) {
			return false;
		}
		//TODO reload!

		cs.sendMessage("§cFeature doesn't work yet");
		return true;
	}

}