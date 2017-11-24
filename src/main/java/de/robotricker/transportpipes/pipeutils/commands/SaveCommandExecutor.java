package de.robotricker.transportpipes.pipeutils.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipes.types.Pipe;
import de.robotricker.transportpipes.pipeutils.config.PlayerSettingsConf;
import de.robotricker.transportpipes.pipeutils.hitbox.AxisAlignedBB;
import de.robotricker.transportpipes.pipeutils.hitbox.OcclusionCullingUtils;

public class SaveCommandExecutor implements PipesCommandExecutor {

	@Override
	public boolean onCommand(CommandSender cs, String[] args) {

		if (!cs.hasPermission("transportpipes.save")) {
			return false;
		}

		TransportPipes.instance.savingManager.savePipesAsync(true);

		cs.sendMessage("§cPipes saved");

		return true;
	}

}
