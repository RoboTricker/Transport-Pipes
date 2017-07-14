package de.robotricker.transportpipes.pipeutils.commands;

import org.bukkit.command.CommandSender;

import de.robotricker.transportpipes.TransportPipes;

public class ReloadPipesCommandExecutor implements PipesCommandExecutor {

	@Override
	public boolean onCommand(CommandSender cs) {
		if (!cs.hasPermission(TransportPipes.instance.generalConf.getPermissionReload())) {
			return false;
		}
		TransportPipes.pipePacketManager.reloadPipesAndItems();
		cs.sendMessage("§cPipes reloaded");
		return true;
	}

}