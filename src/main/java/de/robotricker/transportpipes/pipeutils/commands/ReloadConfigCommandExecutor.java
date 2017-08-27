package de.robotricker.transportpipes.pipeutils.commands;

import org.bukkit.command.CommandSender;

import de.robotricker.transportpipes.TransportPipes;

public class ReloadConfigCommandExecutor implements PipesCommandExecutor {

	@Override
	public boolean onCommand(CommandSender cs, String[] args) {
		if (!cs.hasPermission("transportpipes.reload")) {
			return false;
		}

		TransportPipes.instance.generalConf.reload();
		TransportPipes.instance.locConf.reload();
		
		cs.sendMessage("§cConfig reloaded");
		return true;
	}

}
