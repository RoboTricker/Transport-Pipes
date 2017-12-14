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
import de.robotricker.transportpipes.pipes.DuctType;
import de.robotricker.transportpipes.pipes.WrappedDirection;
import de.robotricker.transportpipes.pipes.types.Pipe;
import de.robotricker.transportpipes.pipeutils.LocationUtils;
import de.robotricker.transportpipes.pipeutils.hitbox.OcclusionCullingUtils;
import de.robotricker.transportpipes.rendersystem.RenderSystem;
import de.robotricker.transportpipes.settings.SettingsUtils;
import io.sentry.Sentry;

public class DuctPacketManager implements Listener {

	private Map<Player, List<Duct>> ductsForPlayers;
	private Map<Player, List<PipeItem>> pipeItemsForPlayers;

	public DuctPacketManager() {
		ductsForPlayers = Collections.synchronizedMap(new HashMap<Player, List<Duct>>());
		pipeItemsForPlayers = Collections.synchronizedMap(new HashMap<Player, List<PipeItem>>());
	}

	public void createDuct(Duct duct, Collection<WrappedDirection> allConnections) {
		// notify duct that some connections might have changed. Knowing this the iron
		// pipe can change its output direction for example.
		duct.notifyConnectionsChange();

		for (RenderSystem pm : duct.getDuctType().getRenderSystems()) {
			pm.createDuctASD(duct, allConnections);
		}
		// client update is done in the next tick
	}

	public void updateDuct(Duct duct) {
		// notify duct that some connections might have changed. Knowing this the iron
		// pipe can change its output direction for example.
		duct.notifyConnectionsChange();

		for (RenderSystem pm : duct.getDuctType().getRenderSystems()) {
			pm.updateDuctASD(duct);
		}
		// client update is done inside the renderSystem method call (that's because
		// here
		// you don't know which ArmorStands to remove and which ones to add)
	}

