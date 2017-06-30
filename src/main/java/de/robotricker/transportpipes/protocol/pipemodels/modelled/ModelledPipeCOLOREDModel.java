package de.robotricker.transportpipes.protocol.pipemodels.modelled;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.pipeutils.PipeColor;
import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.pipeutils.RelLoc;
import de.robotricker.transportpipes.protocol.ArmorStandData;

public class ModelledPipeCOLOREDModel extends ModelledPipeModel {

	@Override
	public ArmorStandData createColoredMidASD(PipeColor pc) {
		ItemStack hoe = pc.getModelledModel_MidHoeItem();

		ArmorStandData asd = new ArmorStandData(new RelLoc(0.5f, -0.43f, 0.5f), new Vector(1, 0, 0), false, hoe, null, new Vector(180f, 0f, 0f), new Vector(0f, 0f, 0f));

		return asd;
	}

	@Override
	public ArmorStandData createColoredConnASD(PipeColor pc, PipeDirection pd) {
		ItemStack hoe = pc.getModelledModel_ConnHoeItem();
		ArmorStandData asd = null;

		if (pd == PipeDirection.UP) {
			asd = new ArmorStandData(new RelLoc(0.5f, -0.43f, 0.5f), new Vector(1, 0, 0), false, hoe, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f));
		} else if (pd == PipeDirection.DOWN) {
			asd = new ArmorStandData(new RelLoc(0.5f, -0.43f, 0.5f), new Vector(1, 0, 0), false, hoe, null, new Vector(-90f, 0f, 0f), new Vector(0f, 0f, 0f));
		} else {
			asd = new ArmorStandData(new RelLoc(0.5f, -0.43f, 0.5f), new Vector(pd.getX(), 0, pd.getZ()), false, hoe, null, new Vector(180f, 0f, 0f), new Vector(0f, 0f, 0f));
		}

		return asd;
	}

}
