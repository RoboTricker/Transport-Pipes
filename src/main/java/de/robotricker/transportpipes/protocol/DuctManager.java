package de.robotricker.transportpipes.protocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
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
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctType;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.rendersystem.RenderSystem;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.hitbox.OcclusionCullingUtils;
import de.robotricker.transportpipes.utils.staticutils.LocationUtils;
import de.robotricker.transportpipes.utils.staticutils.SettingsUtils;
import io.sentry.Sentry;

public class DuctManager implements Listener {

	private DuctProtocol protocol;
	private Map<Player, List<Duct>> ductsForPlayers;
	private Map<Player, List<PipeItem>> pipeItemsForPlayers;

	public DuctManager() {
		this.protocol = new DuctProtocol();
		this.ductsForPlayers = Collections.synchronizedMap(new HashMap<Player, List<Duct>>());
		this.pipeItemsForPlayers = Collections.synchronizedMap(new HashMap<Player, List<PipeItem>>());
	}

	public DuctProtocol getProtocol() {
		return protocol;
	}

	public RenderSystem getPlayerRenderSystem(Player p, DuctType ductType) {
		return TransportPipes.instance.settingsUtils.getOrLoadPlayerSettings(p).getRenderSystem(ductType);
	}

	public List<Player> getAllPlayersWithRenderSystem(RenderSystem renderSystem) {
		List<Player> players = new ArrayList<>();
		players.addAll(Bukkit.getOnlinePlayers());

		Iterator<Player> it = players.iterator();
		while (it.hasNext()) {
			Player p = it.next();
			// remove all players which don't use the given renderSystem
			if (!getPlayerRenderSystem(p, renderSystem.getDuctType()).equals(renderSystem)) {
				it.remove();
			}
		}
		return players;
	}

	public boolean isPlayerShowItems(Player p) {
		return TransportPipes.instance.settingsUtils.getOrLoadPlayerSettings(p).isShowItems();
	}

	/**
	 * creates the duct inside the renderSystems. This creates all needed ASD and
	 * notifies a connection change to this duct. This method does not send the ASD
	 * to the client, this is done in the tick() method
	 */
	public void createDuct(Duct duct, Collection<WrappedDirection> allConnections) {
		// notify duct that some connections might have changed. Knowing this the iron
		// pipe can change its output direction for example.
		duct.notifyConnectionsChange();

		for (RenderSystem pm : duct.getDuctType().getRenderSystems()) {
			pm.createDuctASD(duct, allConnections);
		}
	}

	/**
	 * updates the duct inside the renderSystems. This updated all needed ASD and
	 * notifies a connection change to this duct. The renderSystem will send the
	 * removed / added ASD to all clients with this renderSystem.
	 */
	public void updateDuct(Duct duct) {
		// notify duct that some connections might have changed. Knowing this the iron
		// pipe can change its output direction for example.
		duct.notifyConnectionsChange();

		for (RenderSystem pm : duct.getDuctType().getRenderSystems()) {
			pm.updateDuctASD(duct);
		}
	}

	/**
	 * destroys the cached ASD data inside the renderSystems. It also despawns the
	 * duct for all players.
	 */
	public void destroyDuct(Duct duct) {
		for (Player p : ductsForPlayers.keySet()) {
			despawnDuct(p, duct);
		}
		for (RenderSystem pm : duct.getDuctType().getRenderSystems()) {
			pm.destroyDuctASD(duct);
		}
	}

	/**
	 * spawns the pipeItem for all players (if they are near enough)
	 */
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

