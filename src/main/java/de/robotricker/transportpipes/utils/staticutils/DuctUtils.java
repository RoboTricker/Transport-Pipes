package de.robotricker.transportpipes.utils.staticutils;

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
import com.google.common.collect.Collections2;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.api.DuctRegistrationEvent;
import de.robotricker.transportpipes.api.DuctUnregistrationEvent;
import de.robotricker.transportpipes.api.PipeAPI;
import de.robotricker.transportpipes.api.PlayerDestroyDuctEvent;
import de.robotricker.transportpipes.api.PlayerPlaceDuctEvent;
import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctType;
import de.robotricker.transportpipes.duct.pipe.CraftingPipe;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.duct.pipe.utils.PipeColor;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.config.LocConf;
import de.robotricker.transportpipes.utils.ductdetails.DuctDetails;
import de.robotricker.transportpipes.utils.hitbox.TimingCloseable;

public class DuctUtils {

	public static Duct getDuctAtLocation(Location blockLoc) {
		try (TimingCloseable tc = new TimingCloseable("getDuctAtLocation")) {
			Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(blockLoc.getWorld());
			if (ductMap != null) {
				BlockLoc bl = BlockLoc.convertBlockLoc(blockLoc);
				if (ductMap.containsKey(bl)) {
					return ductMap.get(bl);
				}
			}
			return null;
		}
	}

	public static void updateNeighborDucts(final Location blockLoc) {
		TransportPipes.instance.pipeThread.runTask(new Runnable() {

			@Override
			public void run() {

				Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(blockLoc.getWorld());
				if (ductMap != null) {
					BlockLoc bl = BlockLoc.convertBlockLoc(blockLoc);
					Duct duct = ductMap.get(bl);
					for (WrappedDirection dir : WrappedDirection.values()) {
						BlockLoc neighborBl = BlockLoc.convertBlockLoc(blockLoc.clone().add(dir.getX(), dir.getY(), dir.getZ()));
						if (ductMap.containsKey(neighborBl)) {
							if (duct == null || ductMap.get(neighborBl).getDuctType() == duct.getDuctType()) {
								TransportPipes.instance.ductManager.updateDuct(ductMap.get(neighborBl));
							}
						}
					}
				}

			}
		}, 0);
	}

	public static boolean canBuild(Player p, Block b, Block placedAgainst, EquipmentSlot es) {
		for(String worldName : TransportPipes.instance.generalConf.getDisabledWorlds()) {
			if(worldName.equalsIgnoreCase(b.getWorld().getName())) {
				if(p != null) {
					p.sendMessage("§cPipes are disabled in this world.");
				}
				return false;
			}
		}
		try (TimingCloseable tc = new TimingCloseable("HitboxListener canbuild check")) {
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

			try (TimingCloseable tc2 = new TimingCloseable("HitboxListener blockbreakevent call")) {
				Bukkit.getPluginManager().callEvent(bbe);
			}

			// register anticheat listeners
			bbe.getHandlers().registerAll(unregisterListeners);
			return !bbe.isCancelled() || p.isOp();
		}
	}

	/**
	 * only registers the duct in the global ducts map + spawns the duct
	 * armorstands.
	 */
	public static void registerDuct(final Duct duct, final List<WrappedDirection> neighborPipes) {
		Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(duct.getBlockLoc().getWorld());
		if (ductMap == null) {
			ductMap = Collections.synchronizedMap(new TreeMap<BlockLoc, Duct>());
			TransportPipes.instance.getFullDuctMap().put(duct.getBlockLoc().getWorld(), ductMap);
		}
		ductMap.put(BlockLoc.convertBlockLoc(duct.getBlockLoc()), duct);

		final Collection<WrappedDirection> allConnections = new HashSet<>();
		allConnections.addAll(neighborPipes);
		allConnections.addAll(duct.getOnlyBlockConnections());

		TransportPipes.instance.pipeThread.runTask(new Runnable() {

			@Override
			public void run() {
				TransportPipes.instance.ductManager.createDuct(duct, allConnections);
			}
		}, 0);
		
		Bukkit.getPluginManager().callEvent(new DuctRegistrationEvent(duct));
	}

