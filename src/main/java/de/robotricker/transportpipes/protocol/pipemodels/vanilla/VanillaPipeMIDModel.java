package de.robotricker.transportpipes.protocol.pipemodels.vanilla;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipeutils.PipeColor;
import de.robotricker.transportpipes.pipeutils.RelLoc;
import de.robotricker.transportpipes.protocol.ArmorStandData;

public class VanillaPipeMIDModel extends VanillaPipeModel {

	@Override
	public List<ArmorStandData> createArmorStandDatas(PipeType pt, PipeColor pc) {
		List<ArmorStandData> asds = new ArrayList<ArmorStandData>();
		ItemStack block = pc.getVanillaModel_GlassItem();
		if (pt == PipeType.ICE) {
			block = ITEM_ICE_BLOCK;
		}

		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.43f, 0.5f), new Vector(1, 0, 0), true, block, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));

		return asds;

	}

}
