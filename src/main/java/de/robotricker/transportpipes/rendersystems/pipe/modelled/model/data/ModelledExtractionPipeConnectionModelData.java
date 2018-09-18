package de.robotricker.transportpipes.rendersystems.pipe.modelled.model.data;

import de.robotricker.transportpipes.ducts.types.BasicDuctType;
import de.robotricker.transportpipes.ducts.types.PipeType;
import de.robotricker.transportpipes.utils.TPDirection;

public class ModelledExtractionPipeConnectionModelData extends ModelledPipeConnectionModelData {

    private boolean extractionSide;

    public ModelledExtractionPipeConnectionModelData(TPDirection connectionDir, boolean extractionSide) {
        super(BasicDuctType.valueOf("Pipe").ductTypeValueOf("Extraction"), connectionDir);
        this.extractionSide = extractionSide;
    }

    public boolean isExtractionSide() {
        return extractionSide;
    }
}
