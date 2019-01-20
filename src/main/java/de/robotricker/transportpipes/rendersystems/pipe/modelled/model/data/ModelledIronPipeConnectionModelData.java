package de.robotricker.transportpipes.rendersystems.pipe.modelled.model.data;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.location.TPDirection;

public class ModelledIronPipeConnectionModelData extends ModelledPipeConnectionModelData {

    private boolean outputSide;

    public ModelledIronPipeConnectionModelData(BaseDuctType<? extends Duct> baseDuctType, TPDirection connectionDir, boolean outputSide) {
        super(baseDuctType.ductTypeOf("Iron"), connectionDir);
        this.outputSide = outputSide;
    }

    public boolean isOutputSide() {
        return outputSide;
    }
}
