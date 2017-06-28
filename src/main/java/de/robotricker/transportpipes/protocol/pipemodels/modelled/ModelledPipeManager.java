package de.robotricker.transportpipes.protocol.pipemodels.modelled;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robotricker.transportpipes.pipes.ColoredPipe;
import de.robotricker.transportpipes.pipes.IronPipe;
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

		if (pipe.getPipeType() == PipeType.COLORED) {
			ModelledPipeCOLOREDModel model = ((ModelledPipeCOLOREDModel) pipeModels.get(PipeType.COLORED));
			pipeMidAsd.put(pipe, model.createMIDArmorStandData(pc));

			Map<PipeDirection, ArmorStandData> connsMap = pipeConnsAsd.put(pipe, new HashMap<PipeDirection, ArmorStandData>());
			for (PipeDirection conn : conns)
				connsMap.put(conn, model.createCONNArmorStandData(pc, conn));
		} else if (pipe.getPipeType() == PipeType.GOLDEN) {
			ModelledPipeGOLDENModel model = ((ModelledPipeGOLDENModel) pipeModels.get(PipeType.GOLDEN));
			pipeMidAsd.put(pipe, model.createMIDArmorStandData());

			Map<PipeDirection, ArmorStandData> connsMap = pipeConnsAsd.put(pipe, new HashMap<PipeDirection, ArmorStandData>());
			for (PipeDirection conn : conns)
				connsMap.put(conn, model.createCONNArmorStandData(conn));
		} else if (pipe.getPipeType() == PipeType.IRON) {
			ModelledPipeIRONModel model = ((ModelledPipeIRONModel) pipeModels.get(PipeType.IRON));
			pipeMidAsd.put(pipe, model.createMIDArmorStandData());

			Map<PipeDirection, ArmorStandData> connsMap = pipeConnsAsd.put(pipe, new HashMap<PipeDirection, ArmorStandData>());
			for (PipeDirection conn : conns)
				connsMap.put(conn, model.createCONNArmorStandData(conn, ((IronPipe) pipe).getCurrentOutputDir() == conn));
		} else if (pipe.getPipeType() == PipeType.ICE) {
			ModelledPipeICEModel model = ((ModelledPipeICEModel) pipeModels.get(PipeType.ICE));
			pipeMidAsd.put(pipe, model.createMIDArmorStandData());

			Map<PipeDirection, ArmorStandData> connsMap = pipeConnsAsd.put(pipe, new HashMap<PipeDirection, ArmorStandData>());
			for (PipeDirection conn : conns)
				connsMap.put(conn, model.createCONNArmorStandData(conn));
		}

		if (pipeMidAsd.containsKey(pipe)) {

			//SEND TO CLIENTS

		}

	}

	@Override
	public void updatePipeShape(Pipe pipe) {
		if (!pipeMidAsd.containsKey(pipe)) {
			return;
		}

		PipeColor pc = PipeColor.WHITE;
		if (pipe.getPipeType() == PipeType.COLORED) {
			pc = ((ColoredPipe) pipe).getPipeColor();
		}
		Collection<PipeDirection> newConns = PipeUtils.getPipeConnections(pipe.getBlockLoc(), pipe.getPipeType(), pc);
		Collection<PipeDirection> oldConns = pipeConnsAsd.get(pipe).keySet();

		List<ArmorStandData> removedAsd = new ArrayList<ArmorStandData>();
		List<ArmorStandData> addAsd = new ArrayList<ArmorStandData>();

		for (PipeDirection pd : PipeDirection.values()) {
			if (oldConns.contains(pd) && !newConns.contains(pd)) {
				removedAsd.add(pipeConnsAsd.get(pipe).get(pd));
				pipeConnsAsd.get(pipe).remove(pd);
			} else if (!oldConns.contains(pd) && newConns.contains(pd)) {
				if (pipe.getPipeType() == PipeType.COLORED) {
					ModelledPipeCOLOREDModel model = ((ModelledPipeCOLOREDModel) pipeModels.get(PipeType.COLORED));
					addAsd.add(model.createCONNArmorStandData(pc, pd));
				} else if (pipe.getPipeType() == PipeType.GOLDEN) {
					ModelledPipeGOLDENModel model = ((ModelledPipeGOLDENModel) pipeModels.get(PipeType.GOLDEN));
					addAsd.add(model.createCONNArmorStandData(pd));
				} else if (pipe.getPipeType() == PipeType.IRON) {
					ModelledPipeIRONModel model = ((ModelledPipeIRONModel) pipeModels.get(PipeType.IRON));
					addAsd.add(model.createCONNArmorStandData(pd, ((IronPipe) pipe).getCurrentOutputDir() == pd));
				} else if (pipe.getPipeType() == PipeType.ICE) {
					ModelledPipeICEModel model = ((ModelledPipeICEModel) pipeModels.get(PipeType.ICE));
					addAsd.add(model.createCONNArmorStandData(pd));
				}
				pipeConnsAsd.get(pipe).put(pd, addAsd.get(addAsd.size() - 1));
			}
		}

	}

	@Override
	public void updateIronPipe(IronPipe pipe, PipeDirection oldOutput, PipeDirection newOutput) {
		
	}

	@Override
	public void removePipe(Pipe pipe) {
		if (!pipeMidAsd.containsKey(pipe)) {
			return;
		}

		ArmorStandData midData = pipeMidAsd.remove(pipe);
		Map<PipeDirection, ArmorStandData> connMap = pipeConnsAsd.remove(pipe);

		//SEND TO CLIENTS

	}

}
