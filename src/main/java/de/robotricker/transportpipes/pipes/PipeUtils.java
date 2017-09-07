package de.robotricker.transportpipes.pipes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredListener;

import com.comphenix.packetwrapper.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers.Particle;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import de.robotricker.transportpipes.PipeThread;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.api.PlayerDestroyPipeEvent;
import de.robotricker.transportpipes.api.PlayerPlacePipeEvent;
import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipes.colored.PipeColor;
import de.robotricker.transportpipes.pipes.types.ColoredPipe;
import de.robotricker.transportpipes.pipes.types.Pipe;

public class PipeUtils {

	/**
	 * invoke this if you want to build a new pipe at this location. If there is a
	 * pipe already, it will do nothing. Otherwise it will place the pipe and send
	 * the packets to the players near. don't call this if you only want to update
	 * the pipe! returns whether the pipe could be placed
	 */
	public static boolean buildPipe(Player player, final Location blockLoc, PipeType pt, PipeColor pipeColor) {

		// check if there is already a pipe at this position
		Map<BlockLoc, Pipe> pipeMap = TransportPipes.instance.getPipeMap(blockLoc.getWorld());
		if (pipeMap != null) {
			if (pipeMap.containsKey(BlockLoc.convertBlockLoc(blockLoc))) {
				// there already exists a pipe at this location
				return false;
			}
		}

		// check if there is a block
		if (!(blockLoc.getBlock().getType() == Material.AIR || blockLoc.getBlock().isLiquid())) {
			return false;
		}

		Pipe pipe = pt.createPipe(blockLoc, pipeColor);

		List<PipeDirection> neighborPipes = getOnlyPipeConnections(pipe);

		if (player != null) {
			PlayerPlacePipeEvent ppe = new PlayerPlacePipeEvent(player, pipe);
			Bukkit.getPluginManager().callEvent(ppe);
			if (!ppe.isCancelled()) {
				registerPipe(pipe, neighborPipes);
			} else {
				return false;
			}
		} else {
			registerPipe(pipe, neighborPipes);
		}

		updateNeighborPipes(pipe.getBlockLoc());

		return true;

	}

