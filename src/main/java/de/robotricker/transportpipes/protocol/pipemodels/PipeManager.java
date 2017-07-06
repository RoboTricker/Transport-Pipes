package de.robotricker.transportpipes.protocol.pipemodels;

import java.util.List;

import org.bukkit.entity.Player;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.protocol.ArmorStandProtocol;

public abstract class PipeManager {

	protected ArmorStandProtocol protocol;

	public PipeManager(ArmorStandProtocol protocol) {
		this.protocol = protocol;
	}

	/**
	 * creates the needed ASD for this pipe and saves it in order to have it ready for getASDForPipe(Pipe)
	 */
	public abstract void createPipeASD(Pipe pipe, List<PipeDirection> allConnections);

	/**
	 * creates the needed ASD for this pipe and saves it in order to have it ready for getASDForPipe(Pipe) also sends the removed and added ASD to all clients with this PipeManager
	 */
	public abstract void updatePipeASD(Pipe pipe);

	/**
	 * removes all ASD associated with this pipe and sends this removed ASD to all clients with this PipeManager
	 */
	public abstract void destroyPipeASD(Pipe pipe);

	public abstract List<ArmorStandData> getASDForPipe(Pipe pipe);

	public int[] getASDIdsForPipe(Pipe pipe) {
		return TransportPipes.instance.convertArmorStandListToEntityIdArray(getASDForPipe(pipe));
	}

	public abstract PipeDirection getClickedPipeFace(Player player, Pipe pipe);

}
