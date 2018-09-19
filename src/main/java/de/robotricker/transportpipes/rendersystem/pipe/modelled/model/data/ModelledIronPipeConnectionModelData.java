package de.robotricker.transportpipes.rendersystem.pipe.modelled.model.data;

import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.location.Direction;

public class ModelledIronPipeConnectionModelData extends ModelledPipeConnectionModelData {

    private boolean outputSide;

    public ModelledIronPipeConnectionModelData(Direction connectionDir, boolean outputSide) {
        super(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Iron"), connectionDir);
        this.outputSide = outputSide;
    }

    public boolean isOutputSide() {
        return outputSide;
    }
}