	/**
	 * invoke this if you want to destroy a pipe. This will remove the pipe from the
	 * pipe list and destroys it for all players
	 */
	public static void destroyPipe(final Player player, final Pipe pipeToDestroy) {

		if (player != null) {
			PlayerDestroyPipeEvent pde = new PlayerDestroyPipeEvent(player, pipeToDestroy);
			Bukkit.getPluginManager().callEvent(pde);
			if (pde.isCancelled()) {
				return;
			}
		}

		final Map<BlockLoc, Pipe> pipeMap = TransportPipes.instance.getPipeMap(pipeToDestroy.getBlockLoc().getWorld());
		if (pipeMap != null) {
			// only remove the pipe if it is in the pipe list!
			if (pipeMap.containsKey(BlockLoc.convertBlockLoc(pipeToDestroy.getBlockLoc()))) {

				TransportPipes.instance.pipePacketManager.destroyPipe(pipeToDestroy);

				pipeMap.remove(BlockLoc.convertBlockLoc(pipeToDestroy.getBlockLoc()));

				// drop all items in old pipe
				synchronized (pipeToDestroy.pipeItems) {
					Set<PipeItem> itemsToDrop = new HashSet<>();
					itemsToDrop.addAll(pipeToDestroy.pipeItems.keySet());
					itemsToDrop.addAll(pipeToDestroy.tempPipeItems.keySet());
					itemsToDrop.addAll(pipeToDestroy.tempPipeItemsWithSpawn.keySet());
					for (final PipeItem item : itemsToDrop) {
						Bukkit.getScheduler().runTask(TransportPipes.instance, new Runnable() {

							@Override
							public void run() {
								pipeToDestroy.getBlockLoc().getWorld().dropItem(pipeToDestroy.getBlockLoc().clone().add(0.5d, 0.5d, 0.5d), item.getItem());
							}
						});
						// destroy item for players
						TransportPipes.instance.pipePacketManager.destroyPipeItem(item);
					}
					// and clear old pipe items map
					pipeToDestroy.pipeItems.clear();
					pipeToDestroy.tempPipeItems.clear();
					pipeToDestroy.tempPipeItemsWithSpawn.clear();
				}

				updateNeighborPipes(pipeToDestroy.getBlockLoc());

				final List<ItemStack> droppedItems = pipeToDestroy.getDroppedItems();
				Bukkit.getScheduler().runTask(TransportPipes.instance, new Runnable() {

					@Override
					public void run() {
						if (player != null && player.getGameMode() != GameMode.CREATIVE) {
							Location dropLoc = pipeToDestroy.getBlockLoc().clone().add(0.5d, 0.5d, 0.5d);
							for (ItemStack dropIs : droppedItems) {
								dropLoc.getWorld().dropItem(dropLoc, dropIs);
							}
						}
						if (player != null) {
							// show break particles
							WrapperPlayServerWorldParticles wrapper = new WrapperPlayServerWorldParticles();
							wrapper.setParticleType(Particle.ITEM_CRACK);
							wrapper.setNumberOfParticles(30);
							wrapper.setLongDistance(false);
							wrapper.setX(pipeToDestroy.getBlockLoc().getBlockX() + 0.5f);
							wrapper.setY(pipeToDestroy.getBlockLoc().getBlockY() + 0.5f);
							wrapper.setZ(pipeToDestroy.getBlockLoc().getBlockZ() + 0.5f);
							wrapper.setOffsetX(0.25f);
							wrapper.setOffsetY(0.25f);
							wrapper.setOffsetZ(0.25f);
							wrapper.setParticleData(0.05f);
							wrapper.setData(pipeToDestroy.getBreakParticleData());
							for (Player worldPl : pipeToDestroy.getBlockLoc().getWorld().getPlayers()) {
								wrapper.sendPacket(worldPl);
							}
						}
					}
				});
			}
		}

	}

