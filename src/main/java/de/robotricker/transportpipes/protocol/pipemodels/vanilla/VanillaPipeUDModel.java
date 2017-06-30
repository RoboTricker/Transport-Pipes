package de.robotricker.transportpipes.protocol.pipemodels.vanilla;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.pipeutils.PipeColor;
import de.robotricker.transportpipes.pipeutils.RelLoc;
import de.robotricker.transportpipes.protocol.ArmorStandData;

public class VanillaPipeUDModel extends VanillaPipeModel {

	@Override
	public List<ArmorStandData> createColoredASD(PipeColor pc) {
		List<ArmorStandData> asds = new ArrayList<ArmorStandData>();
		ItemStack block = pc.getVanillaModel_GlassItem();

		asds.add(new ArmorStandData(new RelLoc(0.05f + 1.3f, -1.3f, 0.5f - 0.25f), new Vector(1, 0, 1), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 90f, 90f)));
		asds.add(new ArmorStandData(new RelLoc(0.05f + 0.8f, -1.3f, 0.5f - 0.75f), new Vector(1, 0, 1), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 90f, 90f)));
		asds.add(new ArmorStandData(new RelLoc(0.05f + 1.2f, -1.3f, 0.5f + 0.4f), new Vector(-1, 0, 1), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 90f, 90f)));
		asds.add(new ArmorStandData(new RelLoc(0.05f + 0.74f, -1.3f, 0.5f + 0.84f), new Vector(-1, 0, 1), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 90f, 90f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.675f, 0.5f), new Vector(1, 0, 0), true, block, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.175f, 0.5f), new Vector(1, 0, 0), true, block, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));

		return asds;

	}

	@Override
	public List<ArmorStandData> createIceASD() {
		List<ArmorStandData> asds = new ArrayList<ArmorStandData>();
		ItemStack block = ITEM_ICE_BLOCK;

		asds.add(new ArmorStandData(new RelLoc(0.05f + 1.3f, -1.3f, 0.5f - 0.25f), new Vector(1, 0, 1), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 90f, 90f)));
		asds.add(new ArmorStandData(new RelLoc(0.05f + 0.8f, -1.3f, 0.5f - 0.75f), new Vector(1, 0, 1), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 90f, 90f)));
		asds.add(new ArmorStandData(new RelLoc(0.05f + 1.2f, -1.3f, 0.5f + 0.4f), new Vector(-1, 0, 1), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 90f, 90f)));
		asds.add(new ArmorStandData(new RelLoc(0.05f + 0.74f, -1.3f, 0.5f + 0.84f), new Vector(-1, 0, 1), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 90f, 90f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.675f, 0.5f), new Vector(1, 0, 0), true, block, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.175f, 0.5f), new Vector(1, 0, 0), true, block, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));

		return asds;

	}

}
