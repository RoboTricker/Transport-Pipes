package de.robotricker.transportpipes.pipeutils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import de.robotricker.transportpipes.PipeThread;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.TransportPipes.BlockLoc;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipes.PipeEW;
import de.robotricker.transportpipes.pipes.PipeMID;
import de.robotricker.transportpipes.pipes.PipeNS;
import de.robotricker.transportpipes.pipes.PipeUD;

public class PipeUtils {

	public static boolean buildPipe(final Location blockLoc) {
		return buildPipe(blockLoc, Pipe.class);
	}

	/**
	 * invoke this if you want to build a new pipe at this location. If there is a pipe already, it will do nothing. Otherwise it will place the pipe and send the packets to the players near. don't call this if you only want to update the pipe! returns whether the pipe could be placed
	 */
	public static boolean buildPipe(final Location blockLoc, Class<? extends Pipe> pipeClass) {

		//check if there is already a pipe at this position
		Map<BlockLoc, Pipe> pipeMap = TransportPipes.getPipeMap(blockLoc.getWorld());
		if (pipeMap != null) {
			if (pipeMap.containsKey(TransportPipes.convertBlockLoc(blockLoc))) {
				//there already exists a pipe at this location
				return false;
			}
		}

		//check if there is a block
		if (!(blockLoc.getBlock().getType() == Material.AIR || blockLoc.getBlock().isLiquid())) {
			return false;
		}

		//calculate all neighbor blocks and pipes
		final List<PipeDirection> pipeConnections = getPipeConnections(blockLoc);
		//ONLY SYNC!!!
		final List<PipeDirection> pipeNeighborBlocks = getPipeNeighborBlocksSync(blockLoc);

		List<PipeDirection> combi = new ArrayList<PipeDirection>();
		combi.addAll(pipeConnections);
		for (PipeDirection dir : pipeNeighborBlocks) {
			if (!combi.contains(dir)) {
				combi.add(dir);
			}
		}

		//build pipe
		Class<? extends Pipe> newPipeClass = pipeClass;

		//if the "PipeClass" is null, that means that the pipe is normal and not golden or iron etc.
		if (newPipeClass == null || newPipeClass == Pipe.class) {
			newPipeClass = calculatePipeShapeWithDirList(combi);
		}

		if (newPipeClass != null) {
			Pipe pipe;
			try {
				pipe = newPipeClass.getConstructor(Location.class, List.class).newInstance(blockLoc, pipeNeighborBlocks);
				TransportPipes.putPipe(pipe);

				final Pipe finalPipe = pipe;

				TransportPipes.pipePacketManager.spawnPipeSync(finalPipe);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		//update neighbor pipes
		PipeThread.runTask(new Runnable() {

			@Override
			public void run() {

				Map<BlockLoc, Pipe> pipeMap = TransportPipes.getPipeMap(blockLoc.getWorld());
				if (pipeMap != null) {
					for (PipeDirection dir : PipeDirection.values()) {
						BlockLoc blockLocLong = TransportPipes.convertBlockLoc(blockLoc.clone().add(dir.getX(), dir.getY(), dir.getZ()));
						if (pipeMap.containsKey(blockLocLong)) {
							pipeMap.get(blockLocLong).updatePipeShape();
						}
					}
				}

			}
		}, 0);

		return true;

	}

	/**
	 * invoke this if you want to destroy a pipe. This will remove the pipe from the pipe list and destroys it for all players
	 */
	public static void destroyPipe(final Pipe pipeToDestroy, final boolean dropItem) {
		PipeThread.runTask(new Runnable() {

			@Override
			public void run() {

				Map<BlockLoc, Pipe> pipeMap = TransportPipes.getPipeMap(pipeToDestroy.blockLoc.getWorld());
				if (pipeMap != null) {
					//only remove the pipe if it is in the pipe list!
					if (pipeMap.containsKey(TransportPipes.convertBlockLoc(pipeToDestroy.blockLoc))) {
						pipeMap.remove(TransportPipes.convertBlockLoc(pipeToDestroy.blockLoc));

						TransportPipes.pipePacketManager.destroyPipeSync(pipeToDestroy);

						//drop all items in old pipe
						for (final PipeItem item : pipeToDestroy.pipeItems.keySet()) {
							Bukkit.getScheduler().runTask(TransportPipes.instance, new Runnable() {

								@Override
								public void run() {
									pipeToDestroy.blockLoc.getWorld().dropItem(pipeToDestroy.blockLoc.clone().add(0.5d, 0.5d, 0.5d), item.getItem());
								}
							});
							//destroy item for players
							TransportPipes.pipePacketManager.destroyPipeItemSync(item);
						}
						//and clear old pipe items map
						pipeToDestroy.pipeItems.clear();

						//update neighbor pipes
						for (PipeDirection dir : PipeDirection.values()) {
							BlockLoc blockLocLong = TransportPipes.convertBlockLoc(pipeToDestroy.blockLoc.clone().add(dir.getX(), dir.getY(), dir.getZ()));
							if (pipeMap.containsKey(blockLocLong)) {
								pipeMap.get(blockLocLong).updatePipeShape();
							}
						}

						pipeToDestroy.destroy(dropItem);

					}
				}

			}
		}, 0);
	}

	/**
	 * gets all pipe connection directions (not block connections)
	 */
	public static List<PipeDirection> getPipeConnections(final Location pipeLoc) {

		List<PipeDirection> dirs = new ArrayList<>();

		Map<BlockLoc, Pipe> pipeMap = TransportPipes.getPipeMap(pipeLoc.getWorld());
		if (pipeMap != null) {
			for (PipeDirection dir : PipeDirection.values()) {
				Location blockLoc = pipeLoc.clone().add(dir.getX(), dir.getY(), dir.getZ());
				if (pipeMap.containsKey(TransportPipes.convertBlockLoc(blockLoc))) {
					dirs.add(dir);
				}
			}
		}

		return dirs;

	}

	/**
	 * ONLY IN MAIN THREAD gets block connection dirs (not pipe connections)
	 */
	public static List<PipeDirection> getPipeNeighborBlocksSync(final Location pipeLoc) {
		List<PipeDirection> dirs = new ArrayList<>();
		for (PipeDirection dir : PipeDirection.values()) {
			Location blockLoc = pipeLoc.clone().add(dir.getX(), dir.getY(), dir.getZ());
			if (isIdInventoryHolder(blockLoc.getBlock().getTypeId())) {
				dirs.add(dir);
			}
		}
		return dirs;
	}

	/**
	 * calculates the shape of the pipe that will be used with all blocks and pipes around. Can be used for new pipe and to update a pipe
	 */
	public static Class<? extends Pipe> calculatePipeShapeWithDirList(List<PipeDirection> dirs) {
		Class<? extends Pipe> newPipeClass = null;

		synchronized (dirs) {

			if (dirs.size() == 0 || dirs.size() > 2) {
				newPipeClass = PipeMID.class;
			} else if (dirs.size() == 1) {
				if (dirs.get(0) == PipeDirection.NORTH || dirs.get(0) == PipeDirection.SOUTH) {
					newPipeClass = PipeNS.class;
				} else if (dirs.get(0) == PipeDirection.EAST || dirs.get(0) == PipeDirection.WEST) {
					newPipeClass = PipeEW.class;
				} else if (dirs.get(0) == PipeDirection.UP || dirs.get(0) == PipeDirection.DOWN) {
					newPipeClass = PipeUD.class;
				}
			} else {
				if (dirs.contains(PipeDirection.NORTH) && dirs.contains(PipeDirection.SOUTH)) {
					newPipeClass = PipeNS.class;
				} else if (dirs.contains(PipeDirection.EAST) && dirs.contains(PipeDirection.WEST)) {
					newPipeClass = PipeEW.class;
				} else if (dirs.contains(PipeDirection.UP) && dirs.contains(PipeDirection.DOWN)) {
					newPipeClass = PipeUD.class;
				} else {
					newPipeClass = PipeMID.class;
				}
			}

		}

		return newPipeClass;
	}

	/**
	 * updates the "pipeNeighborBlocks" list of all pipes around this block
	 * 
	 * @param add
	 *            true: block added | false: block removed
	 */
	public static void updatePipeNeighborBlockSync(Block toUpdate, boolean add) {

		Map<BlockLoc, Pipe> pipeMap = TransportPipes.getPipeMap(toUpdate.getWorld());

		if (pipeMap != null) {
			for (PipeDirection pd : PipeDirection.values()) {
				PipeDirection opposite = pd.getOpposite();
				Location blockLoc = toUpdate.getLocation().clone().add(pd.getX(), pd.getY(), pd.getZ());

				if (pipeMap.containsKey(TransportPipes.convertBlockLoc(blockLoc))) {
					final Pipe pipe = pipeMap.get(TransportPipes.convertBlockLoc(blockLoc));
					if (add) {
						//add pipe neighbor block
						if (!pipe.pipeNeighborBlocks.contains(opposite)) {
							pipe.pipeNeighborBlocks.add(opposite);
							PipeThread.runTask(new Runnable() {

								@Override
								public void run() {
									pipe.updatePipeShape();
								}
							}, 0);
						}
					} else {
						//remove pipe neighbor block
						if (pipe.pipeNeighborBlocks.contains(opposite)) {
							pipe.pipeNeighborBlocks.remove(opposite);
							PipeThread.runTask(new Runnable() {

								@Override
								public void run() {
									pipe.updatePipeShape();
								}

							}, 0);
						}
					}
				}
			}
		}
	}

	/**
	 * checks if this blockID is an InventoryHolder
	 */
	public static boolean isIdInventoryHolder(int id) {
		return id == 54 || id == 146 || id == 154 || id == 61 || id == 379 || id == 23 || id == 158 || id == 117;
	}

	public static String LocToString(Location loc) {
		return loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ();
	}

	public static Location StringToLoc(String loc) {
		try {
			return new Location(Bukkit.getWorld(loc.split(":")[0]), Double.parseDouble(loc.split(":")[1]), Double.parseDouble(loc.split(":")[2]), Double.parseDouble(loc.split(":")[3]));
		} catch (Exception e) {
			return null;
		}
	}

	public static Pipe getPipeWithLocation(Location blockLoc) {

		Map<BlockLoc, Pipe> pipeMap = TransportPipes.getPipeMap(blockLoc.getWorld());
		if (pipeMap != null) {
			if (pipeMap.containsKey(TransportPipes.convertBlockLoc(blockLoc))) {
				return pipeMap.get(TransportPipes.convertBlockLoc(blockLoc));
			}
		}
		return null;
	}

}
