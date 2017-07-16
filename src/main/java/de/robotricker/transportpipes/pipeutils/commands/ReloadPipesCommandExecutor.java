package de.robotricker.transportpipes.pipeutils.commands;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.robotricker.transportpipes.TransportPipes;

public class ReloadPipesCommandExecutor implements PipesCommandExecutor {

	@Override
	public boolean onCommand(CommandSender cs) {
		if (!cs.hasPermission(TransportPipes.instance.generalConf.getPermissionReload())) {
			return false;
		}
		//TransportPipes.pipePacketManager.reloadPipesAndItems();
		//TODO reload!

		Block b = ((Player) cs).getTargetBlock((Set<Material>) null, 10);
		if (b != null)
			cs.sendMessage(b.getType() + ":" + b.getTypeId());

		cs.sendMessage("§cPipes reloaded");
		return true;
	}

}