package de.robotricker.transportpipes.rendersystem.vanilla;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.pipeitems.RelLoc;
import de.robotricker.transportpipes.pipes.PipeDirection;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipes.colored.PipeColor;
import de.robotricker.transportpipes.pipeutils.hitbox.AxisAlignedBB;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.rendersystem.vanilla.utils.VanillaPipeModelData;

public class VanillaPipeMIDModel extends VanillaPipeModel {

	public VanillaPipeMIDModel() {
		super();
		aabb = new AxisAlignedBB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
	}

	@Override
	public List<ArmorStandData> createASD(VanillaPipeModelData data) {
		if (data.getPipeType() == PipeType.COLORED) {
			return createColoredASD(data.getColoredPipe_pipeColor());
		} else if (data.getPipeType() == PipeType.ICE) {
			return createIceASD();
		} else if (data.getPipeType() == PipeType.GOLDEN) {
			return createGoldenASD();
		} else if (data.getPipeType() == PipeType.IRON) {
			return createIronASD(data.getIronPipe_outputDirection());
		} else if (data.getPipeType() == PipeType.VOID) {
			return createVoidASD();
		}
		return null;
	}

	private List<ArmorStandData> createColoredASD(PipeColor pc) {
		List<ArmorStandData> asds = new ArrayList<>();
		ItemStack block = pc.getVanillaModel_GlassItem();

		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.43f, 0.5f), new Vector(1, 0, 0), true, block, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));

		return asds;

	}

	private List<ArmorStandData> createIceASD() {
		List<ArmorStandData> asds = new ArrayList<>();

		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.43f, 0.5f), new Vector(1, 0, 0), true, ITEM_ICE_BLOCK, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));

		return asds;

	}

	private List<ArmorStandData> createGoldenASD() {
		List<ArmorStandData> asds = new ArrayList<>();

		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.43f, 0.5f), new Vector(1, 0, 0), true, ITEM_GOLD_BLOCK, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f + 0.26f, -0.255f, 0.5f), new Vector(1, 0, 0), true, ITEM_CARPET_BLUE, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f - 0.26f, -0.255f, 0.5f), new Vector(-1, 0, 0), true, ITEM_CARPET_YELLOW, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.255f, 0.5f + 0.26f), new Vector(0, 0, 1), true, ITEM_CARPET_RED, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.255f, 0.5f - 0.26f), new Vector(0, 0, -1), true, ITEM_CARPET_WHITE, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.255f + 0.26f, 0.5f), new Vector(1, 0, 0), true, ITEM_CARPET_GREEN, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.255f - 0.26f, 0.5f), new Vector(1, 0, 0), true, ITEM_CARPET_BLACK, null, new Vector(180f, 0f, 0f), new Vector(0f, 0f, 0f)));

		return asds;

	}

	private List<ArmorStandData> createIronASD(PipeDirection outputPd) {
		List<ArmorStandData> asds = new ArrayList<>();

		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.43f, 0.5f), new Vector(1, 0, 0), true, ITEM_IRON_BLOCK, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f + 0.26f, -0.255f, 0.5f), new Vector(1, 0, 0), true, outputPd == PipeDirection.EAST ? ITEM_CARPET_YELLOW : ITEM_CARPET_WHITE, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f - 0.26f, -0.255f, 0.5f), new Vector(-1, 0, 0), true, outputPd == PipeDirection.WEST ? ITEM_CARPET_YELLOW : ITEM_CARPET_WHITE, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.255f, 0.5f + 0.26f), new Vector(0, 0, 1), true, outputPd == PipeDirection.SOUTH ? ITEM_CARPET_YELLOW : ITEM_CARPET_WHITE, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.255f, 0.5f - 0.26f), new Vector(0, 0, -1), true, outputPd == PipeDirection.NORTH ? ITEM_CARPET_YELLOW : ITEM_CARPET_WHITE, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.255f + 0.26f, 0.5f), new Vector(1, 0, 0), true, outputPd == PipeDirection.UP ? ITEM_CARPET_YELLOW : ITEM_CARPET_WHITE, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.255f - 0.26f, 0.5f), new Vector(1, 0, 0), true, outputPd == PipeDirection.DOWN ? ITEM_CARPET_YELLOW : ITEM_CARPET_WHITE, null, new Vector(180f, 0f, 0f), new Vector(0f, 0f, 0f)));

		return asds;

	}
	
	private List<ArmorStandData> createVoidASD() {
		List<ArmorStandData> asds = new ArrayList<>();

		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.43f, 0.5f), new Vector(1, 0, 0), true, ITEM_VOID_BLOCK, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));

		return asds;

	}

}
