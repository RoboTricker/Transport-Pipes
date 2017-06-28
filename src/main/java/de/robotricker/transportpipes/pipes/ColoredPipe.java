package de.robotricker.transportpipes.pipes;

import java.util.List;

import org.bukkit.Location;

import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipeutils.PipeColor;
import de.robotricker.transportpipes.pipeutils.PipeDirection;

public class ColoredPipe extends Pipe{

	private PipeColor pipeColor;
	
	public ColoredPipe(Location blockLoc, PipeColor pipeColor) {
		super(blockLoc);
		this.pipeColor = pipeColor;
	}

	@Override
	public PipeDirection itemArrivedAtMiddle(PipeItem item, PipeDirection before, List<PipeDirection> dirs) {
		return null;
	}

	@Override
	public void destroy(boolean dropPipeItem) {
		
	}
	
	public PipeColor getPipeColor(){
		return pipeColor;
	}

}