	/**
	 * invoke this if you want to build a new duct at this location. If there is a
	 * duct already, it will do nothing. Otherwise it will place the duct and send
	 * the packets to the players near. don't call this if you only want to update
	 * the duct! returns whether the duct could be placed.
	 * 
	 * Only call from bukkit thread!
	 */
	public static boolean buildDuct(Player player, final Location blockLoc, DuctDetails ductDetails) {
		// check if there is already a duct at this position
		Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(blockLoc.getWorld());
		if (ductMap != null) {
			if (ductMap.containsKey(BlockLoc.convertBlockLoc(blockLoc))) {
				// there already exists a duct at this location
				return false;
			}
		}

		// check if there is a block
		if (!(blockLoc.getBlock().getType() == Material.AIR || blockLoc.getBlock().isLiquid())) {
			return false;
		}

		Duct duct = ductDetails.createDuct(blockLoc);

		List<WrappedDirection> neighborPipes = duct.getOnlyConnectableDuctConnections();

		if (ductDetails.getDuctType() == DuctType.PIPE) {
			Map<BlockLoc, TransportPipesContainer> containerMap = TransportPipes.instance.getContainerMap(duct.getBlockLoc().getWorld());
			if (containerMap != null) {
				for (WrappedDirection dir : WrappedDirection.values()) {
					BlockLoc bl = BlockLoc.convertBlockLoc(duct.getBlockLoc().clone().add(dir.getX(), dir.getY(), dir.getZ()));
					if (containerMap.containsKey(bl)) {
						if (TransportPipes.isBlockProtectedByLWC(bl.toLocation(duct.getBlockLoc().getWorld()).getBlock())) {
							if (player != null) {
								player.sendMessage(LocConf.load(LocConf.LWC_ERROR));
							}
							return false;
						}
					}
				}
			}
		}

		if (player != null) {
			PlayerPlaceDuctEvent ppe = new PlayerPlaceDuctEvent(player, duct);
			Bukkit.getPluginManager().callEvent(ppe);
			if (!ppe.isCancelled()) {
				DuctUtils.registerDuct(duct, neighborPipes);
			} else {
				return false;
			}
		} else {
			DuctUtils.registerDuct(duct, neighborPipes);
		}

		DuctUtils.updateNeighborDucts(duct.getBlockLoc());

		return true;
	}

	/**
	 * invoke this if you want to destroy a duct. This will remove the duct from the
	 * ducts list and destroys it for all players
	 */
	public static void destroyDuct(final Player player, final Duct ductToDestroy, final boolean dropItems) {

		if (player != null) {
			PlayerDestroyDuctEvent pde = new PlayerDestroyDuctEvent(player, ductToDestroy);
			Bukkit.getPluginManager().callEvent(pde);
			if (pde.isCancelled()) {
				return;
			}
		}

		final Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(ductToDestroy.getBlockLoc().getWorld());
		if (ductMap != null) {
			// only remove the duct if it is in the duct list!
			if (ductMap.containsKey(BlockLoc.convertBlockLoc(ductToDestroy.getBlockLoc()))) {

				TransportPipes.instance.ductManager.destroyDuct(ductToDestroy);

				ductMap.remove(BlockLoc.convertBlockLoc(ductToDestroy.getBlockLoc()));

				if (ductToDestroy.getDuctType() == DuctType.PIPE) {
					Pipe pipeToDestroy = (Pipe) ductToDestroy;
					// drop all items in old pipe
					synchronized (pipeToDestroy.pipeItems) {
						Set<PipeItem> itemsToDrop = new HashSet<>();
						itemsToDrop.addAll(pipeToDestroy.pipeItems.keySet());
						itemsToDrop.addAll(pipeToDestroy.tempPipeItems.keySet());
						itemsToDrop.addAll(pipeToDestroy.tempPipeItemsWithSpawn.keySet());
						for (final PipeItem item : itemsToDrop) {
							TransportPipes.runTask(new Runnable() {

								@Override
								public void run() {
									ductToDestroy.getBlockLoc().getWorld().dropItem(ductToDestroy.getBlockLoc().clone().add(0.5d, 0.5d, 0.5d), item.getItem());
								}
							});
							// destroy item for players
							TransportPipes.instance.ductManager.destroyPipeItem(item);
						}
						// and clear old pipe items map
						pipeToDestroy.pipeItems.clear();
						pipeToDestroy.tempPipeItems.clear();
						pipeToDestroy.tempPipeItemsWithSpawn.clear();
					}
				}

				updateNeighborDucts(ductToDestroy.getBlockLoc());

				final List<ItemStack> droppedItems = ductToDestroy.getDroppedItems();
				TransportPipes.runTask(new Runnable() {

					@Override
					public void run() {
						if ((player != null && player.getGameMode() != GameMode.CREATIVE) || dropItems) {
							Location dropLoc = ductToDestroy.getBlockLoc().clone().add(0.5d, 0.5d, 0.5d);
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
							wrapper.setX(ductToDestroy.getBlockLoc().getBlockX() + 0.5f);
							wrapper.setY(ductToDestroy.getBlockLoc().getBlockY() + 0.5f);
							wrapper.setZ(ductToDestroy.getBlockLoc().getBlockZ() + 0.5f);
							wrapper.setOffsetX(0.25f);
							wrapper.setOffsetY(0.25f);
							wrapper.setOffsetZ(0.25f);
							wrapper.setParticleData(0.05f);
							wrapper.setData(ductToDestroy.getBreakParticleData());
							for (Player worldPl : ductToDestroy.getBlockLoc().getWorld().getPlayers()) {
								wrapper.sendPacket(worldPl);
							}
						}
					}
				});
				
				Bukkit.getPluginManager().callEvent(new DuctUnregistrationEvent(ductToDestroy));
			}
		}

	}

}
