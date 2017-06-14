package de.robotricker.transportpipes.pipeutils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import de.robotricker.transportpipes.PipeThread;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.TransportPipes.BlockLoc;
import de.robotricker.transportpipes.api.PlayerDestroyPipeEvent;
import de.robotricker.transportpipes.api.PlayerPlacePipeEvent;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipes.GoldenPipe;
import de.robotricker.transportpipes.pipes.IronPipe;
import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipes.PipeEW;
import de.robotricker.transportpipes.pipes.PipeMID;
import de.robotricker.transportpipes.pipes.PipeNS;
import de.robotricker.transportpipes.pipes.PipeUD;

public class PipeUtils {

	public static boolean buildPipe(Player player, Location blockLoc, PipeColor pipeColor) {
		return buildPipe(player, blockLoc, Pipe.class, pipeColor);
	}

	/**
	 * invoke this if you want to build a new pipe at this location. If there is a pipe already, it will do nothing. Otherwise it will place the pipe and send the packets to the players near. don't call this if you only want to update the pipe! returns whether the pipe could be placed
	 */
	public static boolean buildPipe(Player player, final Location blockLoc, Class<? extends Pipe> pipeClass, PipeColor pipeColor) {

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
		final List<PipeDirection> pipeConnections = getPipeConnections(blockLoc, pipeColor, pipeClass == null || pipeClass == Pipe.class);
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
				pipe = createPipeObject(newPipeClass, blockLoc, pipeNeighborBlocks, pipeColor);

				if (player != null) {
					PlayerPlacePipeEvent ppe = new PlayerPlacePipeEvent(player, pipe);
					Bukkit.getPluginManager().callEvent(ppe);
					if (!ppe.isCancelled()) {
						TransportPipes.putPipe(pipe);
						TransportPipes.pipePacketManager.spawnPipeSync(pipe);
					} else {
						return false;
					}
				} else {
					TransportPipes.putPipe(pipe);
					TransportPipes.pipePacketManager.spawnPipeSync(pipe);
				}

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
	public static void destroyPipe(Player player, final Pipe pipeToDestroy, final boolean dropItem) {

		if (player != null) {
			PlayerDestroyPipeEvent pde = new PlayerDestroyPipeEvent(player, pipeToDestroy);
			Bukkit.getPluginManager().callEvent(pde);
			if (pde.isCancelled()) {
				return;
			}
		}

		final Map<BlockLoc, Pipe> pipeMap = TransportPipes.getPipeMap(pipeToDestroy.blockLoc.getWorld());
		if (pipeMap != null) {
			//only remove the pipe if it is in the pipe list!
			if (pipeMap.containsKey(TransportPipes.convertBlockLoc(pipeToDestroy.blockLoc))) {
				pipeMap.remove(TransportPipes.convertBlockLoc(pipeToDestroy.blockLoc));

				TransportPipes.pipePacketManager.destroyPipeSync(pipeToDestroy);

				//drop all items in old pipe
				List<PipeItem> itemsToDrop = new ArrayList<PipeItem>();
				itemsToDrop.addAll(pipeToDestroy.pipeItems.keySet());
				for (PipeItem item : pipeToDestroy.tempPipeItems.keySet()) {
					if (!itemsToDrop.contains(item)) {
						itemsToDrop.add(item);
					}
				}
				for (PipeItem item : pipeToDestroy.tempPipeItemsWithSpawn.keySet()) {
					if (!itemsToDrop.contains(item)) {
						itemsToDrop.add(item);
					}
				}
				for (final PipeItem item : itemsToDrop) {
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
				pipeToDestroy.tempPipeItems.clear();
				pipeToDestroy.tempPipeItemsWithSpawn.clear();

				PipeThread.runTask(new Runnable() {

					@Override
					public void run() {
						//update neighbor pipes
						for (PipeDirection dir : PipeDirection.values()) {
							BlockLoc blockLocLong = TransportPipes.convertBlockLoc(pipeToDestroy.blockLoc.clone().add(dir.getX(), dir.getY(), dir.getZ()));
							if (pipeMap.containsKey(blockLocLong)) {
								pipeMap.get(blockLocLong).updatePipeShape();
							}
						}
					}
				}, 0);

				pipeToDestroy.destroy(dropItem);

			}
		}

	}

	/**
	 * gets all pipe connection directions (not block connections)
	 * 
	 * @param normalPipe
	 *            determines if the pipe at pipeLoc (for which the neighbor pipe check is done) is a normal pipe or a special pipe (golden, iron pipe)
	 */
	public static List<PipeDirection> getPipeConnections(Location pipeLoc, PipeColor pipeColor, boolean normalPipe) {

		List<PipeDirection> dirs = new ArrayList<>();

		Map<BlockLoc, Pipe> pipeMap = TransportPipes.getPipeMap(pipeLoc.getWorld());

		if (pipeColor == null) {
			BlockLoc pipeLocBlockLoc = TransportPipes.convertBlockLoc(pipeLoc);
			pipeColor = pipeMap.containsKey(pipeLocBlockLoc) ? pipeMap.get(pipeLocBlockLoc).getPipeColor() : null;
		}
		if (pipeMap != null) {
			for (PipeDirection dir : PipeDirection.values()) {
				Location blockLoc = pipeLoc.clone().add(dir.getX(), dir.getY(), dir.getZ());
				BlockLoc bl = TransportPipes.convertBlockLoc(blockLoc);
				if (pipeMap.containsKey(bl)) {
					Pipe connectedPipe = pipeMap.get(bl);
					if (connectedPipe instanceof GoldenPipe || connectedPipe instanceof IronPipe || !normalPipe) {
						dirs.add(dir);
					} else if (pipeColor == null || connectedPipe.getPipeColor().equals(pipeColor)) {
						dirs.add(dir);
					}
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
		boolean v1_9or1_10 = Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10");
		return id == 54 || id == 146 || id == 154 || id == 61 || id == 62 || id == 379 || id == 23 || id == 158 || id == 117 || (!v1_9or1_10 && id >= 219 && id <= 234);
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
			BlockLoc bl = TransportPipes.convertBlockLoc(blockLoc);
			if (pipeMap.containsKey(bl)) {
				return pipeMap.get(bl);
			}
		}
		return null;
	}

	public static Pipe createPipeObject(Class<? extends Pipe> pipeClass, Location loc, List list, PipeColor pipeColor) {
		try {
			return pipeClass.getConstructor(Location.class, List.class, PipeColor.class).newInstance(loc, list, pipeColor);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
