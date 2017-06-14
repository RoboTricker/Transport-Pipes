package de.robotricker.transportpipes.pipeutils.commands;

import org.bukkit.command.CommandSender;

import de.robotricker.transportpipes.TransportPipes;

public class ReloadPipesCommandExecutor implements PipesCommandExecutor {

	@Override
	public boolean onCommand(CommandSender cs) {
		if (!cs.hasPermission(TransportPipes.instance.getConfig().getString("permissions.reload", "tp.reload"))) {
			return false;
		}
		TransportPipes.pipePacketManager.reloadPipesAndItems();
		cs.sendMessage(TransportPipes.instance.PREFIX + "Pipes reloaded");
		return true;
	}

}