package de.robotricker.transportpipes.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.PipeThread;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipes.BlockLoc;
import de.robotricker.transportpipes.pipes.WrappedDirection;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipes.PipeUtils;
import de.robotricker.transportpipes.pipes.colored.PipeColor;
import de.robotricker.transportpipes.pipes.types.Pipe;

/**
 * 
 * This class is the interface between the TransportPipes plugin and other plugins.<br>
 * It allows you to build/destroy pipes programmatically and gather some extra information about the PipeThread etc.<br>
 * 
 * @author RoboTricker
 */
public class PipeAPI {

	/**
	 * Builds a pipe at the given location. Additionally you can determine which type and which color the pipe should have.<br>
	 * The PipeColor is ignored if PipeType is not COLORED.
	 */
	public static void buildPipe(Location blockLoc, PipeType pt, PipeColor pipeColor) {
		PipeUtils.buildPipe(null, blockLoc.getBlock().getLocation(), pt, pipeColor);
	}

	/**
	 * Detroys the pipe at the given location.
	 */
	public static void destroyPipe(Location blockLoc) {
		Pipe pipe = PipeUtils.getDuctAtLocation(blockLoc);
		if (pipe != null) {
			PipeUtils.destroyPipe(null, pipe);
		}
	}

	/**
	 * Returns the current ticks per second of the TransportPipes thread.
	 */
	public static int getTPS() {
		return TransportPipes.instance.pipeThread.getCalculatedTps();
	}

	/**
	 * Returns the max tps of the TransportPipes thread.<br>
	 * If this value is reached with the real tps, the thread is running fine.
	 */
	public static int getMaxTPS() {
		return PipeThread.WANTED_TPS;
	}

	/**
	 * Returns the amount of pipes in all worlds.
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
	 * Returns the amount of pipes in the given world.
	 */
	public static int getPipeCount(World world) {
		Map<BlockLoc, Pipe> pipeMap = TransportPipes.instance.getPipeMap(world);
		if (pipeMap != null) {
			return pipeMap.size();
		}
		return 0;
	}

	/**
	 * Checks whether at the given location is a pipe.
	 */
	public static boolean isPipe(Location blockLoc) {
		return PipeUtils.getDuctAtLocation(blockLoc) != null;
	}

	/**
	 * Returns the pipe object at the given location or null if there is no pipe.
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
	 * Destroys all pipes in this world.
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
			destroyPipe(pipe.getBlockLoc());
		}
	}

	/**
	 * Puts any item into the given pipe object with a moving direction of "itemDirection".
	 */
	public static void putItemInPipe(Pipe pipe, ItemStack item, WrappedDirection itemDirection) {
		PipeItem pi = new PipeItem(item, pipe.getBlockLoc(), itemDirection);
		pipe.tempPipeItemsWithSpawn.put(pi, itemDirection);
	}

	/**
	 * Registers a custom container block at the given location. Every pipe around this block will try to extract/insert items from/into this container.<br>
	 * Create your own implementation of the TransportPipesContainer interface in order to specify which items to extract and where inserted items should go.
	 * 
	 * @param tpc
	 *            your own implementation of the TransportPipesContainer interface
	 */
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

		Map<BlockLoc, Pipe> pipeMap = TransportPipes.instance.getPipeMap(blockLoc.getWorld());
		if (pipeMap != null) {
			for (WrappedDirection pd : WrappedDirection.values()) {
				bl = BlockLoc.convertBlockLoc(blockLoc.clone().add(pd.getX(), pd.getY(), pd.getZ()));
				if (pipeMap.containsKey(bl)) {
					final Pipe pipe = pipeMap.get(bl);
					TransportPipes.instance.pipeThread.runTask(new Runnable() {

						@Override
						public void run() {
							TransportPipes.instance.pipePacketManager.updatePipe(pipe);
						}
					}, 0);
				}
			}
		}

	}

	/**
	 * Unregisters a custom container block. See {@link PipeAPI#registerTransportPipesContainer(Location, TransportPipesContainer)}
	 */
	public static void unregisterTransportPipesContainer(Location blockLoc) {
		BlockLoc bl = BlockLoc.convertBlockLoc(blockLoc);

		Map<BlockLoc, TransportPipesContainer> containerMap = TransportPipes.instance.getContainerMap(blockLoc.getWorld());
		if (containerMap != null) {
			if (containerMap.containsKey(bl)) {
				containerMap.remove(bl);

				Map<BlockLoc, Pipe> pipeMap = TransportPipes.instance.getPipeMap(blockLoc.getWorld());
				if (pipeMap != null) {
					for (WrappedDirection pd : WrappedDirection.values()) {
						bl = BlockLoc.convertBlockLoc(blockLoc.clone().add(pd.getX(), pd.getY(), pd.getZ()));
						if (pipeMap.containsKey(bl)) {
							final Pipe pipe = pipeMap.get(bl);
							TransportPipes.instance.pipeThread.runTask(new Runnable() {

								@Override
								public void run() {
									TransportPipes.instance.pipePacketManager.updatePipe(pipe);
								}
							}, 0);
						}
					}
				}
			}
		}

	}

}
