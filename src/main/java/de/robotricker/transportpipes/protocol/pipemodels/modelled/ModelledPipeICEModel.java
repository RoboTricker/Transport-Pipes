package de.robotricker.transportpipes.protocol.pipemodels.modelled;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.pipeutils.RelLoc;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.protocol.pipemodels.modelled.utils.ModelledPipeConnModelData;
import de.robotricker.transportpipes.protocol.pipemodels.modelled.utils.ModelledPipeMidModelData;

public class ModelledPipeICEModel extends ModelledPipeModel {

	@Override
	public ArmorStandData createMidASD(ModelledPipeMidModelData data) {
		ItemStack hoe = ITEM_HOE_MID_ICE;

		ArmorStandData asd = new ArmorStandData(new RelLoc(0.5f, 0.5f - 1.1875f, 0.5f), new Vector(1, 0, 0), false, hoe, null, new Vector(180f, 0f, 0f), new Vector(0f, 0f, 0f));

		return asd;
	}

	@Override
	public ArmorStandData createConnASD(ModelledPipeConnModelData data) {
		ItemStack hoe = ITEM_HOE_CONN_ICE;
		ArmorStandData asd = null;

		if (data.getConnDirection() == PipeDirection.UP) {
			asd = new ArmorStandData(new RelLoc(0.75f, 0.5f - 1.4369f, 0.5f), new Vector(1, 0, 0), false, hoe, null, new Vector(-90f, 0f, 0f), new Vector(0f, 0f, 0f));
		} else if (data.getConnDirection() == PipeDirection.DOWN) {
			asd = new ArmorStandData(new RelLoc(0.25f, 0.5f - 1.1885f - 0.25f, 0.5f), new Vector(1, 0, 0), false, hoe, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f));
		} else {
			asd = new ArmorStandData(new RelLoc(0.5f, 0.5f - 1.1875f, 0.5f), new Vector(data.getConnDirection().getX(), 0, data.getConnDirection().getZ()), false, hoe, null, new Vector(180f, 180f, 0f), new Vector(0f, 0f, 0f));
		}

		return asd;
	}

}