	/**
	 * updates the pipeItem (location) for all players (if they are near enough)
	 */
	public void updatePipeItem(PipeItem pipeItem) {
		try {
			List<Player> playerList = LocationUtils.getPlayerList(pipeItem.getBlockLoc().getWorld());
			for (Player on : playerList) {
				if (on.getWorld().equals(pipeItem.getBlockLoc().getWorld())) {
					if (pipeItem.getBlockLoc().distance(on.getLocation()) <= TransportPipes.instance.settingsUtils.getOrLoadPlayerSettings(on).getRenderDistance()) {
						getProtocol().updatePipeItem(on, pipeItem);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Sentry.capture(e);
		}
	}

	/**
	 * permanently destroys the pipeItem for all players
	 */
	public void destroyPipeItem(PipeItem pipeItem) {
		for (Player p : pipeItemsForPlayers.keySet()) {
			despawnItem(p, pipeItem);
		}
	}

	/**
	 * sends the ASD (from the player's renderSystem) for the given duct to the
	 * player.
	 */
	private void spawnDuct(final Player p, final Duct duct) {
		List<Duct> list;
		if (!ductsForPlayers.containsKey(p)) {
			ductsForPlayers.put(p, new ArrayList<Duct>());
		}
		list = ductsForPlayers.get(p);
		if (!list.contains(duct)) {
			List<ArmorStandData> ASD = getPlayerRenderSystem(p, duct.getDuctType()).getASDForDuct(duct);
			if (ASD != null && !ASD.isEmpty()) {
				list.add(duct);
				getProtocol().sendArmorStandDatas(p, duct.getBlockLoc(), ASD);
			}
		}
	}

	/**
	 * sends the ASD for the given pipeItem to the player
	 */
	private void spawnItem(final Player p, final PipeItem item) {
		List<PipeItem> list;
		if (!pipeItemsForPlayers.containsKey(p)) {
			pipeItemsForPlayers.put(p, new ArrayList<PipeItem>());
		}
		list = pipeItemsForPlayers.get(p);
		if (!list.contains(item)) {
			list.add(item);
			getProtocol().sendPipeItem(p, item);
		}
	}

	/**
	 * despawns the ASD for the given duct on the client
	 */
	private void despawnDuct(final Player p, final Duct duct) {
		if (ductsForPlayers.containsKey(p)) {
			List<Duct> list = ductsForPlayers.get(p);
			if (list.contains(duct)) {
				list.remove(duct);
				getProtocol().removeArmorStandDatas(p, getPlayerRenderSystem(p, duct.getDuctType()).getASDIdsForDuct(duct));
			}
		}
	}

	/**
	 * despawns the ASD for the given pipeItem on the client
	 */
	private void despawnItem(final Player p, final PipeItem item) {
		if (pipeItemsForPlayers.containsKey(p)) {
			List<PipeItem> list = pipeItemsForPlayers.get(p);
			if (list.contains(item)) {
				list.remove(item);
				getProtocol().removePipeItem(p, item);
			}
		}
	}

	/**
	 * called every tick. This method makes sure that all ducts and items far away
	 * from the player or occluded from the player are despawned and the ducts and
	 * items near enough are spawned.
	 */
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

	public void changePlayerRenderSystem(Player p, int newRenderSystemId) {
		// despawn all old ducts
		Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(p.getWorld());
		if (ductMap != null) {
			synchronized (ductMap) {
				for (Duct duct : ductMap.values()) {
					TransportPipes.instance.ductManager.despawnDuct(p, duct);
				}
			}
		}

		// change render system
		TransportPipes.instance.settingsUtils.getOrLoadPlayerSettings(p).setRenderSystem(newRenderSystemId);
		for (DuctType dt : DuctType.values()) {
			if (!dt.isEnabled()) {
				continue;
			}
			TransportPipes.instance.ductManager.getPlayerRenderSystem(p, dt).initPlayer(p);
		}

		// spawn all new ducts
		if (ductMap != null) {
			synchronized (ductMap) {
				for (Duct duct : ductMap.values()) {
					TransportPipes.instance.ductManager.spawnDuct(p, duct);
				}
			}
		}
	}

	public void changeShowItems(Player p, boolean showItems) {
		TransportPipes.instance.settingsUtils.getOrLoadPlayerSettings(p).setShowItems(showItems);
	}

	public void reloadRenderSystem(Player p) {
		Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(p.getWorld());
		if (ductMap != null) {
			synchronized (ductMap) {
				// despawn all pipes
				for (Duct duct : ductMap.values()) {
					TransportPipes.instance.ductManager.despawnDuct(p, duct);
				}
				// spawn all pipes
				for (Duct duct : ductMap.values()) {
					TransportPipes.instance.ductManager.spawnDuct(p, duct);
				}
			}
		}
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
					if (!dt.isEnabled()) {
						continue;
					}
					getPlayerRenderSystem(e.getPlayer(), dt).initPlayer(e.getPlayer());
				}

				TransportPipes.instance.pipeThread.runTask(new Runnable() {

					@Override
					public void run() {
						reloadRenderSystem(e.getPlayer());
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
				reloadRenderSystem(e.getPlayer());
			}
		}, PipeThread.WANTED_TPS);
	}

	private void quit(final Player p) {
		TransportPipes.instance.pipeThread.runTask(new Runnable() {

			@Override
			public void run() {
				if (DuctManager.this.ductsForPlayers.containsKey(p)) {
					DuctManager.this.ductsForPlayers.remove(p);
				}
				if (DuctManager.this.pipeItemsForPlayers.containsKey(p)) {
					DuctManager.this.pipeItemsForPlayers.remove(p);
				}
			}
		}, 0);
	}

}