	public static void updateNeighborPipes(final Location blockLoc) {
		TransportPipes.instance.pipeThread.runTask(new Runnable() {

			@Override
			public void run() {

				Map<BlockLoc, Pipe> pipeMap = TransportPipes.instance.getPipeMap(blockLoc.getWorld());
				if (pipeMap != null) {
					for (PipeDirection dir : PipeDirection.values()) {
						BlockLoc blockLocLong = BlockLoc.convertBlockLoc(blockLoc.clone().add(dir.getX(), dir.getY(), dir.getZ()));
						if (pipeMap.containsKey(blockLocLong)) {
							TransportPipes.instance.pipePacketManager.updatePipe(pipeMap.get(blockLocLong));
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

		List<PipeDirection> dirs = new ArrayList<>();

		Map<BlockLoc, Pipe> pipeMap = TransportPipes.instance.getPipeMap(pipe.getBlockLoc().getWorld());

		if (pipeMap != null) {
			for (PipeDirection dir : PipeDirection.values()) {
				Location blockLoc = pipe.getBlockLoc().clone().add(dir.getX(), dir.getY(), dir.getZ());
				BlockLoc bl = BlockLoc.convertBlockLoc(blockLoc);
				if (pipeMap.containsKey(bl)) {
					Pipe connectedPipe = pipeMap.get(bl);
					if (connectedPipe.getPipeType() == PipeType.EXTRACTION && pipe.getPipeType() == PipeType.EXTRACTION) {
						continue;
					}
					if (connectedPipe.getPipeType() == PipeType.VOID && pipe.getPipeType() == PipeType.VOID) {
						continue;
					}
					if (connectedPipe.getPipeType() == PipeType.COLORED && pipe.getPipeType() == PipeType.COLORED) {
						if (((ColoredPipe) connectedPipe).getPipeColor().equals(((ColoredPipe) pipe).getPipeColor())) {
							dirs.add(dir);
						}
						continue;
					}
					dirs.add(dir);
				}
			}
		}

		return dirs;

	}

	/**
	 * gets all block connection directions (not pipe connections)
	 **/
	public static List<PipeDirection> getOnlyBlockConnections(Pipe pipe) {

		List<PipeDirection> dirs = new ArrayList<>();

		Map<BlockLoc, TransportPipesContainer> containerMap = TransportPipes.instance.getContainerMap(pipe.getBlockLoc().getWorld());

		if (containerMap != null) {
			for (PipeDirection dir : PipeDirection.values()) {
				Location blockLoc = pipe.getBlockLoc().clone().add(dir.getX(), dir.getY(), dir.getZ());
				BlockLoc bl = BlockLoc.convertBlockLoc(blockLoc);
				if (containerMap.containsKey(bl)) {
					dirs.add(dir);
				}
			}
		}

		return dirs;

	}

	public static Pipe getPipeWithLocation(Location blockLoc) {
		Map<BlockLoc, Pipe> pipeMap = TransportPipes.instance.getPipeMap(blockLoc.getWorld());
		if (pipeMap != null) {
			BlockLoc bl = BlockLoc.convertBlockLoc(blockLoc);
			if (pipeMap.containsKey(bl)) {
				return pipeMap.get(bl);
			}
		}
		return null;
	}

	public static boolean canBuild(Player p, Block b, Block placedAgainst, EquipmentSlot es) {
		try (Timing timed2 = Timings.ofStart(TransportPipes.instance, "HitboxListener canBuild check")) {
			BlockBreakEvent bbe = new BlockBreakEvent(b, p);

			// unregister anticheat listeners
			List<RegisteredListener> unregisterListeners = new ArrayList<>();
			for (RegisteredListener rl : bbe.getHandlers().getRegisteredListeners()) {
				for (String antiCheat : TransportPipes.instance.generalConf.getAnticheatPlugins()) {
					if (rl.getPlugin().getName().equalsIgnoreCase(antiCheat)) {
						unregisterListeners.add(rl);
					}
				}
				if (rl.getListener().equals(TransportPipes.instance.containerBlockUtils)) {
					unregisterListeners.add(rl);
				}
			}
			for (RegisteredListener rl : unregisterListeners) {
				bbe.getHandlers().unregister(rl);
			}

			try (Timing timed3 = Timings.ofStart(TransportPipes.instance, "HitboxListener BlockBreakEvent call")) {
				Bukkit.getPluginManager().callEvent(bbe);
			}

			// register anticheat listeners
			bbe.getHandlers().registerAll(unregisterListeners);
			return !bbe.isCancelled() || p.isOp();
		}
	}

	public static void registerPipe(final Pipe pipe, final List<PipeDirection> neighborPipes) {
		Map<BlockLoc, Pipe> pipeMap = TransportPipes.instance.getPipeMap(pipe.getBlockLoc().getWorld());
		if (pipeMap == null) {
			pipeMap = Collections.synchronizedMap(new TreeMap<BlockLoc, Pipe>());
			TransportPipes.instance.getFullPipeMap().put(pipe.getBlockLoc().getWorld(), pipeMap);
		}
		pipeMap.put(BlockLoc.convertBlockLoc(pipe.getBlockLoc()), pipe);

		Bukkit.getScheduler().runTask(TransportPipes.instance, new Runnable() {

			@Override
			public void run() {

				final Collection<PipeDirection> allConnections = new HashSet<>();
				allConnections.addAll(neighborPipes);

				// update container blocks sync
				Map<BlockLoc, TransportPipesContainer> containerMap = TransportPipes.instance.getContainerMap(pipe.getBlockLoc().getWorld());
				for (PipeDirection pd : PipeDirection.values()) {
					Location blockLoc = pipe.getBlockLoc().clone().add(pd.getX(), pd.getY(), pd.getZ());
					if (containerMap != null && containerMap.containsKey(BlockLoc.convertBlockLoc(blockLoc))) {
						allConnections.add(pd);
					}
				}

				TransportPipes.instance.pipeThread.runTask(new Runnable() {

					@Override
					public void run() {
						TransportPipes.instance.pipePacketManager.createPipe(pipe, allConnections);
					}
				}, 0);
			}
		});
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
