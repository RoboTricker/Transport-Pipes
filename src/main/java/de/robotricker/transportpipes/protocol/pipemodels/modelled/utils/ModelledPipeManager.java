package de.robotricker.transportpipes.protocol.pipemodels.modelled.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.TransportPipes.BlockLoc;
import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipeutils.PipeColor;
import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.pipeutils.PipeItemUtils;
import de.robotricker.transportpipes.pipeutils.hitbox.AxisAlignedBB;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.protocol.ArmorStandProtocol;
import de.robotricker.transportpipes.protocol.pipemodels.PipeManager;
import de.robotricker.transportpipes.protocol.pipemodels.modelled.ModelledPipeCOLOREDModel;
import de.robotricker.transportpipes.protocol.pipemodels.modelled.ModelledPipeGOLDENModel;
import de.robotricker.transportpipes.protocol.pipemodels.modelled.ModelledPipeICEModel;
import de.robotricker.transportpipes.protocol.pipemodels.modelled.ModelledPipeIRONModel;
import de.robotricker.transportpipes.protocol.pipemodels.modelled.ModelledPipeModel;

public class ModelledPipeManager extends PipeManager implements Listener {

	public static final ItemStack ITEM_PIPE_WHITE = PipeItemUtils.createToolItemStack(25, PipeColor.WHITE.getColorCode() + TransportPipes.instance.PIPE_NAME);

	private Map<Pipe, ArmorStandData> pipeMidAsd = new HashMap<Pipe, ArmorStandData>();
	private Map<Pipe, Map<PipeDirection, ArmorStandData>> pipeConnsAsd = new HashMap<Pipe, Map<PipeDirection, ArmorStandData>>();
	private AxisAlignedBB pipeMidAABB;
	private Map<PipeDirection, AxisAlignedBB> pipeConnsAABBs = new HashMap<PipeDirection, AxisAlignedBB>();

	private Map<PipeType, ModelledPipeModel> pipeModels = new HashMap<PipeType, ModelledPipeModel>();

	private List<Player> loadedResourcePackPlayers = new ArrayList<Player>();

	public ModelledPipeManager(ArmorStandProtocol protocol) {
		super(protocol);
		pipeModels.put(PipeType.COLORED, new ModelledPipeCOLOREDModel());
		pipeModels.put(PipeType.ICE, new ModelledPipeICEModel());
		pipeModels.put(PipeType.GOLDEN, new ModelledPipeGOLDENModel());
		pipeModels.put(PipeType.IRON, new ModelledPipeIRONModel());

		pipeMidAABB = new AxisAlignedBB(4d / 16d, 4d / 16d, 4d / 16d, 12d / 16d, 12d / 16d, 12d / 16d);
		pipeConnsAABBs.put(PipeDirection.NORTH, new AxisAlignedBB(4d / 16d, 4d / 16d, 0d / 16d, 12d / 16d, 12d / 16d, 4d / 16d));
		pipeConnsAABBs.put(PipeDirection.EAST, new AxisAlignedBB(12d / 16d, 4d / 16d, 4d / 16d, 16d / 16d, 12d / 16d, 12d / 16d));
		pipeConnsAABBs.put(PipeDirection.SOUTH, new AxisAlignedBB(4d / 16d, 4d / 16d, 12d / 16d, 12d / 16d, 12d / 16d, 16d / 16d));
		pipeConnsAABBs.put(PipeDirection.WEST, new AxisAlignedBB(0d / 16d, 4d / 16d, 4d / 16d, 4d / 16d, 12d / 16d, 12d / 16d));
		pipeConnsAABBs.put(PipeDirection.UP, new AxisAlignedBB(4d / 16d, 12d / 16d, 4d / 16d, 12d / 16d, 16d / 16d, 12d / 16d));
		pipeConnsAABBs.put(PipeDirection.DOWN, new AxisAlignedBB(4d / 16d, 0d / 16d, 4d / 16d, 12d / 16d, 4d / 16d, 12d / 16d));
	}

	@Override
	public void createPipeASD(Pipe pipe, List<PipeDirection> allConnections) {
		if (pipeMidAsd.containsKey(pipe)) {
			return;
		}

		ModelledPipeModel model = pipeModels.get(pipe.getPipeType());
		pipeMidAsd.put(pipe, model.createMidASD(ModelledPipeMidModelData.createModelData(pipe)));
		Map<PipeDirection, ArmorStandData> connsMap = new HashMap<PipeDirection, ArmorStandData>();
		pipeConnsAsd.put(pipe, connsMap);
		for (PipeDirection conn : allConnections) {
			connsMap.put(conn, model.createConnASD(ModelledPipeConnModelData.createModelData(pipe, conn)));
		}

	}

