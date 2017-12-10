package de.robotricker.transportpipes.protocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.robotricker.transportpipes.PipeThread;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipes.BlockLoc;
import de.robotricker.transportpipes.pipes.Duct;
import de.robotricker.transportpipes.pipes.WrappedDirection;
import de.robotricker.transportpipes.pipes.types.Pipe;
import de.robotricker.transportpipes.pipeutils.LocationUtils;
import de.robotricker.transportpipes.pipeutils.hitbox.OcclusionCullingUtils;
import de.robotricker.transportpipes.rendersystem.RenderSystem;
import de.robotricker.transportpipes.settings.SettingsUtils;
import io.sentry.Sentry;

public class PipePacketManager implements Listener {

	private Map<Player, List<Duct>> ductsForPlayers;
	private Map<Player, List<PipeItem>> itemsForPlayers;

	public PipePacketManager() {
		ductsForPlayers = Collections.synchronizedMap(new HashMap<Player, List<Duct>>());
		itemsForPlayers = Collections.synchronizedMap(new HashMap<Player, List<PipeItem>>());
	}

	public void createDuct(Duct duct, Collection<WrappedDirection> allConnections) {
		// notify pipe that some connections might have changed. Knowing this the iron
		// pipe can change its output direction for example.
		duct.notifyConnectionsChange();

		for (RenderSystem pm : duct.getRenderSystems()) {
			pm.createDuctASD(duct, allConnections);
		}
		// client update is done in the next tick
	}

	public void updateDuct(Duct duct) {
		// notify pipe that some connections might have changed. Knowing this the iron
		// pipe can change its output direction for example.
		duct.notifyConnectionsChange();

		for (RenderSystem pm : duct.getRenderSystems()) {
			pm.updateDuctASD(duct);
		}
		// client update is done inside the PipeManager method call (that's because here
		// you don't know which ArmorStands to remove and which ones to add)
	}

	public void destroyDuct(Duct duct) {
		for (Player p : ductsForPlayers.keySet()) {
			despawnDuct(p, duct);
		}
		for (RenderSystem pm : duct.getRenderSystems()) {
			pm.destroyDuctASD(duct);
		}
	}

