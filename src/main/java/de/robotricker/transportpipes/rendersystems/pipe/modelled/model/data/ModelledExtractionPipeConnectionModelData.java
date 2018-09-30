package de.robotricker.transportpipes.rendersystems.pipe.modelled.model.data;

import de.robotricker.transportpipes.ducts.types.BaseDuctType;
import de.robotricker.transportpipes.location.TPDirection;

public class ModelledExtractionPipeConnectionModelData extends ModelledPipeConnectionModelData {

    private boolean extractionSide;

    public ModelledExtractionPipeConnectionModelData(TPDirection connectionDir, boolean extractionSide) {
        super(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Extraction"), connectionDir);
        this.extractionSide = extractionSide;
    }

    public boolean isExtractionSide() {
        return extractionSide;
    }
}
