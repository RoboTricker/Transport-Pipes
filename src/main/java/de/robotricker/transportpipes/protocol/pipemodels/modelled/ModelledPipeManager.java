package de.robotricker.transportpipes.protocol.pipemodels.modelled;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import de.robotricker.transportpipes.pipes.IronPipe;
import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.protocol.pipemodels.PipeManager;

public class ModelledPipeManager extends PipeManager {

	private Map<PipeType, ModelledPipeModel> pipeModels = new HashMap<PipeType, ModelledPipeModel>();

	public ModelledPipeManager() {
		pipeModels.put(PipeType.COLORED, new ModelledPipeCOLOREDModel());
		pipeModels.put(PipeType.ICE, new ModelledPipeICEModel());
		pipeModels.put(PipeType.GOLDEN, new ModelledPipeGOLDENModel());
		pipeModels.put(PipeType.IRON, new ModelledPipeIRONModel());
	}

	@Override
	public void sendPipe(Player p, Pipe pipe) {
		
	}

	@Override
	public void updatePipeShape(Player p, Pipe pipe) {

	}
	
	@Override
	public void updateIronPipe(Player p, IronPipe pipe){
		
	}

	@Override
	public void removePipe(Player p, Pipe pipe) {

	}

}
