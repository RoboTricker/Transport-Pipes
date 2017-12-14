package de.robotricker.transportpipes.rendersystem.vanilla;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.duct.pipe.utils.PipeColor;
import de.robotricker.transportpipes.pipeitems.RelLoc;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.rendersystem.vanilla.utils.VanillaPipeModelData;
import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.hitbox.AxisAlignedBB;

public class VanillaPipeMIDModel extends VanillaPipeModel {

	public VanillaPipeMIDModel() {
		super();
		aabb = new AxisAlignedBB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
	}

	@Override
	public List<ArmorStandData> createASD(VanillaPipeModelData data) {
		switch (data.getPipeType()) {
		case COLORED:
			return createColoredASD(data.getColoredPipe_pipeColor());
		case ICE:
			return createIceASD();
		case GOLDEN:
			return createGoldenASD();
		case IRON:
			return createIronASD(data.getIronPipe_outputDirection());
		case VOID:
			return createVoidASD();
		case EXTRACTION:
			return createExtractionASD();
		default:
			return null;
		}
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

	private List<ArmorStandData> createIronASD(WrappedDirection outputPd) {
		List<ArmorStandData> asds = new ArrayList<>();

		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.43f, 0.5f), new Vector(1, 0, 0), true, ITEM_IRON_BLOCK, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f + 0.26f, -0.255f, 0.5f), new Vector(1, 0, 0), true, outputPd == WrappedDirection.EAST ? ITEM_CARPET_YELLOW : ITEM_CARPET_WHITE, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f - 0.26f, -0.255f, 0.5f), new Vector(-1, 0, 0), true, outputPd == WrappedDirection.WEST ? ITEM_CARPET_YELLOW : ITEM_CARPET_WHITE, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.255f, 0.5f + 0.26f), new Vector(0, 0, 1), true, outputPd == WrappedDirection.SOUTH ? ITEM_CARPET_YELLOW : ITEM_CARPET_WHITE, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.255f, 0.5f - 0.26f), new Vector(0, 0, -1), true, outputPd == WrappedDirection.NORTH ? ITEM_CARPET_YELLOW : ITEM_CARPET_WHITE, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.255f + 0.26f, 0.5f), new Vector(1, 0, 0), true, outputPd == WrappedDirection.UP ? ITEM_CARPET_YELLOW : ITEM_CARPET_WHITE, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.255f - 0.26f, 0.5f), new Vector(1, 0, 0), true, outputPd == WrappedDirection.DOWN ? ITEM_CARPET_YELLOW : ITEM_CARPET_WHITE, null, new Vector(180f, 0f, 0f), new Vector(0f, 0f, 0f)));

		return asds;

	}

	private List<ArmorStandData> createVoidASD() {
		List<ArmorStandData> asds = new ArrayList<>();

		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.43f, 0.5f), new Vector(1, 0, 0), true, ITEM_VOID_BLOCK, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));

		return asds;

	}

	private List<ArmorStandData> createExtractionASD() {
		List<ArmorStandData> asds = new ArrayList<>();

		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.43f, 0.5f), new Vector(1, 0, 0), true, ITEM_EXTRACTION_BLOCK, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));

		return asds;

	}

}
