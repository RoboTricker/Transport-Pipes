package de.robotricker.transportpipes.pipeutils.commands;

import org.bukkit.command.CommandSender;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.saving.SavingManager;

public class SaveCommandExecutor implements PipesCommandExecutor {

	@Override
	public boolean onCommand(CommandSender cs) {

		if (!cs.hasPermission(TransportPipes.instance.generalConf.getPermissionSave())) {
			return false;
		}

		SavingManager.savePipesAsync();
		
		cs.sendMessage("§cPipes saved");

		return true;
	}

}
