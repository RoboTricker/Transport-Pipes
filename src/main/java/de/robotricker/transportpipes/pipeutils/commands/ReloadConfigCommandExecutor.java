package de.robotricker.transportpipes.pipeutils.commands;

import org.bukkit.command.CommandSender;

import de.robotricker.transportpipes.TransportPipes;

public class ReloadConfigCommandExecutor implements PipesCommandExecutor {

	@Override
	public boolean onCommand(CommandSender cs) {
		if (!cs.hasPermission(TransportPipes.instance.generalConf.getPermissionReload())) {
			return false;
		}

		TransportPipes.instance.generalConf.reload();
		TransportPipes.instance.locConf.reload();
		
		cs.sendMessage("§cConfig reloaded");
		return true;
	}

}
