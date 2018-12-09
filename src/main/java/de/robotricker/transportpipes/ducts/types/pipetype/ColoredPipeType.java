package de.robotricker.transportpipes.ducts.types.pipetype;

import de.robotricker.transportpipes.ducts.pipe.Pipe;
import de.robotricker.transportpipes.ducts.types.BaseDuctType;

public class ColoredPipeType extends PipeType {

    public ColoredPipeType(BaseDuctType<Pipe> baseDuctType, String name, char colorCode) {
        super(baseDuctType, name, colorCode);
    }

}
