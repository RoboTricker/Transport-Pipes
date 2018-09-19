package de.robotricker.transportpipes.rendersystem.pipe.modelled.model.data;

import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.location.Direction;

public class ModelledExtractionPipeConnectionModelData extends ModelledPipeConnectionModelData {

    private boolean extractionSide;

    public ModelledExtractionPipeConnectionModelData(Direction connectionDir, boolean extractionSide) {
        super(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Extraction"), connectionDir);
        this.extractionSide = extractionSide;
    }

    public boolean isExtractionSide() {
        return extractionSide;
    }
}
