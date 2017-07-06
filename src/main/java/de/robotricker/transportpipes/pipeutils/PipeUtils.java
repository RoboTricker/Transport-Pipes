package de.robotricker.transportpipes.pipeutils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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

		List<PipeDirection> neighborPipes = getOnlyPipeConnections(pipe);

		if (player != null) {
			PlayerPlacePipeEvent ppe = new PlayerPlacePipeEvent(player, pipe);
			Bukkit.getPluginManager().callEvent(ppe);
			if (!ppe.isCancelled()) {
				TransportPipes.putPipe(pipe, neighborPipes);
			} else {
				return false;
			}
		} else {
			TransportPipes.putPipe(pipe, neighborPipes);
		}

		updatePipeNeighborPipes(pipe.getBlockLoc());

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

				TransportPipes.pipePacketManager.destroyPipe(pipeToDestroy);

				pipeMap.remove(TransportPipes.convertBlockLoc(pipeToDestroy.blockLoc));

				//drop all items in old pipe
				Set<PipeItem> itemsToDrop = new HashSet<PipeItem>();
				itemsToDrop.addAll(pipeToDestroy.pipeItems.keySet());
				itemsToDrop.addAll(pipeToDestroy.tempPipeItems.keySet());
				itemsToDrop.addAll(pipeToDestroy.tempPipeItemsWithSpawn.keySet());
				for (final PipeItem item : itemsToDrop) {
					Bukkit.getScheduler().runTask(TransportPipes.instance, new Runnable() {

						@Override
						public void run() {
							pipeToDestroy.blockLoc.getWorld().dropItem(pipeToDestroy.blockLoc.clone().add(0.5d, 0.5d, 0.5d), item.getItem());
						}
					});
					//destroy item for players
					TransportPipes.pipePacketManager.destroyPipeItem(item);
				}
				//and clear old pipe items map
				pipeToDestroy.pipeItems.clear();
				pipeToDestroy.tempPipeItems.clear();
				pipeToDestroy.tempPipeItemsWithSpawn.clear();

				updatePipeNeighborPipes(pipeToDestroy.blockLoc);

				if (dropItem) {
					final List<ItemStack> droppedItems = pipeToDestroy.getDroppedItems();
					Bukkit.getScheduler().runTask(TransportPipes.instance, new Runnable() {

						@Override
						public void run() {
							Location dropLoc = pipeToDestroy.blockLoc.clone().add(0.5d, 0.5d, 0.5d);
							for (ItemStack dropIs : droppedItems) {
								dropLoc.getWorld().dropItem(dropLoc, dropIs);
							}
						}
					});
				}
			}
		}

	}

	public static void updatePipeNeighborPipes(final Location blockLoc) {
		PipeThread.runTask(new Runnable() {

			@Override
			public void run() {

				Map<BlockLoc, Pipe> pipeMap = TransportPipes.getPipeMap(blockLoc.getWorld());
				if (pipeMap != null) {
					for (PipeDirection dir : PipeDirection.values()) {
						BlockLoc blockLocLong = TransportPipes.convertBlockLoc(blockLoc.clone().add(dir.getX(), dir.getY(), dir.getZ()));
						if (pipeMap.containsKey(blockLocLong)) {
							TransportPipes.pipePacketManager.updatePipe(pipeMap.get(blockLocLong));
						}
					}
				}

			}
		}, 0);
	}

	/**
	 * gets all pipe connection directions (not block connections)
	 **/
	public static List<PipeDirection> getOnlyPipeConnections(Pipe pipe) {

		List<PipeDirection> dirs = new ArrayList<PipeDirection>();

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

}
