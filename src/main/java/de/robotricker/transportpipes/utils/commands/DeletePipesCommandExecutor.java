package de.robotricker.transportpipes.utils.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.staticutils.DuctUtils;

public class DeletePipesCommandExecutor implements PipesCommandExecutor {

	@Override
	public boolean onCommand(CommandSender cs, String[] args) {
		if (!cs.hasPermission("transportpipes.delete")) {
			return false;
		}

		if (cs instanceof Player) {
			Player p = (Player) cs;
			BlockLoc playerBl = BlockLoc.convertBlockLoc(p.getLocation());
			try {
				int radiusSquared = (int) Math.pow(Integer.parseInt(args[0]), 2);
				Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(p.getWorld());
				List<Duct> removedDucts = new ArrayList<Duct>();

				if (ductMap != null) {

					synchronized (ductMap) {
						Set<BlockLoc> keySet = ductMap.keySet();
						Iterator<BlockLoc> keySetIt = keySet.iterator();
						while (keySetIt.hasNext()) {
							BlockLoc bl = keySetIt.next();
							if (bl.distanceSquared(playerBl) <= radiusSquared) {
								removedDucts.add(ductMap.get(bl));
							}
						}
						for (Duct duct : removedDucts) {
							DuctUtils.destroyDuct(null, duct, false);
						}
					}
				}
				cs.sendMessage("§c" + removedDucts.size() + " ducts deleted");
			} catch (NumberFormatException e) {
				cs.sendMessage("§cRadius has to be an integer");
			}
		} else {
			cs.sendMessage("§cYou're not a player!");
		}
		return true;
	}

}