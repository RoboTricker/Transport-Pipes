package de.robotricker.transportpipes.rendersystem.pipe.vanilla.model.data;

import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.location.Direction;

public class VanillaIronPipeModelData extends VanillaPipeModelData {

    private Direction outputDir;

    public VanillaIronPipeModelData(Direction outputDir) {
        super(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Iron"));
        this.outputDir = outputDir;
    }

    public Direction getOutputDir() {
        return outputDir;
    }

}
