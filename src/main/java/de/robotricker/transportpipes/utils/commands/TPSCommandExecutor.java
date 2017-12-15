package de.robotricker.transportpipes.utils.commands;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import de.robotricker.transportpipes.PipeThread;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctType;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.config.LocConf;
import de.robotricker.transportpipes.utils.hitbox.HitboxListener;
import de.robotricker.transportpipes.utils.hitbox.TimingCloseable;

public class TPSCommandExecutor implements PipesCommandExecutor {

	@Override
	public boolean onCommand(CommandSender cs, String[] args) {
		if (!cs.hasPermission("transportpipes.tps")) {
			return false;
		}
		int tps = TransportPipes.instance.pipeThread.getCalculatedTps();
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

		float lastTickDiff = TransportPipes.instance.pipeThread.getLastTickDiff() / 1000f;

		cs.sendMessage(String.format(LocConf.load(LocConf.COMMANDS_HEADER), TransportPipes.instance.getDescription().getVersion()));
		cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Tick duration: " + colour + (TransportPipes.instance.pipeThread.timeTick / 10000) / 100f + "ms"));
		cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Last Tick: " + lastTickDiff));
		cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6TPS: " + colour + tps + " &6/ §2" + PipeThread.WANTED_TPS));

		for (World world : Bukkit.getWorlds()) {
			int worldPipes = 0;
			int worldItems = 0;
			Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(world);
			if (ductMap != null) {
				synchronized (ductMap) {
					for (Duct duct : ductMap.values()) {
						if (duct.getDuctType() == DuctType.PIPE) {
							Pipe pipe = (Pipe) duct;
							worldPipes++;
							worldItems += pipe.pipeItems.size() + pipe.tempPipeItems.size() + pipe.tempPipeItemsWithSpawn.size();
						}
					}
				}
				cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6" + world.getName() + ": &e" + worldPipes + " &6" + "pipes, &e" + worldItems + " &6items"));
			}
		}

		/*
		 * cs.sendMessage("Timings:"); for (String timing :
		 * TimingCloseable.timings.keySet()) { long time =
		 * TimingCloseable.timings.get(timing); long maxTime =
		 * TimingCloseable.timingsRecord.get(timing); String timeS =
		 * String.format("%.3f", time / 1000000f); String maxTimeS =
		 * String.format("%.3f", maxTime / 1000000f); long amount =
		 * TimingCloseable.timingsAmount.get(timing); cs.sendMessage(timing + ": §6" +
		 * timeS + "§r millis (Max: §6" + maxTimeS + "§r, " + amount + " times)"); }
		 */

		cs.sendMessage(LocConf.load(LocConf.COMMANDS_FOOTER));

		return true;

	}

}
