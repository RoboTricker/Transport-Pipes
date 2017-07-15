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

import de.robotricker.transportpipes.PipeThread;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipes.BlockLoc;
import de.robotricker.transportpipes.pipes.PipeDirection;
import de.robotricker.transportpipes.pipes.types.Pipe;
import de.robotricker.transportpipes.rendersystem.PipeRenderSystem;
import de.robotricker.transportpipes.settings.SettingsUtils;

public class PipePacketManager implements Listener {

	//not really(!) thread safe! Handle in main thread!
	//public Map<World, List<Integer>> allWorldEntityIds = Collections.synchronizedMap(new HashMap<World, List<Integer>>());
	private Map<Player, List<Pipe>> pipesForPlayers = Collections.synchronizedMap(new HashMap<Player, List<Pipe>>());
	private Map<Player, List<PipeItem>> itemsForPlayers = Collections.synchronizedMap(new HashMap<Player, List<PipeItem>>());

	public void createPipe(Pipe pipe, List<PipeDirection> allConnections) {
		for (PipeRenderSystem pm : TransportPipes.instance.getPipeRenderSystems()) {
			pm.createPipeASD(pipe, allConnections);
		}
		//client update is done in the next tick
	}

	public void updatePipe(Pipe pipe) {
		for (PipeRenderSystem pm : TransportPipes.instance.getPipeRenderSystems()) {
			pm.updatePipeASD(pipe);
		}
		//client update is done inside the PipeManager method call (that's because here you don't know which ArmorStands to remove and which ones to add)
	}

	public void destroyPipe(Pipe pipe) {
		for (Player p : pipesForPlayers.keySet()) {
			despawnPipe(p, pipe);
		}
		for (PipeRenderSystem pm : TransportPipes.instance.getPipeRenderSystems()) {
			pm.destroyPipeASD(pipe);
		}
	}

	public void createPipeItem(PipeItem pipeItem) {
		try {
			for (Player on : pipeItem.getBlockLoc().getWorld().getPlayers()) {
				if (pipeItem.getBlockLoc().distance(on.getLocation()) <= SettingsUtils.loadPlayerSettings(on).getRenderDistance()) {
					spawnItem(on, pipeItem);
				}
			}
		} catch (IllegalStateException e) {
			handleAsyncError(e);
		}
	}

	public void updatePipeItem(PipeItem pipeItem) {
		try {
			for (Player on : pipeItem.getBlockLoc().getWorld().getPlayers()) {
				if (pipeItem.getBlockLoc().distance(on.getLocation()) <= SettingsUtils.loadPlayerSettings(on).getRenderDistance()) {
					TransportPipes.armorStandProtocol.updatePipeItem(on, pipeItem);
				}
			}
		} catch (IllegalStateException e) {
			handleAsyncError(e);
		}
	}

	public void destroyPipeItem(PipeItem pipeItem) {
		for (Player p : itemsForPlayers.keySet()) {
			despawnItem(p, pipeItem);
		}
	}

	public void spawnPipe(final Player p, final Pipe pipe) {
		List<Pipe> list;
		if (!pipesForPlayers.containsKey(p)) {
			pipesForPlayers.put(p, new ArrayList<Pipe>());
		}
		list = pipesForPlayers.get(p);
		if (!list.contains(pipe)) {
			List<ArmorStandData> ASD = TransportPipes.armorStandProtocol.getPlayerPipeRenderSystem(p).getASDForPipe(pipe);
			if (ASD != null && !ASD.isEmpty()) {
				list.add(pipe);
				TransportPipes.armorStandProtocol.sendArmorStandDatas(p, pipe.getBlockLoc(), ASD);
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
			TransportPipes.armorStandProtocol.sendPipeItem(p, item);
		}
	}

	public void despawnPipe(final Player p, final Pipe pipe) {
		if (pipesForPlayers.containsKey(p)) {
			List<Pipe> list = pipesForPlayers.get(p);
			if (list.contains(pipe)) {
				list.remove(pipe);
				TransportPipes.armorStandProtocol.removeArmorStandDatas(p, TransportPipes.armorStandProtocol.getPlayerPipeRenderSystem(p).getASDIdsForPipe(pipe));
			}
		}
	}

	public void despawnItem(final Player p, final PipeItem item) {
		if (itemsForPlayers.containsKey(p)) {
			List<PipeItem> list = itemsForPlayers.get(p);
			if (list.contains(item)) {
				list.remove(item);
				TransportPipes.armorStandProtocol.removePipeItem(p, item);
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
							for (Player on : world.getPlayers()) {
								if (pipe.blockLoc.distance(on.getLocation()) <= SettingsUtils.loadPlayerSettings(on).getRenderDistance()) {
									//spawn pipe if not spawned
									spawnPipe(on, pipe);
								} else {
									//destroy pipe if spawned
									despawnPipe(on, pipe);
								}

								for (int i2 = 0; i2 < pipe.pipeItems.size(); i2++) {
									PipeItem item = (PipeItem) pipe.pipeItems.keySet().toArray()[i2];
									if (item.getBlockLoc().distance(on.getLocation()) <= SettingsUtils.loadPlayerSettings(on).getRenderDistance()) {
										spawnItem(on, item);
									} else {
										despawnItem(on, item);
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
