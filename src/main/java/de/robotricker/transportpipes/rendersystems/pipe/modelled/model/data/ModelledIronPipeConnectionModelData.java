package de.robotricker.transportpipes.rendersystems.pipe.modelled.model.data;

import de.robotricker.transportpipes.ducts.pipe.PipeType;
import de.robotricker.transportpipes.utils.TPDirection;

public class ModelledIronPipeConnectionModelData extends ModelledPipeConnectionModelData {

    private boolean outputSide;

    public ModelledIronPipeConnectionModelData(TPDirection connectionDir, boolean outputSide) {
        super(PipeType.valueOf("Iron"), connectionDir);
        this.outputSide = outputSide;
    }

    public boolean isOutputSide() {
        return outputSide;
    }
}
