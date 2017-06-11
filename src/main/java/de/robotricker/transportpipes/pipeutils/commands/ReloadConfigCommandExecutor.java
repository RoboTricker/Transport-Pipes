package de.robotricker.transportpipes.pipeutils.commands;

import org.bukkit.command.CommandSender;

import de.robotricker.transportpipes.TransportPipes;

public class ReloadConfigCommandExecutor implements PipesCommandExecutor {

	@Override
	public void onCommand(CommandSender cs, String[] args) {
		TransportPipes.instance.reloadConfig();
		cs.sendMessage(TransportPipes.instance.PREFIX + "Config reloaded");
	}

}