	@Override
	public void updatePipeASD(Pipe pipe) {
		if (!pipeMidAsd.containsKey(pipe) || !pipeConnsAsd.containsKey(pipe) || pipeConnsAsd.get(pipe) == null) {
			return;
		}

		List<ArmorStandData> removedASD = new ArrayList<ArmorStandData>();
		List<ArmorStandData> addedASD = new ArrayList<ArmorStandData>();

		Map<PipeDirection, ArmorStandData> connsMap = pipeConnsAsd.get(pipe);
		ModelledPipeModel model = pipeModels.get(pipe.getPipeType());

		List<PipeDirection> newConns = pipe.getAllConnections();
		for (PipeDirection pd : PipeDirection.values()) {
			if (connsMap.containsKey(pd) && newConns.contains(pd)) {
				//direction was active before and after update
				ArmorStandData newASD = model.createConnASD(ModelledPipeConnModelData.createModelData(pipe, pd));
				if (!connsMap.get(pd).isSimilar(newASD)) {
					//ASD changed after update in this direction
					removedASD.add(connsMap.get(pd));
					addedASD.add(newASD);
					connsMap.put(pd, newASD);
				}
			} else if (!connsMap.containsKey(pd) && newConns.contains(pd)) {
				//direction wasn't active before update but direction IS active after update
				ArmorStandData newASD = model.createConnASD(ModelledPipeConnModelData.createModelData(pipe, pd));
				addedASD.add(newASD);
				connsMap.put(pd, newASD);
			} else if (connsMap.containsKey(pd) && !newConns.contains(pd)) {
				//direction was active before update but isn't active after update
				removedASD.add(connsMap.get(pd));
				connsMap.remove(pd);
			}
		}

		//SEND TO CLIENTS
		List<Player> players = protocol.getPlayersWithPipeManager(this);
		int[] removedIds = TransportPipes.instance.convertArmorStandListToEntityIdArray(removedASD);
		for (Player p : players) {
			protocol.removeArmorStandDatas(p, removedIds);
			protocol.sendArmorStandDatas(p, pipe.getBlockLoc(), addedASD);
		}

	}

	@Override
	public void destroyPipeASD(Pipe pipe) {
		if (!pipeMidAsd.containsKey(pipe) || !pipeConnsAsd.containsKey(pipe) || pipeConnsAsd.get(pipe) == null) {
			return;
		}
		pipeMidAsd.remove(pipe);
		pipeConnsAsd.remove(pipe);
	}

	@Override
	public List<ArmorStandData> getASDForPipe(Pipe pipe) {
		List<ArmorStandData> ASD = new ArrayList<ArmorStandData>();
		if (pipeMidAsd.containsKey(pipe)) {
			ASD.add(pipeMidAsd.get(pipe));
		}
		if (pipeConnsAsd.containsKey(pipe)) {
			ASD.addAll(pipeConnsAsd.get(pipe).values());
		}
		return ASD;
	}

	@Override
	public PipeDirection getClickedPipeFace(Player player, Pipe pipe) {

		if (pipe == null) {
			return null;
		}

		Vector ray = player.getEyeLocation().getDirection();
		Vector origin = player.getEyeLocation().toVector();

		List<PipeDirection> pipeConns = pipe.getAllConnections();
		PipeDirection clickedMidFace = pipeMidAABB.rayIntersection(ray, origin, pipe.getBlockLoc());
		if (clickedMidFace != null && !pipeConns.contains(clickedMidFace)) {
			return clickedMidFace;
		} else {
			double nearestDistanceSquared = Double.MAX_VALUE;
			PipeDirection currentClickedConnFace = null;
			for (PipeDirection pd : pipeConns) {
				AxisAlignedBB connAABB = pipeConnsAABBs.get(pd);
				double newDistanceSquared = connAABB.getAABBMiddle(pipe.getBlockLoc()).distanceSquared(origin);
				if (newDistanceSquared < nearestDistanceSquared) {
					PipeDirection clickedConnFace = connAABB.rayIntersection(ray, origin, pipe.getBlockLoc());
					if (clickedConnFace != null) {
						nearestDistanceSquared = newDistanceSquared;
						currentClickedConnFace = clickedConnFace;
					}
				}
			}
			return currentClickedConnFace;
		}

	}

	@Override
	public String getPipeRenderSystemName() {
		return "Modelled";
	}

	@Override
	public ItemStack getRepresentationItem() {
		return ITEM_PIPE_WHITE;
	}

	@Override
	public void initPlayer(Player p) {
		if (!loadedResourcePackPlayers.contains(p)) {
			p.closeInventory();
			p.setResourcePack("http://frontfight.net/TransportPipes-ResourcePack2.zip");
		}
	}

	@EventHandler
	public void onResourcePackStatus(PlayerResourcePackStatusEvent e) {
		if (e.getStatus() == Status.DECLINED || e.getStatus() == Status.FAILED_DOWNLOAD) {

			PipeManager beforePm = TransportPipes.armorStandProtocol.getPlayerPipeManager(e.getPlayer());
			if (beforePm.equals(TransportPipes.modelledPipeManager)) {
				Map<BlockLoc, Pipe> pipeMap = TransportPipes.getPipeMap(e.getPlayer().getWorld());
				if (pipeMap != null) {
					synchronized (pipeMap) {
						for (Pipe pipe : pipeMap.values()) {
							TransportPipes.pipePacketManager.despawnPipe(e.getPlayer(), pipe);
						}
					}
				}

				TransportPipes.armorStandProtocol.setPlayerPipeManager(e.getPlayer(), TransportPipes.vanillaPipeManager);
				TransportPipes.vanillaPipeManager.initPlayer(e.getPlayer());

				if (pipeMap != null) {
					synchronized (pipeMap) {
						for (Pipe pipe : pipeMap.values()) {
							TransportPipes.pipePacketManager.spawnPipe(e.getPlayer(), pipe);
						}
					}
				}
			}
			e.getPlayer().sendMessage("§cResourcepack Download failed: Switched to the Vanilla Model System");
		} else {
			if (!loadedResourcePackPlayers.contains(e.getPlayer())) {
				loadedResourcePackPlayers.add(e.getPlayer());
			}
		}
	}

}
