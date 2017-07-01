package de.robotricker.transportpipes.protocol.pipemodels.modelled.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.pipeutils.hitbox.AxisAlignedBB;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.protocol.ArmorStandProtocol;
import de.robotricker.transportpipes.protocol.pipemodels.PipeManager;
import de.robotricker.transportpipes.protocol.pipemodels.modelled.ModelledPipeCOLOREDModel;
import de.robotricker.transportpipes.protocol.pipemodels.modelled.ModelledPipeGOLDENModel;
import de.robotricker.transportpipes.protocol.pipemodels.modelled.ModelledPipeICEModel;
import de.robotricker.transportpipes.protocol.pipemodels.modelled.ModelledPipeIRONModel;
import de.robotricker.transportpipes.protocol.pipemodels.modelled.ModelledPipeModel;

public class ModelledPipeManager extends PipeManager {

	private Map<Pipe, ArmorStandData> pipeMidAsd = new HashMap<Pipe, ArmorStandData>();
	private Map<Pipe, Map<PipeDirection, ArmorStandData>> pipeConnsAsd = new HashMap<Pipe, Map<PipeDirection, ArmorStandData>>();
	private AxisAlignedBB pipeMidAABB;
	private Map<PipeDirection, AxisAlignedBB> pipeConnsAABBs = new HashMap<PipeDirection, AxisAlignedBB>();

	private Map<PipeType, ModelledPipeModel> pipeModels = new HashMap<PipeType, ModelledPipeModel>();

	public ModelledPipeManager(ArmorStandProtocol protocol) {
		super(protocol);
		pipeModels.put(PipeType.COLORED, new ModelledPipeCOLOREDModel());
		pipeModels.put(PipeType.ICE, new ModelledPipeICEModel());
		pipeModels.put(PipeType.GOLDEN, new ModelledPipeGOLDENModel());
		pipeModels.put(PipeType.IRON, new ModelledPipeIRONModel());

		pipeMidAABB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
		for (PipeDirection pd : PipeDirection.values()) {
			pipeConnsAABBs.put(pd, new AxisAlignedBB(0, 0, 0, 0, 0, 0));
		}

	}

	@Override
	public void sendPipe(Pipe pipe) {
		if (pipeMidAsd.containsKey(pipe)) {
			return;
		}

		List<PipeDirection> conns = pipe.getAllConnections();

		ModelledPipeModel model = pipeModels.get(pipe.getPipeType());
		pipeMidAsd.put(pipe, model.createMidASD(ModelledPipeMidModelData.createModelData(pipe)));
		Map<PipeDirection, ArmorStandData> connsMap = new HashMap<PipeDirection, ArmorStandData>();
		pipeConnsAsd.put(pipe, connsMap);
		for (PipeDirection conn : conns) {
			connsMap.put(conn, model.createConnASD(ModelledPipeConnModelData.createModelData(pipe, conn)));
		}

	}

	@Override
	public void updatePipe(Pipe pipe) {
		if (!pipeMidAsd.containsKey(pipe) || !pipeConnsAsd.containsKey(pipe)) {
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
		int[] removedIds = new int[removedASD.size()];
		for (int i = 0; i < removedIds.length; i++) {
			removedIds[i] = removedASD.get(i).getEntityID();
			if (removedIds[i] == -1) {
				System.err.println("ERRRRRROR: ______________________ -1");
			}
		}
		for (Player p : players) {
			protocol.removeArmorStandDatas(p, removedIds);
			protocol.sendArmorStandDatas(p, pipe.getBlockLoc(), addedASD);
		}

	}

	@Override
	public void destroyPipe(Pipe pipe) {
		if (!pipeMidAsd.containsKey(pipe) || !pipeConnsAsd.containsKey(pipe)) {
			return;
		}

		ArmorStandData midASD = pipeMidAsd.remove(pipe);
		Collection<ArmorStandData> connsASD = pipeConnsAsd.remove(pipe).values();

		//SEND TO CLIENTS
		List<Player> players = protocol.getPlayersWithPipeManager(this);
		int[] removedIds = new int[connsASD.size() + 1];
		removedIds[0] = midASD.getEntityID();
		if (removedIds[0] == -1) {
			System.err.println("ERRRRRROR2: ______________________ -1");
		}
		for (int i = 1; i < removedIds.length; i++) {
			removedIds[i] = connsASD.toArray(new ArmorStandData[0])[i - 1].getEntityID();
			if (removedIds[i] == -1) {
				System.err.println("ERRRRRROR3: ______________________ -1");
			}
		}
		for (Player p : players) {
			protocol.removeArmorStandDatas(p, removedIds);
		}

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
		
		if(pipe == null){
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

}
