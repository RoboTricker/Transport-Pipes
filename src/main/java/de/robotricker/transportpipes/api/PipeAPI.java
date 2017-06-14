package de.robotricker.transportpipes.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.PipeThread;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.TransportPipes.BlockLoc;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipes.GoldenPipe;
import de.robotricker.transportpipes.pipes.IronPipe;
import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipeutils.PipeColor;
import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.pipeutils.PipeUtils;

/**
 * 
 * This class is the interface between the TransportPipes plugin and other plugins.<br>
 * It allows you to build/destroy pipes programmatically and gather some extra information about the PipeThread etc.<br>
 * There are also some new events called "PlayerPlacePipeEvent", "PlayerDestroyPipeEvent" and "PipeExplodeEvent (async!)" for which you can register your<br>
 * listeners and cancel them if you want to.
 * 
 * @author RoboTricker
 */
public class PipeAPI {

	/**
	 * builds a normal pipe at the given location.
	 */
	public static void buildPipe(Location blockLoc, PipeColor pipeColor) {
		PipeUtils.buildPipe(null, blockLoc, pipeColor, false);
	}

	/**
	 * builds a golden pipe at the given location.
	 */
	public static void buildGoldenPipe(Location blockLoc) {
		PipeUtils.buildPipe(null, blockLoc, GoldenPipe.class, PipeColor.WHITE, false);
	}

	/**
	 * builds an iron pipe at the given location.
	 */
	public static void buildIronPipe(Location blockLoc) {
		PipeUtils.buildPipe(null, blockLoc, IronPipe.class, PipeColor.WHITE, false);
	}

	/**
	 * builds a detector pipe at the given location.
	 */
	public static void buildDetectorPipe(Location blockLoc) {
		PipeUtils.buildPipe(null, blockLoc, PipeColor.WHITE, true);
	}

	/**
	 * detroys the pipe on the given location.
	 * 
	 * @param dropItem
	 *            if true the destroye pipe will drop the pipe item (blaze rod).
	 */
	public static void destroyPipe(Location blockLoc, boolean dropItem) {
		Pipe pipe = PipeUtils.getPipeWithLocation(blockLoc);
		if (pipe != null) {
			PipeUtils.destroyPipe(null, pipe, dropItem);
		}
	}

	/**
	 * gives you the current "ticks per second" of the TransportPipes-Thread (7 is good and the maximum).
	 */
	public static int getTPS() {
		return PipeThread.getCalculatedTps();
	}

	/**
	 * returns the amount of pipes in all worlds.
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
	 * returns the amount of pipes in the given world.
	 */
	public static int getPipeCount(World world) {
		Map<BlockLoc, Pipe> pipeMap = TransportPipes.getPipeMap(world);
		if (pipeMap != null) {
			return pipeMap.size();
		}
		return 0;
	}

	/**
	 * checks if at this specific location is a pipe.
	 */
	public static boolean isPipe(Location blockLoc) {
		return PipeUtils.getPipeWithLocation(blockLoc) != null;
	}

	/**
	 * returns a pipe object at the given location or null if there is no pipe.
	 */
	public static Pipe getPipeAtLocation(Location blockLoc) {
		World world = blockLoc.getWorld();
		BlockLoc bl = new BlockLoc(blockLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ());
		if (TransportPipes.getPipeMap(world) != null) {
			return TransportPipes.getPipeMap(world).getOrDefault(bl, null);
		}
		return null;
	}

	/**
	 * destroys all pipes in this world.
	 * 
	 * @param dropItems
	 *            if true the destroyed pipes will drop the pipe item (blaze rod).
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

	/**
	 * puts any item (with an amount of 1!!!) into the given pipe object with a moving direction to "itemDirection".
	 */
	public static void putItemInPipe(Pipe pipe, ItemStack item, PipeDirection itemDirection) {
		PipeItem pi = new PipeItem(item, pipe.blockLoc, itemDirection);
		pipe.tempPipeItemsWithSpawn.put(pi, itemDirection);
	}

}
