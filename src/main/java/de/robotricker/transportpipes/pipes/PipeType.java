package de.robotricker.transportpipes.pipes;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.pipes.colored.PipeColor;
import de.robotricker.transportpipes.pipes.types.ColoredPipe;
import de.robotricker.transportpipes.pipes.types.GoldenPipe;
import de.robotricker.transportpipes.pipes.types.IcePipe;
import de.robotricker.transportpipes.pipes.types.IronPipe;
import de.robotricker.transportpipes.pipes.types.Pipe;
import de.robotricker.transportpipes.pipeutils.config.LocConf;

public enum PipeType {

	COLORED(0, "", LocConf.PIPES_COLORED),
	GOLDEN(1, "§6", LocConf.PIPES_GOLDEN),
	IRON(2, "§7", LocConf.PIPES_IRON),
	ICE(3, "§b", LocConf.PIPES_ICE);

	private int id;
	private String pipeName_colorCode;
	private String pipeName_locConfKey;

	PipeType(int id, String pipeName_colorCode, String pipeName_locConfKey) {
		this.id = id;
		this.pipeName_colorCode = pipeName_colorCode;
		this.pipeName_locConfKey = pipeName_locConfKey;
	}

	public int getId() {
		return id;
	}

	public String getFormattedPipeName() {
		return pipeName_colorCode + LocConf.load(pipeName_locConfKey);
	}

	public Pipe createPipe(Location blockLoc, PipeColor pc) {
		if (this == COLORED) {
			return new ColoredPipe(blockLoc, pc);
		} else if (this == GOLDEN) {
			return new GoldenPipe(blockLoc);
		} else if (this == IRON) {
			return new IronPipe(blockLoc);
		} else if (this == ICE) {
			return new IcePipe(blockLoc);
		}
		return null;
	}

	public static PipeType getFromId(int id) {
		for (PipeType pt : PipeType.values()) {
			if (pt.getId() == id) {
				return pt;
			}
		}
		return null;
	}

	/**
	 * returns the pipeType you can place with this item, or null if there is no pipe available for this item
	 */
	public static PipeType getFromPipeItem(ItemStack item) {
		if (item == null) {
			return null;
		}
		if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
			String displayName = item.getItemMeta().getDisplayName();
			if (PipeColor.getPipeColorByPipeItem(item) != null) {
				return PipeType.COLORED;
			}
			for (PipeType pt : PipeType.values()) {
				if (displayName.equals(pt.getFormattedPipeName())) {
					return pt;
				}
			}
		}
		return null;
	}

}