	public void destroyDuct(Duct duct) {
		for (Player p : ductsForPlayers.keySet()) {
			despawnDuct(p, duct);
		}
		for (RenderSystem pm : duct.getDuctType().getRenderSystems()) {
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
		for (Player p : pipeItemsForPlayers.keySet()) {
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
			List<ArmorStandData> ASD = TransportPipes.instance.armorStandProtocol.getPlayerRenderSystem(p, duct.getDuctType()).getASDForDuct(duct);
			if (ASD != null && !ASD.isEmpty()) {
				list.add(duct);
				TransportPipes.instance.armorStandProtocol.sendArmorStandDatas(p, duct.getBlockLoc(), ASD);
			}
		}
	}

	public void spawnItem(final Player p, final PipeItem item) {
		List<PipeItem> list;
		if (!pipeItemsForPlayers.containsKey(p)) {
			pipeItemsForPlayers.put(p, new ArrayList<PipeItem>());
		}
		list = pipeItemsForPlayers.get(p);
		if (!list.contains(item)) {
			list.add(item);
			TransportPipes.instance.armorStandProtocol.sendPipeItem(p, item);
		}
	}

	public void despawnDuct(final Player p, final Duct duct) {
		if (ductsForPlayers.containsKey(p)) {
			List<Duct> list = ductsForPlayers.get(p);
			if (list.contains(duct)) {
				list.remove(duct);
				TransportPipes.instance.armorStandProtocol.removeArmorStandDatas(p, TransportPipes.instance.armorStandProtocol.getPlayerRenderSystem(p, duct.getDuctType()).getASDIdsForDuct(duct));
			}
		}
	}

	public void despawnItem(final Player p, final PipeItem item) {
		if (pipeItemsForPlayers.containsKey(p)) {
			List<PipeItem> list = pipeItemsForPlayers.get(p);
			if (list.contains(item)) {
				list.remove(item);
				TransportPipes.instance.armorStandProtocol.removePipeItem(p, item);
			}
		}
	}

	public void tickSync() {

		for (World world : Bukkit.getWorlds()) {
			Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(world);
			if (ductMap != null) {
				synchronized (ductMap) {
					for (Duct duct : ductMap.values()) {
						try {
							List<Player> playerList = LocationUtils.getPlayerList(world);
							for (Player on : playerList) {
								if (!duct.getBlockLoc().getWorld().equals(on.getWorld())) {
									continue;
								}
								if (duct.getBlockLoc().distance(on.getLocation()) <= TransportPipes.instance.settingsUtils.getOrLoadPlayerSettings(on).getRenderDistance()) {
									if (OcclusionCullingUtils.isDuctVisibleForPlayer(on, duct)) {
										// spawn duct if not spawned
										spawnDuct(on, duct);
									} else {
										// destroy duct if spawned
										despawnDuct(on, duct);
									}
								} else {
									// destroy duct if spawned
									despawnDuct(on, duct);
								}

								if (duct.getDuctType() == DuctType.PIPE) {
									Pipe pipe = (Pipe) duct;
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
		// remove cached ducts if the player changes world, so the duct will be spawned
		// if he switches back
		if (ductsForPlayers.containsKey(e.getPlayer())) {
			List<Duct> rmList = new ArrayList<>();
			for (int i = 0; i < ductsForPlayers.get(e.getPlayer()).size(); i++) {
				Duct duct = ductsForPlayers.get(e.getPlayer()).get(i);
				if (duct.getBlockLoc().getWorld().equals(e.getFrom())) {
					rmList.add(duct);
				}
			}
			ductsForPlayers.get(e.getPlayer()).removeAll(rmList);
		}

		// remove cached items if the player changes world, so the item will be spawned
		// if he switches back
		if (pipeItemsForPlayers.containsKey(e.getPlayer())) {
			List<PipeItem> rmList = new ArrayList<>();
			for (int i = 0; i < pipeItemsForPlayers.get(e.getPlayer()).size(); i++) {
				PipeItem pipeItem = pipeItemsForPlayers.get(e.getPlayer()).get(i);
				if (pipeItem.getBlockLoc().getWorld().equals(e.getFrom())) {
					rmList.add(pipeItem);
				}
			}
			pipeItemsForPlayers.get(e.getPlayer()).removeAll(rmList);
		}
	}

	@EventHandler
	public void onJoin(final PlayerJoinEvent e) {
		Bukkit.getScheduler().runTaskLater(TransportPipes.instance, new Runnable() {

			@Override
			public void run() {
				for (DuctType dt : DuctType.values()) {
					if(!dt.isEnabled()) {
						continue;
					}
					TransportPipes.instance.armorStandProtocol.getPlayerRenderSystem(e.getPlayer(), dt).initPlayer(e.getPlayer());
				}

				TransportPipes.instance.pipeThread.runTask(new Runnable() {

					@Override
					public void run() {
						TransportPipes.instance.armorStandProtocol.reloadRenderSystem(e.getPlayer());
					}
				}, PipeThread.WANTED_TPS);

			}
		}, 40L);
	}

	@EventHandler
	public void onTeleport(final PlayerTeleportEvent e) {
		TransportPipes.instance.pipeThread.runTask(new Runnable() {

			@Override
			public void run() {
				TransportPipes.instance.armorStandProtocol.reloadRenderSystem(e.getPlayer());
			}
		}, PipeThread.WANTED_TPS);
	}

	private void quit(final Player p) {
		TransportPipes.instance.pipeThread.runTask(new Runnable() {

			@Override
			public void run() {
				if (DuctPacketManager.this.ductsForPlayers.containsKey(p)) {
					DuctPacketManager.this.ductsForPlayers.remove(p);
				}
				if (DuctPacketManager.this.pipeItemsForPlayers.containsKey(p)) {
					DuctPacketManager.this.pipeItemsForPlayers.remove(p);
				}
			}
		}, 0);
	}

}
