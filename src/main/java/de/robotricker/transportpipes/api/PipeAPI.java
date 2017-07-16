package de.robotricker.transportpipes.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import de.robotricker.transportpipes.PipeThread;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipes.BlockLoc;
import de.robotricker.transportpipes.pipes.PipeDirection;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipes.PipeUtils;
import de.robotricker.transportpipes.pipes.colored.PipeColor;
import de.robotricker.transportpipes.pipes.types.Pipe;

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
	 * builds a pipe at the given location.
	 */
	public static void buildPipe(Location blockLoc, PipeType pt, PipeColor pipeColor) {
		PipeUtils.buildPipe(null, blockLoc.getBlock().getLocation(), pt, pipeColor);
	}

	/**
	 * detroys the pipe on the given location.
	 * 
	 * @param blockLoc
	 *            location of the pipe
	 */
	public static void destroyPipe(Location blockLoc) {
		Pipe pipe = PipeUtils.getPipeWithLocation(blockLoc);
		if (pipe != null) {
			PipeUtils.destroyPipe(null, pipe);
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
			Map<BlockLoc, Pipe> pipeMap = TransportPipes.instance.getPipeMap(world);
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
		Map<BlockLoc, Pipe> pipeMap = TransportPipes.instance.getPipeMap(world);
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
		if (TransportPipes.instance.getPipeMap(world) != null) {
			return TransportPipes.instance.getPipeMap(world).get(bl);
		}
		return null;
	}

	/**
	 * destroys all pipes in this world.
	 * 
	 * @param world
	 *            world containing the pipes that nees to be destroyes
	 */
	public static void destroyPipes(World world) {
		List<Pipe> toDestroy = new ArrayList<>();

		Map<BlockLoc, Pipe> pipeMap = TransportPipes.instance.getPipeMap(world);
		if (pipeMap != null) {
			synchronized (pipeMap) {
				toDestroy.addAll(pipeMap.values());
			}
		}

		for (Pipe pipe : toDestroy) {
			destroyPipe(pipe.blockLoc);
		}
	}

	/**
	 * puts any item (with an amount of 1!!!) into the given pipe object with a moving direction to "itemDirection".
	 */
	public static void putItemInPipe(Pipe pipe, ItemData item, PipeDirection itemDirection) {
		PipeItem pi = new PipeItem(item, pipe.blockLoc, itemDirection);
		pipe.tempPipeItemsWithSpawn.put(pi, itemDirection);
	}

	public static void registerTransportPipesContainer(Location blockLoc, TransportPipesContainer tpc) {
		BlockLoc bl = BlockLoc.convertBlockLoc(blockLoc);

		Map<BlockLoc, TransportPipesContainer> containerMap = TransportPipes.instance.getContainerMap(blockLoc.getWorld());
		if (containerMap == null) {
			containerMap = Collections.synchronizedMap(new TreeMap<BlockLoc, TransportPipesContainer>());
			TransportPipes.instance.getFullContainerMap().put(blockLoc.getWorld(), containerMap);
		}
		if (containerMap.containsKey(bl)) {
			throw new IllegalArgumentException("There is already a TransportPipesContainer object registered at this location");
		}
		containerMap.put(bl, tpc);
		System.out.println("registered " + blockLoc.getBlock().getState().getClass().getSimpleName());
	}

	public static void unregisterTransportPipesContainer(Location blockLoc) {
		BlockLoc bl = BlockLoc.convertBlockLoc(blockLoc);

		Map<BlockLoc, TransportPipesContainer> containerMap = TransportPipes.instance.getContainerMap(blockLoc.getWorld());
		if (containerMap != null) {
			if (containerMap.containsKey(bl)) {
				containerMap.remove(bl);
				System.out.println("unregistered " + blockLoc.getBlock().getState().getClass().getSimpleName());
			}
		}
	}

}
