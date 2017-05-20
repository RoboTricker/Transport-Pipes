package de.robotricker.transportpipes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import de.robotricker.transportpipes.TransportPipes.BlockLoc;
import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipeutils.PipeColor;
import de.robotricker.transportpipes.pipeutils.PipeUtils;

/**
 * 
 * This class is the interface between the TransportPipes plugin and other plugins.<br>
 * It allows you to build/destroy pipes programmatically and gather some extra information about the PipeThread etc.
 * 
 * @author RoboTricker
 */
public class PipeAPI {

	/**
	 * builds a pipe on the given location
	 */
	public static void buildPipe(Location blockLoc, PipeColor pipeColor) {
		PipeUtils.buildPipe(blockLoc, pipeColor);
	}

	/**
	 * detroys the pipe on the given location
	 * 
	 * @param dropItem
	 *            if true the destroye pipe will drop the pipe item (blaze rod)
	 */
	public static void destroyPipe(Location blockLoc, boolean dropItem) {
		Pipe pipe = PipeUtils.getPipeWithLocation(blockLoc);
		if (pipe != null) {
			PipeUtils.destroyPipe(pipe, dropItem);
		}
	}

	/**
	 * gives you the current "ticks per second" of the TransportPipes-Thread (7 is good and the maximum)
	 */
	public static int getTPS() {
		return PipeThread.getCalculatedTps();
	}

	/**
	 * returns the amount of pipes in all worlds
	 */
	public static int getPipeCount() {
		int amount = 0;
		for (World world : Bukkit.getWorlds()) {
			Map<BlockLoc, Pipe> pipeMap = TransportPipes.getPipeMap(world);
			if (pipeMap != null) {
				amount += pipeMap.size();
			}
		}
		return amount;
	}

	/**
	 * returns the amount of pipes in the given world
	 */
	public static int getPipeCount(World world) {
		Map<BlockLoc, Pipe> pipeMap = TransportPipes.getPipeMap(world);
		if (pipeMap != null) {
			return pipeMap.size();
		}
		return 0;
	}

	/**
	 * checks if at this specific location is a pipe
	 */
	public static boolean isPipe(Location blockLoc) {
		return PipeUtils.getPipeWithLocation(blockLoc) != null;
	}

	/**
	 * destroys all pipes in this world
	 * 
	 * @param dropItems
	 *            if true the destroyed pipes will drop the pipe item (blaze rod)
	 */
	public static void destroyPipes(World world, boolean dropItems) {
		List<Pipe> toDestroy = new ArrayList<>();

		Map<BlockLoc, Pipe> pipeMap = TransportPipes.getPipeMap(world);
		if (pipeMap != null) {
			synchronized (pipeMap) {
				for (Pipe pipe : pipeMap.values()) {
					toDestroy.add(pipe);
				}
			}
		}

		for (Pipe pipe : toDestroy) {
			destroyPipe(pipe.blockLoc, dropItems);
		}
	}

}
