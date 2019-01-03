package de.robotricker.transportpipes.ducts.types.pipetype;

import de.robotricker.transportpipes.ducts.pipe.Pipe;
import de.robotricker.transportpipes.ducts.types.BaseDuctType;
import de.robotricker.transportpipes.ducts.types.DuctType;

public class PipeType extends DuctType {

    public PipeType(BaseDuctType<Pipe> baseDuctType, String name, char colorCode) {
        super(baseDuctType, name, colorCode);
    }

}
