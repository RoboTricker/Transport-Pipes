package de.robotricker.transportpipes.rendersystems.pipe.modelled.model.data;

import de.robotricker.transportpipes.ducts.types.BasicDuctType;
import de.robotricker.transportpipes.ducts.types.PipeType;
import de.robotricker.transportpipes.utils.TPDirection;

public class ModelledIronPipeConnectionModelData extends ModelledPipeConnectionModelData {

    private boolean outputSide;

    public ModelledIronPipeConnectionModelData(TPDirection connectionDir, boolean outputSide) {
        super(BasicDuctType.valueOf("Pipe").ductTypeValueOf("Iron"), connectionDir);
        this.outputSide = outputSide;
    }

    public boolean isOutputSide() {
        return outputSide;
    }
}
