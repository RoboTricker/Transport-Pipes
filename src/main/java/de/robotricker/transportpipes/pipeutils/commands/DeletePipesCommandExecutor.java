package de.robotricker.transportpipes.pipeutils.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipes.BlockLoc;
import de.robotricker.transportpipes.pipes.PipeUtils;
import de.robotricker.transportpipes.pipes.types.Pipe;

public class DeletePipesCommandExecutor implements PipesCommandExecutor {

	@Override
	public boolean onCommand(CommandSender cs, String[] args) {
		if (!cs.hasPermission(TransportPipes.instance.generalConf.getPermissionDelete())) {
			return false;
		}

		if (cs instanceof Player) {
			Player p = (Player) cs;
			BlockLoc playerBl = BlockLoc.convertBlockLoc(p.getLocation());
			try {
				int radiusSquared = (int) Math.pow(Integer.parseInt(args[0]), 2);
				Map<BlockLoc, Pipe> pipeMap = TransportPipes.instance.getPipeMap(p.getWorld());
				List<Pipe> removedPipes = new ArrayList<Pipe>();

				if (pipeMap != null) {

					synchronized (pipeMap) {
						Set<BlockLoc> keySet = pipeMap.keySet();
						Iterator<BlockLoc> keySetIt = keySet.iterator();
						while (keySetIt.hasNext()) {
							BlockLoc bl = keySetIt.next();
							if (bl.distanceSquared(playerBl) <= radiusSquared) {
								removedPipes.add(pipeMap.get(bl));
							}
						}
						for (Pipe pipe : removedPipes) {
							PipeUtils.destroyPipe(null, pipe);
						}
					}
				}
				cs.sendMessage("§c" + removedPipes.size() + " Pipes deleted");
			} catch (NumberFormatException e) {
				cs.sendMessage("§cRadius has to be an integer");
			}
		} else {
			cs.sendMessage("§cYou're not a player!");
		}
		return true;
	}

}