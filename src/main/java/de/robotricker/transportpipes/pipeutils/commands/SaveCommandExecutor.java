package de.robotricker.transportpipes.pipeutils.commands;

import org.bukkit.command.CommandSender;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.saving.SavingManager;

public class SaveCommandExecutor implements PipesCommandExecutor {

	@Override
	public boolean onCommand(CommandSender cs, String[] args) {

		if (!cs.hasPermission(TransportPipes.instance.generalConf.getPermissionSave())) {
			return false;
		}

		SavingManager.savePipesAsync();
//		Block b = ((Player) cs).getTargetBlock((HashSet<Material>) null, 10);
//		if (b != null) {
//			AxisAlignedBB aabb = new AxisAlignedBB(((Player) cs).getTargetBlock((HashSet<Material>) null, 10));
//			System.out.println(b.getType() + "->" + aabb.toString());
//		}
		cs.sendMessage("§cPipes saved");

		return true;
	}

}