	public void createPipeItem(PipeItem pipeItem) {
		try {
			List<Player> playerList = LocationUtils.getPlayerList(pipeItem.getBlockLoc().getWorld());
			for (Player on : playerList) {
				if (on.getWorld().equals(pipeItem.getBlockLoc().getWorld())) {
					if (pipeItem.getBlockLoc().distance(on.getLocation()) <= TransportPipes.instance.settingsUtils.getOrLoadPlayerSettings(on).getRenderDistance()) {
						spawnItem(on, pipeItem);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Sentry.capture(e);
		}
	}

	public void updatePipeItem(PipeItem pipeItem) {
		try {
			List<Player> playerList = LocationUtils.getPlayerList(pipeItem.getBlockLoc().getWorld());
			for (Player on : playerList) {
				if (on.getWorld().equals(pipeItem.getBlockLoc().getWorld())) {
					if (pipeItem.getBlockLoc().distance(on.getLocation()) <= TransportPipes.instance.settingsUtils.getOrLoadPlayerSettings(on).getRenderDistance()) {
						TransportPipes.instance.armorStandProtocol.updatePipeItem(on, pipeItem);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Sentry.capture(e);
		}
	}

	public void destroyPipeItem(PipeItem pipeItem) {
		for (Player p : itemsForPlayers.keySet()) {
			despawnItem(p, pipeItem);
		}
	}

	public void spawnDuct(final Player p, final Duct duct) {
		List<Duct> list;
		if (!ductsForPlayers.containsKey(p)) {
			ductsForPlayers.put(p, new ArrayList<Duct>());
		}
		list = ductsForPlayers.get(p);
		if (!list.contains(duct)) {
			List<ArmorStandData> ASD = TransportPipes.instance.armorStandProtocol.getPlayerPipeRenderSystem(p).getASDForPipe(pipe);
			if (ASD != null && !ASD.isEmpty()) {
				list.add(pipe);
				TransportPipes.instance.armorStandProtocol.sendArmorStandDatas(p, pipe.getBlockLoc(), ASD);
			}
		}
	}

	public void spawnItem(final Player p, final PipeItem item) {
		List<PipeItem> list;
		if (!itemsForPlayers.containsKey(p)) {
			itemsForPlayers.put(p, new ArrayList<PipeItem>());
		}
		list = itemsForPlayers.get(p);
		if (!list.contains(item)) {
			list.add(item);
			TransportPipes.instance.armorStandProtocol.sendPipeItem(p, item);
		}
	}

	public void despawnDuct(final Player p, final Duct duct) {
		if (ductsForPlayers.containsKey(p)) {
			List<Pipe> list = ductsForPlayers.get(p);
			if (list.contains(pipe)) {
				list.remove(pipe);
				TransportPipes.instance.armorStandProtocol.removeArmorStandDatas(p, TransportPipes.instance.armorStandProtocol.getPlayerPipeRenderSystem(p).getASDIdsForDuct(pipe));
			}
		}
	}

	public void despawnItem(final Player p, final PipeItem item) {
		if (itemsForPlayers.containsKey(p)) {
			List<PipeItem> list = itemsForPlayers.get(p);
			if (list.contains(item)) {
				list.remove(item);
				TransportPipes.instance.armorStandProtocol.removePipeItem(p, item);
			}
		}
	}

	public void tickSync() {

		for (World world : Bukkit.getWorlds()) {
			Map<BlockLoc, Pipe> pipeMap = TransportPipes.instance.getPipeMap(world);
			if (pipeMap != null) {
				synchronized (pipeMap) {
					for (Pipe pipe : pipeMap.values()) {
						try {
							List<Player> playerList = LocationUtils.getPlayerList(world);
							for (Player on : playerList) {
								if (!pipe.getBlockLoc().getWorld().equals(on.getWorld())) {
									continue;
								}
								if (pipe.getBlockLoc().distance(on.getLocation()) <= TransportPipes.instance.settingsUtils.getOrLoadPlayerSettings(on).getRenderDistance()) {
									if (OcclusionCullingUtils.isPipeVisibleForPlayer(on, pipe)) {
										// spawn pipe if not spawned
										spawnPipe(on, pipe);
									} else {
										// destroy pipe if spawned
										despawnPipe(on, pipe);
									}
								} else {
									// destroy pipe if spawned
									despawnPipe(on, pipe);
								}

								synchronized (pipe.pipeItems) {
									for (int i2 = 0; i2 < pipe.pipeItems.size(); i2++) {
										PipeItem item = pipe.pipeItems.keySet().toArray(new PipeItem[0])[i2];
										if (!item.getBlockLoc().getWorld().equals(on.getWorld())) {
											continue;
										}
										if (item.getBlockLoc().distance(on.getLocation()) <= TransportPipes.instance.settingsUtils.getOrLoadPlayerSettings(on).getRenderDistance()) {
											if (OcclusionCullingUtils.isPipeItemVisibleForPlayer(on, item)) {
												spawnItem(on, item);
											} else {
												despawnItem(on, item);
											}
										} else {
											despawnItem(on, item);
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

			}
		}

		OcclusionCullingUtils.clearCachedChunkSnapshots();

	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		quit(e.getPlayer());
	}

	@EventHandler
	public void onKick(PlayerKickEvent e) {
		quit(e.getPlayer());
	}

	@EventHandler
	public void onWorldChange(final PlayerChangedWorldEvent e) {
		// remove cached pipes if the player changes world, so the pipe will be spawned
		// if he switches back
		if (ductsForPlayers.containsKey(e.getPlayer())) {
			List<Pipe> rmList = new ArrayList<>();
			for (int i = 0; i < ductsForPlayers.get(e.getPlayer()).size(); i++) {
				Pipe pipe = ductsForPlayers.get(e.getPlayer()).get(i);
				if (pipe.getBlockLoc().getWorld().equals(e.getFrom())) {
					rmList.add(pipe);
				}
			}
			ductsForPlayers.get(e.getPlayer()).removeAll(rmList);
		}

		// remove cached items if the player changes world, so the item will be spawned
		// if he switches back
		if (itemsForPlayers.containsKey(e.getPlayer())) {
			List<PipeItem> rmList = new ArrayList<>();
			for (int i = 0; i < itemsForPlayers.get(e.getPlayer()).size(); i++) {
				PipeItem pipeItem = itemsForPlayers.get(e.getPlayer()).get(i);
				if (pipeItem.getBlockLoc().getWorld().equals(e.getFrom())) {
					rmList.add(pipeItem);
				}
			}
			itemsForPlayers.get(e.getPlayer()).removeAll(rmList);
		}
	}

	@EventHandler
	public void onJoin(final PlayerJoinEvent e) {
		TransportPipes.instance.pipeThread.runTask(new Runnable() {

			@Override
			public void run() {
				TransportPipes.instance.armorStandProtocol.reloadPipeRenderSystem(e.getPlayer());
			}
		}, PipeThread.WANTED_TPS);
	}
	
	@EventHandler
	public void onTeleport(final PlayerTeleportEvent e) {
		TransportPipes.instance.pipeThread.runTask(new Runnable() {

			@Override
			public void run() {
				TransportPipes.instance.armorStandProtocol.reloadPipeRenderSystem(e.getPlayer());
			}
		}, PipeThread.WANTED_TPS);
	}

	private void quit(final Player p) {
		TransportPipes.instance.pipeThread.runTask(new Runnable() {

			@Override
			public void run() {
				if (PipePacketManager.this.ductsForPlayers.containsKey(p)) {
					PipePacketManager.this.ductsForPlayers.remove(p);
				}
				if (PipePacketManager.this.itemsForPlayers.containsKey(p)) {
					PipePacketManager.this.itemsForPlayers.remove(p);
				}
			}
		}, 0);
	}

}
