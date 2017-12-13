package de.robotricker.transportpipes.rendersystem;

import java.util.Collection;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.pipes.Duct;
import de.robotricker.transportpipes.pipes.DuctType;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipes.WrappedDirection;
import de.robotricker.transportpipes.pipes.colored.PipeColor;
import de.robotricker.transportpipes.pipeutils.DuctItemUtils;
import de.robotricker.transportpipes.pipeutils.InventoryUtils;
import de.robotricker.transportpipes.pipeutils.PipeDetails;
import de.robotricker.transportpipes.pipeutils.config.LocConf;
import de.robotricker.transportpipes.pipeutils.hitbox.AxisAlignedBB;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.protocol.ArmorStandProtocol;
import de.robotricker.transportpipes.protocol.ProtocolUtils;

public abstract class RenderSystem implements Listener {

	private static ItemStack MODELLED_REPRESENTATION_ITEM = InventoryUtils.createToolItemStack(25);

	static {
		InventoryUtils.changeDisplayName(MODELLED_REPRESENTATION_ITEM, PipeColor.WHITE.getColorCode() + PipeType.COLORED.getFormattedPipeName());
	}

	protected ArmorStandProtocol protocol;

	public RenderSystem(ArmorStandProtocol protocol) {
		this.protocol = protocol;
	}

	/**
	 * creates the needed ASD for this duct and saves it in order to have it ready
	 * for getASDForDuct(Duct)
	 */
	public abstract void createDuctASD(Duct duct, Collection<WrappedDirection> allConnections);

	/**
	 * creates the needed ASD for this duct and saves it in order to have it ready
	 * for getASDForDuct(Duct) also sends the removed and added ASD to all clients
	 * with this renderManager
	 */
	public abstract void updateDuctASD(Duct duct);

	/**
	 * removes all ASD associated with this duct
	 */
	public abstract void destroyDuctASD(Duct duct);

	public abstract List<ArmorStandData> getASDForDuct(Duct duct);

	public int[] getASDIdsForDuct(Duct duct) {
		return ProtocolUtils.convertArmorStandListToEntityIdArray(getASDForDuct(duct));
	}

	public abstract WrappedDirection getClickedDuctFace(Player player, Duct duct);

	public abstract AxisAlignedBB getOuterHitbox(Duct duct);

	public abstract void initPlayer(Player p);

	/**
	 * for one DuctType there mustn't be a renderSystemId for multiple renderSystems
	 */
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

	public static String getRenderSystemIdName(int renderSystemId) {
		if (renderSystemId == 0) {
			return LocConf.load(LocConf.SETTINGS_RENDERSYSTEM_VANILLA);
		} else if (renderSystemId == 1) {
			return LocConf.load(LocConf.SETTINGS_RENDERSYSTEM_MODELLED);
		}
		return null;
	}

	public static ItemStack getRenderSystemIdRepresentationItem(int renderSystemId) {
		if (renderSystemId == 0) {
			return DuctItemUtils.getClonedDuctItem(new PipeDetails(PipeColor.WHITE));
		} else if (renderSystemId == 1) {
			return MODELLED_REPRESENTATION_ITEM.clone();
		}
		return null;
	}
	
	public static int getRenderSystemAmount() {
		return 2;
	}

}
