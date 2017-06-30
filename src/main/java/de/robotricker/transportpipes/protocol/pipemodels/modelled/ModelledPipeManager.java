package de.robotricker.transportpipes.protocol.pipemodels.modelled;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Color;

import de.robotricker.transportpipes.pipes.ColoredPipe;
import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipeutils.PipeColor;
import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.pipeutils.PipeUtils;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.protocol.ArmorStandProtocol;
import de.robotricker.transportpipes.protocol.pipemodels.PipeManager;

public class ModelledPipeManager extends PipeManager {

	private Map<Pipe, ArmorStandData> pipeMidAsd = new HashMap<Pipe, ArmorStandData>();
	private Map<Pipe, Map<PipeDirection, ArmorStandData>> pipeConnsAsd = new HashMap<Pipe, Map<PipeDirection, ArmorStandData>>();

	private Map<PipeType, ModelledPipeModel> pipeModels = new HashMap<PipeType, ModelledPipeModel>();

	public ModelledPipeManager(ArmorStandProtocol protocol) {
		super(protocol);
		pipeModels.put(PipeType.COLORED, new ModelledPipeCOLOREDModel());
		pipeModels.put(PipeType.ICE, new ModelledPipeICEModel());
		pipeModels.put(PipeType.GOLDEN, new ModelledPipeGOLDENModel());
		pipeModels.put(PipeType.IRON, new ModelledPipeIRONModel());
	}

	@Override
	public void sendPipe(Pipe pipe) {
		if (pipeMidAsd.containsKey(pipe)) {
			return;
		}

		PipeColor pc = PipeColor.WHITE;
		if (pipe.getPipeType() == PipeType.COLORED) {
			pc = ((ColoredPipe) pipe).getPipeColor();
		}
		List<PipeDirection> conns = PipeUtils.getPipeConnections(pipe.getBlockLoc(), pipe.getPipeType(), pc);

		ModelledPipeModel model = pipeModels.get(pipe.getPipeType());
		pipeMidAsd.put(pipe, model.createMidASD(pipe.getPipeType(), pc));
		Map<PipeDirection, ArmorStandData> connsMap = pipeConnsAsd.put(pipe, new HashMap<PipeDirection, ArmorStandData>());
		for (PipeDirection conn : conns) {
			connsMap.put(conn, model.createConnASD(pipe.getPipeType(), conn, Color.RED, pc, false));
		}

		//SEND TO CLIENTS

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

		PipeColor pc = PipeColor.WHITE;
		if (pipe.getPipeType() == PipeType.COLORED) {
			pc = ((ColoredPipe) pipe).getPipeColor();
		}
		List<PipeDirection> newConns = PipeUtils.getPipeConnections(pipe.getBlockLoc(), pipe.getPipeType(), pc);
		for (PipeDirection pd : PipeDirection.values()) {
			if (connsMap.containsKey(pd) && newConns.contains(pd)) {
				//direction was active before and after update
				ArmorStandData newASD = model.createConnASD(pipe.getPipeType(), pd, Color.RED, pc, false);
				if (!connsMap.get(pd).isSimilar(newASD)) {
					//ASD changed after update in this direction
					removedASD.add(connsMap.get(pd));
					addedASD.add(newASD);
					connsMap.put(pd, newASD);
				}
			} else if (!connsMap.containsKey(pd) && newConns.contains(pd)) {
				//direction wasn't active before update but direction IS active after update
				ArmorStandData newASD = model.createConnASD(pipe.getPipeType(), pd, Color.RED, pc, false);
				addedASD.add(newASD);
				connsMap.put(pd, newASD);
			} else if (connsMap.containsKey(pd) && !newConns.contains(pd)) {
				//direction was active before update but isn't active after update
				removedASD.add(connsMap.get(pd));
				connsMap.remove(pd);
			}
		}

		//SEND TO CLIENTS

	}

	@Override
	public void destroyPipe(Pipe pipe) {
		if (!pipeMidAsd.containsKey(pipe) || !pipeConnsAsd.containsKey(pipe)) {
			return;
		}

		ArmorStandData midASD = pipeMidAsd.remove(pipe);
		Collection<ArmorStandData> connsASD = pipeConnsAsd.remove(pipe).values();

		//SEND TO CLIENTS

	}

}
