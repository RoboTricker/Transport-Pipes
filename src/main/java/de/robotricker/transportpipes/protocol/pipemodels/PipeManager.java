package de.robotricker.transportpipes.protocol.pipemodels;

import org.bukkit.entity.Player;

import de.robotricker.transportpipes.pipes.IronPipe;
import de.robotricker.transportpipes.pipes.Pipe;

public abstract class PipeManager {

	public abstract void sendPipe(Player p, Pipe pipe);

	public abstract void updatePipeShape(Player p, Pipe pipe);

	public abstract void updateIronPipe(Player p, IronPipe pipe);
	
	public abstract void removePipe(Player p, Pipe pipe);

}
