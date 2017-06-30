package de.robotricker.transportpipes.protocol.pipemodels.modelled;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipeutils.PipeColor;
import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.protocol.pipemodels.PipeModel;

public abstract class ModelledPipeModel extends PipeModel {

	protected static final ItemStack ITEM_HOE_MID_ICE = new ItemStack(Material.WOOD_HOE, 1, (short) 10);
	protected static final ItemStack ITEM_HOE_CONN_ICE = new ItemStack(Material.WOOD_HOE, 1, (short) 11);
	protected static final ItemStack ITEM_HOE_MID_GOLDEN = new ItemStack(Material.WOOD_HOE, 1, (short) 10);
	protected static final ItemStack ITEM_HOE_CONN_GOLDEN = new ItemStack(Material.WOOD_HOE, 1, (short) 11);
	protected static final ItemStack ITEM_HOE_MID_IRON = new ItemStack(Material.WOOD_HOE, 1, (short) 10);
	protected static final ItemStack ITEM_HOE_CONN_IRON = new ItemStack(Material.WOOD_HOE, 1, (short) 11);
	protected static final ItemStack ITEM_HOE_CONN_IRON_OUTPUT = new ItemStack(Material.WOOD_HOE, 1, (short) 12);

	public ArmorStandData createGoldenMidASD() {
		return null;
	}

	public ArmorStandData createGoldenConnASD(Color c, PipeDirection pd) {
		return null;
	}

	public ArmorStandData createIronMidASD() {
		return null;
	}

	public ArmorStandData createIronConnASD(boolean output, PipeDirection pd) {
		return null;
	}

	public ArmorStandData createIceMidASD() {
		return null;
	}

	public ArmorStandData createIceConnASD(PipeDirection pd) {
		return null;
	}

	public ArmorStandData createColoredMidASD(PipeColor pc) {
		return null;
	}

	public ArmorStandData createColoredConnASD(PipeColor pc, PipeDirection pd) {
		return null;
	}

	public ArmorStandData createMidASD(PipeType pt, PipeColor colored_pipecolor) {
		switch (pt) {
		case COLORED:
			return createColoredMidASD(colored_pipecolor);
		case GOLDEN:
			return createGoldenMidASD();
		case IRON:
			return createGoldenMidASD();
		case ICE:
			return createGoldenMidASD();
		default:
			return null;
		}
	}

	public ArmorStandData createConnASD(PipeType pt, PipeDirection pd, Color golden_color, PipeColor colored_pipecolor, boolean iron_output) {
		switch (pt) {
		case COLORED:
			return createColoredConnASD(colored_pipecolor, pd);
		case GOLDEN:
			return createGoldenConnASD(golden_color, pd);
		case IRON:
			return createIronConnASD(iron_output, pd);
		case ICE:
			return createIceConnASD(pd);
		default:
			return null;
		}
	}

}
