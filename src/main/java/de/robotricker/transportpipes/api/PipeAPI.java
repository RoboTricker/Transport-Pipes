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
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctType;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.duct.pipe.utils.PipeColor;
import de.robotricker.transportpipes.duct.pipe.utils.PipeType;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.ductdetails.PipeDetails;
import de.robotricker.transportpipes.utils.staticutils.DuctUtils;
import io.sentry.Sentry;

/**
 * 
 * This class is the interface between the TransportPipes plugin and other
 * plugins.<br>
 * It allows you to build/destroy pipes programmatically and gather some extra
 * information about the PipeThread etc.<br>
 * 
 * @author RoboTricker
 */
public class PipeAPI {

	/**
	 * Builds a pipe at the given location. Additionally you can determine which
	 * type and which color the pipe should have.<br>
	 * The PipeColor is ignored if PipeType is not COLORED.
	 */
	public static void buildPipe(Location blockLoc, PipeType pt, PipeColor pipeColor) {
		PipeDetails pd;
		if (pt == PipeType.COLORED) {
			pd = new PipeDetails(pipeColor);
		} else {
			pd = new PipeDetails(pt);
		}
		DuctUtils.buildDuct(null, blockLoc.getBlock().getLocation(), pd);
	}

	/**
	 * Detroys the duct at the given location.
	 */
	public static void destroyDuct(Location blockLoc) {
		Duct duct = DuctUtils.getDuctAtLocation(blockLoc);
		if (duct != null) {
			DuctUtils.destroyDuct(null, duct, false);
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
	 * Returns the amount of ducts in all worlds.
	 */
	public static int getDuctCount() {
		int amount = 0;
		for (World world : Bukkit.getWorlds()) {
			Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(world);
			if (ductMap != null) {
				amount += ductMap.size();
			}
		}
		return amount;
	}

	/**
	 * Returns the amount of ducts in the given world.
	 */
	public static int getDuctCount(World world) {
		Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(world);
		if (ductMap != null) {
			return ductMap.size();
		}
		return 0;
	}

	/**
	 * Checks whether at the given location is a pipe.
	 */
	public static boolean isDuct(Location blockLoc, DuctType dt) {
		Duct duct = DuctUtils.getDuctAtLocation(blockLoc);
		if (dt == null) {
			return duct != null;
		}
		return duct != null && duct.getDuctType() == dt;
	}

	/**
	 * Returns the pipe object at the given location or null if there is no pipe.
	 */
	public static Pipe getPipeAtLocation(Location blockLoc) {
		World world = blockLoc.getWorld();
		BlockLoc bl = new BlockLoc(blockLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ());
		if (TransportPipes.instance.getDuctMap(world) != null) {
			Duct duct = TransportPipes.instance.getDuctMap(world).get(bl);
			return duct != null && duct instanceof Pipe ? (Pipe) duct : null;
		}
		return null;
	}

	/**
	 * Destroys all ducts in this world.
	 */
	public static void destroyDucts(World world) {
		List<Duct> toDestroy = new ArrayList<>();

		Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(world);
		if (ductMap != null) {
			synchronized (ductMap) {
				toDestroy.addAll(ductMap.values());
			}
		}

		for (Duct duct : toDestroy) {
			destroyDuct(duct.getBlockLoc());
		}
	}

	/**
	 * Puts any item into the given pipe object with a moving direction of
	 * "itemDirection".
	 */
	public static void putItemInPipe(Pipe pipe, ItemStack item, WrappedDirection itemDirection) {
		PipeItem pi = new PipeItem(item, pipe.getBlockLoc(), itemDirection);
		pipe.tempPipeItemsWithSpawn.put(pi, itemDirection);
	}

	/**
	 * Registers a custom container block at the given location. Every pipe around
	 * this block will try to extract/insert items from/into this container.<br>
	 * Create your own implementation of the TransportPipesContainer interface in
	 * order to specify which items to extract and where inserted items should go.
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

		try {
			Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(blockLoc.getWorld());
			if (ductMap != null) {
				for (WrappedDirection pd : WrappedDirection.values()) {
					bl = BlockLoc.convertBlockLoc(blockLoc.clone().add(pd.getX(), pd.getY(), pd.getZ()));
					if (ductMap.containsKey(bl)) {
						final Duct duct = ductMap.get(bl);
						if (duct.getDuctType() == DuctType.PIPE) {
							TransportPipes.instance.pipeThread.runTask(new Runnable() {

								@Override
								public void run() {
									TransportPipes.instance.ductManager.updateDuct((Pipe) duct);
								}
							}, 0);
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Sentry.capture(ex);
		}

	}

	/**
	 * Unregisters a custom container block. See
	 * {@link PipeAPI#registerTransportPipesContainer(Location, TransportPipesContainer)}
	 */
	public static void unregisterTransportPipesContainer(Location blockLoc) {
		try {
			BlockLoc bl = BlockLoc.convertBlockLoc(blockLoc);

			Map<BlockLoc, TransportPipesContainer> containerMap = TransportPipes.instance.getContainerMap(blockLoc.getWorld());
			if (containerMap != null) {
				if (containerMap.containsKey(bl)) {
					containerMap.remove(bl);

					Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(blockLoc.getWorld());
					if (ductMap != null) {
						for (WrappedDirection pd : WrappedDirection.values()) {
							bl = BlockLoc.convertBlockLoc(blockLoc.clone().add(pd.getX(), pd.getY(), pd.getZ()));
							if (ductMap.containsKey(bl)) {
								final Duct duct = ductMap.get(bl);
								if (duct.getDuctType() == DuctType.PIPE) {
									TransportPipes.instance.pipeThread.runTask(new Runnable() {

										@Override
										public void run() {
											TransportPipes.instance.ductManager.updateDuct((Pipe) duct);
										}
									}, 0);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Sentry.capture(e);
		}

	}

}
