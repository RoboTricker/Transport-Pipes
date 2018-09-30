package de.robotricker.transportpipes.rendersystems.pipe.modelled.model.data;

import de.robotricker.transportpipes.ducts.types.BaseDuctType;
import de.robotricker.transportpipes.location.TPDirection;

public class ModelledIronPipeConnectionModelData extends ModelledPipeConnectionModelData {

    private boolean outputSide;

    public ModelledIronPipeConnectionModelData(TPDirection connectionDir, boolean outputSide) {
        super(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Iron"), connectionDir);
        this.outputSide = outputSide;
    }

    public boolean isOutputSide() {
        return outputSide;
    }
}
