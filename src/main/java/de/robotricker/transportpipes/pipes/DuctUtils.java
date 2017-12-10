package de.robotricker.transportpipes.pipes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.RegisteredListener;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipes.types.Pipe;
import de.robotricker.transportpipes.pipeutils.hitbox.TimingCloseable;

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
							if (duct == null || ductMap.get(neighborBl).getClass().isInstance(duct)) {
								TransportPipes.instance.pipePacketManager.updateDuct(ductMap.get(neighborBl));
							}
						}
					}
				}

			}
		}, 0);
	}

	public static boolean canBuild(Player p, Block b, Block placedAgainst, EquipmentSlot es) {
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
				TransportPipes.instance.pipePacketManager.createDuct(duct, allConnections);
			}
		}, 0);
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
