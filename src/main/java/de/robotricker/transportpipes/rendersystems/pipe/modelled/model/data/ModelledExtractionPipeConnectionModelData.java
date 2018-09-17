package de.robotricker.transportpipes.rendersystems.pipe.modelled.model.data;

import de.robotricker.transportpipes.ducts.pipe.PipeType;
import de.robotricker.transportpipes.utils.TPDirection;

public class ModelledExtractionPipeConnectionModelData extends ModelledPipeConnectionModelData {

    private boolean extractionSide;

    public ModelledExtractionPipeConnectionModelData(TPDirection connectionDir, boolean extractionSide) {
        super(PipeType.valueOf("Extraction"), connectionDir);
        this.extractionSide = extractionSide;
    }

    public boolean isExtractionSide() {
        return extractionSide;
    }
}
