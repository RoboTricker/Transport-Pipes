package de.robotricker.transportpipes.protocol.pipemodels;

import java.util.List;

import org.bukkit.entity.Player;

import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipeutils.PipeDirection;

public abstract class PipeModel {

	public abstract void sendPipe(Player p, Pipe pipe);

	public abstract void updatePipe(Player p, Pipe pipe, List<PipeDirection> oldConns, List<PipeDirection> newConns);

	public abstract void removePipe(Player p, Pipe pipe);

}
