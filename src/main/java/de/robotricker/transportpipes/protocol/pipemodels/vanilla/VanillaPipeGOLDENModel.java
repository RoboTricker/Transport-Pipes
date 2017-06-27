package de.robotricker.transportpipes.protocol.pipemodels.vanilla;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.util.Vector;

import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipeutils.PipeColor;
import de.robotricker.transportpipes.pipeutils.RelLoc;
import de.robotricker.transportpipes.protocol.ArmorStandData;

public class VanillaPipeGOLDENModel extends VanillaPipeModel {

	@Override
	public List<ArmorStandData> createArmorStandDatas(PipeType pt, PipeColor pc) {
		List<ArmorStandData> asds = new ArrayList<ArmorStandData>();

		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.43f, 0.5f), new Vector(1, 0, 0), true, ITEM_GOLD_BLOCK, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f + 0.26f, -0.255f, 0.5f), new Vector(1, 0, 0), true, ITEM_CARPET_WHITE, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f - 0.26f, -0.255f, 0.5f), new Vector(-1, 0, 0), true, ITEM_CARPET_YELLOW, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.255f, 0.5f + 0.26f), new Vector(0, 0, 1), true, ITEM_CARPET_GREEN, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.255f, 0.5f - 0.26f), new Vector(0, 0, -1), true, ITEM_CARPET_BLUE, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.255f + 0.26f, 0.5f), new Vector(1, 0, 0), true, ITEM_CARPET_RED, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.255f - 0.26f, 0.5f), new Vector(1, 0, 0), true, ITEM_CARPET_BLACK, null, new Vector(180f, 0f, 0f), new Vector(0f, 0f, 0f)));

		return asds;

	}

}
