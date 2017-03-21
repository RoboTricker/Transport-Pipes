package de.robotricker.transportpipes.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.PipeThread;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.manager.settings.SettingsManager;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipes.Pipe;

public class PipePacketManager implements Listener {

	//not really(!) thread safe! Handle in main thread!
	private Map<Player, List<Pipe>> pipesForPlayers = Collections.synchronizedMap(new HashMap<Player, List<Pipe>>());
	private Map<Player, List<PipeItem>> itemsForPlayers = Collections.synchronizedMap(new HashMap<Player, List<PipeItem>>());

	/**
	 * only removes or sends the edited ArmorStands in this pipe! Does not edit the Pipe-ArmorStand List
	 */
	public void processPipeEdit(final Pipe pipe, List<ArmorStandData> added, List<ArmorStandData> removed) {
		try {
			for (Player p : pipesForPlayers.keySet()) {
				if (pipesForPlayers.get(p).contains(pipe)) {
					//send ASDs
					for (ArmorStandData asd : added) {
						TransportPipes.armorStandProtocol.sendArmorStandData(p, pipe.getBlockLoc(), asd, new Vector(0, 0, 0));
					}
					//remove ASDs
					int[] ids = new int[removed.size()];
					for (int i = 0; i < removed.size(); i++) {
						ids[i] = removed.get(i).getEntityID();
					}
					TransportPipes.armorStandProtocol.removeArmorStandDatas(p, ids);
				}
			}
		} catch (IllegalStateException e) {
			handleAsyncError(e);
		}
	}

	private void putAndSpawnPipe(final Player p, final Pipe pipe) {
		List<Pipe> list;
		if (!pipesForPlayers.containsKey(p)) {
			pipesForPlayers.put(p, new ArrayList<>());
		}
		list = pipesForPlayers.get(p);
		if (!list.contains(pipe)) {
			list.add(pipe);
			TransportPipes.armorStandProtocol.sendPipe(p, pipe);
		}
	}

	private void putAndSpawnItem(final Player p, final PipeItem item) {
		List<PipeItem> list;
		if (!itemsForPlayers.containsKey(p)) {
			itemsForPlayers.put(p, new ArrayList<>());
		}
		list = itemsForPlayers.get(p);
		if (!list.contains(item)) {
			list.add(item);
			TransportPipes.armorStandProtocol.sendPipeItem(p, item);
		}
	}

	private void removeAndDestroyPipe(final Player p, final Pipe pipe) {
		if (pipesForPlayers.containsKey(p)) {
			List<Pipe> list = pipesForPlayers.get(p);
			if (list.contains(pipe)) {
				list.remove(pipe);
				TransportPipes.armorStandProtocol.removePipe(p, pipe);
			}
		}
	}

	private void removeAndDestroyItem(final Player p, final PipeItem item) {
		if (itemsForPlayers.containsKey(p)) {
			List<PipeItem> list = itemsForPlayers.get(p);
			if (list.contains(item)) {
				list.remove(item);
				TransportPipes.armorStandProtocol.removePipeItem(p, item);
			}
		}
	}

	public void spawnPipeSync(Pipe pipe) {
		try {
			for (Player on : pipe.blockLoc.getWorld().getPlayers()) {
				if (pipe.blockLoc.distance(on.getLocation()) <= SettingsManager.getViewDistance(on)) {
					putAndSpawnPipe(on, pipe);
				}
			}
		} catch (IllegalStateException e) {
			handleAsyncError(e);
		}
	}

	public void spawnPipeItemSync(PipeItem item) {
		try {
			for (Player on : item.getBlockLoc().getWorld().getPlayers()) {
				if (item.getBlockLoc().distance(on.getLocation()) <= SettingsManager.getViewDistance(on)) {
					putAndSpawnItem(on, item);
				}
			}
		} catch (IllegalStateException e) {
			handleAsyncError(e);
		}
	}

	public void destroyPipeSync(Pipe pipe) {
		for (Player p : pipesForPlayers.keySet()) {
			removeAndDestroyPipe(p, pipe);
		}
	}

	public void destroyPipeItemSync(PipeItem item) {
		for (Player p : itemsForPlayers.keySet()) {
			removeAndDestroyItem(p, item);
		}
	}

	public void tickSync() {

		for (World world : Bukkit.getWorlds()) {
			Map<Long, Pipe> pipeMap = TransportPipes.getPipeMap(world);
			if (pipeMap != null) {
				synchronized (pipeMap) {
					for (Pipe pipe : pipeMap.values()) {
						try {
							for (Player on : world.getPlayers()) {
								if (pipe.blockLoc.distance(on.getLocation()) <= SettingsManager.getViewDistance(on)) {
									//spawn pipe if not spawned
									putAndSpawnPipe(on, pipe);
								} else {
									//destroy pipe if spawned
									removeAndDestroyPipe(on, pipe);
								}

								for (int i2 = 0; i2 < pipe.pipeItems.size(); i2++) {
									PipeItem item = (PipeItem) pipe.pipeItems.keySet().toArray()[i2];
									if (item.getBlockLoc().distance(on.getLocation()) <= SettingsManager.getViewDistance(on)) {
										putAndSpawnItem(on, item);
									} else {
										removeAndDestroyItem(on, item);
									}
								}

							}
						} catch (IllegalStateException e) {
							handleAsyncError(e);
						}

					}
				}

			}
		}
	}

	public void updatePipeItem(PipeItem item) {
		try {
			for (Player on : item.getBlockLoc().getWorld().getPlayers()) {
				if (item.getBlockLoc().distance(on.getLocation()) <= SettingsManager.getViewDistance(on)) {
					TransportPipes.armorStandProtocol.updatePipeItem(on, item);
				}
			}
		} catch (IllegalStateException e) {
			handleAsyncError(e);
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
		//remove cached pipes if the player changes world, so the pipe will be spawned if he switches back
		if (pipesForPlayers.containsKey(e.getPlayer())) {
			List<Pipe> rmList = new ArrayList<Pipe>();
			for (int i = 0; i < pipesForPlayers.get(e.getPlayer()).size(); i++) {
				Pipe pipe = pipesForPlayers.get(e.getPlayer()).get(i);
				if (pipe.getBlockLoc().getWorld().equals(e.getFrom())) {
					rmList.add(pipe);
				}
			}
			pipesForPlayers.get(e.getPlayer()).removeAll(rmList);
		}

		//remove cached items if the player changes world, so the item will be spawned if he switches back
		if (itemsForPlayers.containsKey(e.getPlayer())) {
			List<PipeItem> rmList = new ArrayList<PipeItem>();
			for (int i = 0; i < itemsForPlayers.get(e.getPlayer()).size(); i++) {
				PipeItem pipeItem = itemsForPlayers.get(e.getPlayer()).get(i);
				if (pipeItem.getBlockLoc().getWorld().equals(e.getFrom())) {
					rmList.add(pipeItem);
				}
			}
			itemsForPlayers.get(e.getPlayer()).removeAll(rmList);
		}
	}

	private void quit(final Player p) {
		PipeThread.runTask(new Runnable() {

			@Override
			public void run() {
				if (PipePacketManager.this.pipesForPlayers.containsKey(p)) {
					PipePacketManager.this.pipesForPlayers.remove(p);
				}
				if (PipePacketManager.this.itemsForPlayers.containsKey(p)) {
					PipePacketManager.this.itemsForPlayers.remove(p);
				}
			}
		}, 0);
	}

	private void handleAsyncError(IllegalStateException e) {
		System.err.println("ASYNC ERROR:");
		e.printStackTrace();
	}

}
