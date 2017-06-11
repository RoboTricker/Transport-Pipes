package de.robotricker.transportpipes.pipeutils.commands;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import de.robotricker.transportpipes.PipeThread;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.TransportPipes.BlockLoc;
import de.robotricker.transportpipes.pipes.Pipe;

public class TPSCommandExecutor implements PipesCommandExecutor {

	@Override
	public void onCommand(CommandSender cs, String[] args) {
		int tps = PipeThread.getCalculatedTps();
		ChatColor colour = ChatColor.DARK_GREEN;
		if (tps <= 1) {
			colour = ChatColor.DARK_RED;
		} else if (tps <= 3) {
			colour = ChatColor.RED;
		} else if (tps <= 4) {
			colour = ChatColor.GOLD;
		} else if (tps <= 5) {
			colour = ChatColor.GREEN;
		}

		int armorStandSendsSinceServerStart = 0;
		Map<World, List<Integer>> allWorldEntityIds = TransportPipes.pipePacketManager.allWorldEntityIds;
		synchronized (allWorldEntityIds) {
			for (World w : allWorldEntityIds.keySet()) {
				armorStandSendsSinceServerStart += allWorldEntityIds.get(w).size();
			}
		}

		cs.sendMessage(TransportPipes.instance.PREFIX + ChatColor.GOLD + "TransportPipes " + ChatColor.YELLOW + "v" + ChatColor.GOLD + TransportPipes.instance.getDescription().getVersion());
		cs.sendMessage(TransportPipes.instance.PREFIX + ChatColor.GOLD + "TPS: " + colour + tps + " " + ChatColor.GOLD + "/ " + ChatColor.DARK_GREEN + PipeThread.WANTED_TPS);
		cs.sendMessage(TransportPipes.instance.PREFIX + ChatColor.GOLD + "Tick duration: " + colour + (PipeThread.timeTick / 10000) / 100f + "ms");
		cs.sendMessage(TransportPipes.instance.PREFIX + ChatColor.GOLD + "Armorstands sent since server start: " + ChatColor.YELLOW + armorStandSendsSinceServerStart);
		for (World world : Bukkit.getWorlds()) {
			int worldPipes = 0;
			int worldItems = 0;
			Map<BlockLoc, Pipe> pipeMap = TransportPipes.getPipeMap(world);
			if (pipeMap != null) {
				synchronized (pipeMap) {
					for (Pipe pipe : pipeMap.values()) {
						worldPipes++;
						worldItems += pipe.pipeItems.size() + pipe.tempPipeItems.size() + pipe.tempPipeItemsWithSpawn.size();
					}
				}
				cs.sendMessage(TransportPipes.instance.PREFIX + ChatColor.GOLD + world.getName() + ": " + ChatColor.YELLOW + "" + worldPipes + " " + ChatColor.GOLD + "pipes, " + ChatColor.YELLOW + "" + worldItems + " " + ChatColor.GOLD + "items");
			}
		}

	}

}
