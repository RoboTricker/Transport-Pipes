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

		cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l&m---------------&7&l[ &6TransportPipes " + TransportPipes.instance.getDescription().getVersion() + "&7&l]&7&l&m---------------"));
		cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Tick duration: " + colour + (PipeThread.timeTick / 10000) / 100f + "ms"));
		cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6TPS: " + colour + tps + " &6/ §2" + PipeThread.WANTED_TPS));
		cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Armorstands sent since server start: &e" + armorStandSendsSinceServerStart));

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
				cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6" + world.getName() + ": &e" + worldPipes + " &6" + "pipes, &e" + worldItems + " &6items"));
			}
		}

		cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&l&m--------------------------------------------"));

	}

}
