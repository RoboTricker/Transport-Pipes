package de.robotricker.transportpipes.protocol.pipemodels;

import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.protocol.ArmorStandProtocol;

public abstract class PipeManager {

	protected ArmorStandProtocol protocol;

	public PipeManager(ArmorStandProtocol protocol) {
		this.protocol = protocol;
	}

	public abstract void sendPipe(Pipe pipe);

	public abstract void updatePipe(Pipe pipe);

	public abstract void destroyPipe(Pipe pipe);

}
