package de.robotricker.transportpipes.rendersystem;

import java.util.Collection;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipes.PipeDirection;
import de.robotricker.transportpipes.pipes.types.Pipe;
import de.robotricker.transportpipes.pipeutils.hitbox.AxisAlignedBB;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.protocol.ArmorStandProtocol;
import de.robotricker.transportpipes.protocol.ProtocolUtils;

public abstract class PipeRenderSystem implements Listener {

	protected ArmorStandProtocol protocol;

	public PipeRenderSystem(ArmorStandProtocol protocol) {
		this.protocol = protocol;
	}

	/**
	 * creates the needed ASD for this pipe and saves it in order to have it ready for getASDForPipe(Pipe)
	 */
	public abstract void createPipeASD(Pipe pipe, Collection<PipeDirection> allConnections);

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
		return ProtocolUtils.convertArmorStandListToEntityIdArray(getASDForPipe(pipe));
	}

	public abstract PipeDirection getClickedPipeFace(Player player, Pipe pipe);

	public abstract AxisAlignedBB getOuterHitbox(Pipe pipe);
	
	public abstract void initPlayer(Player p);

	public abstract String getPipeRenderSystemName();

	public abstract ItemStack getRepresentationItem();

	public abstract int getRenderSystemId();

	public static PipeRenderSystem getRenderSystemFromId(int renderSystemId) {
		for (PipeRenderSystem prs : TransportPipes.instance.getPipeRenderSystems()) {
			if (prs.getRenderSystemId() == renderSystemId) {
				return prs;
			}
		}
		return null;
	}

}
