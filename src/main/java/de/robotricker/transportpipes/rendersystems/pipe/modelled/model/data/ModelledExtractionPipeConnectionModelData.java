package de.robotricker.transportpipes.rendersystems.pipe.modelled.model.data;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.location.TPDirection;

public class ModelledExtractionPipeConnectionModelData extends ModelledPipeConnectionModelData {

    private boolean extractionSide;

    public ModelledExtractionPipeConnectionModelData(BaseDuctType<? extends Duct> baseDuctType, TPDirection connectionDir, boolean extractionSide) {
        super(baseDuctType.ductTypeOf("Extraction"), connectionDir);
        this.extractionSide = extractionSide;
    }

    public boolean isExtractionSide() {
        return extractionSide;
    }
}
