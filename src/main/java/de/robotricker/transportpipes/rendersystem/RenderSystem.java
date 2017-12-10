package de.robotricker.transportpipes.rendersystem;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipes.Duct;
import de.robotricker.transportpipes.pipes.DuctType;
import de.robotricker.transportpipes.pipes.WrappedDirection;
import de.robotricker.transportpipes.pipes.types.Pipe;
import de.robotricker.transportpipes.pipeutils.hitbox.AxisAlignedBB;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.protocol.ArmorStandProtocol;
import de.robotricker.transportpipes.protocol.ProtocolUtils;

public abstract class RenderSystem implements Listener {

	protected ArmorStandProtocol protocol;

	public RenderSystem(ArmorStandProtocol protocol) {
		this.protocol = protocol;
	}

	/**
	 * creates the needed ASD for this pipe and saves it in order to have it ready
	 * for getASDForPipe(Pipe)
	 */
	public abstract void createDuctASD(Duct duct, Collection<WrappedDirection> allConnections);

	/**
	 * creates the needed ASD for this pipe and saves it in order to have it ready
	 * for getASDForPipe(Pipe) also sends the removed and added ASD to all clients
	 * with this PipeManager
	 */
	public abstract void updateDuctASD(Duct duct);

	/**
	 * removes all ASD associated with this pipe
	 */
	public abstract void destroyDuctASD(Duct duct);

	public abstract List<ArmorStandData> getASDForDuct(Duct duct);

	public int[] getASDIdsForDuct(Duct duct) {
		return ProtocolUtils.convertArmorStandListToEntityIdArray(getASDForDuct(duct));
	}

	public abstract WrappedDirection getClickedDuctFace(Player player, Duct duct);

	public abstract AxisAlignedBB getOuterHitbox(Duct duct);

	public abstract void initPlayer(Player p);

	public abstract String getPipeRenderSystemName();

	public abstract ItemStack getRepresentationItem();

	public abstract int[] getRenderSystemIds();

	public abstract DuctType getDuctType();
	
	public static RenderSystem getRenderSystemFromId(int renderSystemId, DuctType ductType) {
		for (RenderSystem prs : ductType.getRenderSystems()) {
			for (int id : prs.getRenderSystemIds()) {
				if (id == renderSystemId) {
					return prs;
				}
			}
		}
		return null;
	}

}
