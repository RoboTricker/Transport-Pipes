package de.robotricker.transportpipes.protocol.pipemodels;

import de.robotricker.transportpipes.pipes.IronPipe;
import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.protocol.ArmorStandProtocol;

public abstract class PipeManager {

	protected ArmorStandProtocol protocol;

	public PipeManager(ArmorStandProtocol protocol) {
		this.protocol = protocol;
	}

	public abstract void sendPipe(Pipe pipe);

	public abstract void updatePipeShape(Pipe pipe);

	public abstract void updateIronPipe(IronPipe pipe, PipeDirection oldOutput, PipeDirection newOutput);

	public abstract void removePipe(Pipe pipe);

}
