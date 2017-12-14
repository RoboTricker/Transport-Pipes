package de.robotricker.transportpipes.rendersystem.modelled;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.pipeitems.RelLoc;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.rendersystem.modelled.utils.ModelledPipeConnModelData;
import de.robotricker.transportpipes.rendersystem.modelled.utils.ModelledPipeMidModelData;
import de.robotricker.transportpipes.utils.WrappedDirection;

public class ModelledPipeCOLOREDModel extends ModelledPipeModel {

	@Override
	public ArmorStandData createMidASD(ModelledPipeMidModelData data) {
		ItemStack hoe = data.getColoredPipe_pipeColor().getModelledModel_MidHoeItem();

		return new ArmorStandData(new RelLoc(0.5f, 0.5f - 1.1875f, 0.5f), new Vector(1, 0, 0), false, hoe, null, new Vector(180f, 0f, 0f), new Vector(0f, 0f, 0f));
	}

	@Override
	public ArmorStandData createConnASD(ModelledPipeConnModelData data) {
		ItemStack hoe = data.getColoredPipe_pipeColor().getModelledModel_ConnHoeItem();
		ArmorStandData asd;

		if (data.getConnDirection() == WrappedDirection.UP) {
			asd = new ArmorStandData(new RelLoc(0.75f, 0.5f - 1.4369f, 0.5f), new Vector(1, 0, 0), false, hoe, null, new Vector(-90f, 0f, 0f), new Vector(0f, 0f, 0f));
		} else if (data.getConnDirection() == WrappedDirection.DOWN) {
			asd = new ArmorStandData(new RelLoc(0.25f, 0.5f - 1.1885f - 0.25f, 0.5f), new Vector(1, 0, 0), false, hoe, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f));
		} else {
			asd = new ArmorStandData(new RelLoc(0.5f, 0.5f - 1.1875f, 0.5f), new Vector(data.getConnDirection().getX(), 0, data.getConnDirection().getZ()), false, hoe, null, new Vector(180f, 180f, 0f), new Vector(0f, 0f, 0f));
		}

		return asd;
	}

}
