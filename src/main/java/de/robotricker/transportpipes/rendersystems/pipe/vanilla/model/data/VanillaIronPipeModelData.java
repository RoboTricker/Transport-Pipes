package de.robotricker.transportpipes.rendersystems.pipe.vanilla.model.data;

import de.robotricker.transportpipes.ducts.types.BaseDuctType;
import de.robotricker.transportpipes.location.TPDirection;

public class VanillaIronPipeModelData extends VanillaPipeModelData {

    private TPDirection outputDir;

    public VanillaIronPipeModelData(TPDirection outputDir) {
        super(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Iron"));
        this.outputDir = outputDir;
    }

    public TPDirection getOutputDir() {
        return outputDir;
    }

}
