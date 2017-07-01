package de.robotricker.transportpipes.pipeutils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.PipeThread;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.TransportPipes.BlockLoc;
import de.robotricker.transportpipes.api.PlayerDestroyPipeEvent;
import de.robotricker.transportpipes.api.PlayerPlacePipeEvent;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipes.ColoredPipe;
import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipes.PipeType;

public class PipeUtils {

	/**
	 * invoke this if you want to build a new pipe at this location. If there is a pipe already, it will do nothing. Otherwise it will place the pipe and send the packets to the players near. don't call this if you only want to update the pipe! returns whether the pipe could be placed
	 */
	public static boolean buildPipe(Player player, final Location blockLoc, PipeType pt, PipeColor pipeColor) {

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

		Pipe pipe = pt.createPipe(blockLoc, pipeColor);

		if (player != null) {
			PlayerPlacePipeEvent ppe = new PlayerPlacePipeEvent(player, pipe);
			Bukkit.getPluginManager().callEvent(ppe);
			if (!ppe.isCancelled()) {
				TransportPipes.putPipe(pipe);
				TransportPipes.vanillaPipeManager.sendPipe(pipe);
				TransportPipes.modelledPipeManager.sendPipe(pipe);
				TransportPipes.pipePacketManager.spawnPipeSync(pipe);
			} else {
				return false;
			}
		} else {
			TransportPipes.putPipe(pipe);
			TransportPipes.vanillaPipeManager.sendPipe(pipe);
			TransportPipes.modelledPipeManager.sendPipe(pipe);
			TransportPipes.pipePacketManager.spawnPipeSync(pipe);
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
							TransportPipes.vanillaPipeManager.updatePipe(pipeMap.get(blockLocLong));
							TransportPipes.modelledPipeManager.updatePipe(pipeMap.get(blockLocLong));
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

				TransportPipes.vanillaPipeManager.destroyPipe(pipeToDestroy);
				TransportPipes.modelledPipeManager.destroyPipe(pipeToDestroy);

				pipeMap.remove(TransportPipes.convertBlockLoc(pipeToDestroy.blockLoc));

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
								TransportPipes.vanillaPipeManager.updatePipe(pipeMap.get(blockLocLong));
								TransportPipes.modelledPipeManager.updatePipe(pipeMap.get(blockLocLong));
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
	 *            determines if the pipe at pipeLoc (for which the neighbor pipe check is done) is a normal pipe or a special pipe (golden, iron or detector pipe)
	 */
	public static List<PipeDirection> getPipeConnections(Pipe pipe) {

		List<PipeDirection> dirs = new ArrayList<>();

		Map<BlockLoc, Pipe> pipeMap = TransportPipes.getPipeMap(pipe.getBlockLoc().getWorld());

		if (pipeMap != null) {
			for (PipeDirection dir : PipeDirection.values()) {
				Location blockLoc = pipe.getBlockLoc().clone().add(dir.getX(), dir.getY(), dir.getZ());
				BlockLoc bl = TransportPipes.convertBlockLoc(blockLoc);
				if (pipeMap.containsKey(bl)) {
					Pipe connectedPipe = pipeMap.get(bl);
					if (connectedPipe.getPipeType() == PipeType.COLORED && pipe.getPipeType() == PipeType.COLORED) {
						if (((ColoredPipe) connectedPipe).getPipeColor().equals(((ColoredPipe) pipe).getPipeColor())) {
							dirs.add(dir);
						}
					} else {
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
									TransportPipes.vanillaPipeManager.updatePipe(pipe);
									TransportPipes.modelledPipeManager.updatePipe(pipe);
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
									TransportPipes.vanillaPipeManager.updatePipe(pipe);
									TransportPipes.modelledPipeManager.updatePipe(pipe);
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

	public static Pipe createPipeObject(Class<? extends Pipe> pipeClass, Location loc, List list, PipeColor pipeColor, boolean icePipe) {
		try {
			return pipeClass.getConstructor(Location.class, List.class, PipeColor.class, boolean.class).newInstance(loc, list, pipeColor, icePipe);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean isPipeItemIcePipe(ItemStack pipeItem) {
		if (pipeItem.hasItemMeta() && pipeItem.getItemMeta().hasDisplayName()) {
			return pipeItem.getItemMeta().getDisplayName().equals(TransportPipes.instance.ICE_PIPE_NAME);
		}
		return false;
	}

}
