package de.robotricker.transportpipes.duct.types.pipetype;

import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.duct.types.DuctType;

public class PipeType extends DuctType {

    public PipeType(BaseDuctType<Pipe> baseDuctType, String name, String displayName, String craftingPermission) {
        super(baseDuctType, name, displayName,
                craftingPermission);
    }

}
