package de.robotricker.transportpipes.pipeutils.commands;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipeutils.PipeColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveCommandExecutor implements PipesCommandExecutor {

	@Override
	public boolean onCommand(CommandSender cs) {
		if (!(cs instanceof Player)) {
			return false;
		}
		Player player = (Player) cs;
		if (!player.hasPermission(TransportPipes.instance.getConfig().getString("permissions.give", "tp.give"))) {
			return false;
		}
		for (PipeType pipeType : PipeType.values()) {
			if (pipeType == PipeType.COLORED) {
				for (PipeColor pipeColor : PipeColor.values()) {
					player.getInventory().addItem(TransportPipes.instance.getPipeItemForPlayer(null, pipeType, pipeColor));
				}
			}
			player.getInventory().addItem(TransportPipes.instance.getPipeItemForPlayer(null, pipeType, null));
		}
		return true;
	}

}
