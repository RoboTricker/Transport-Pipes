package de.robotricker.transportpipes.rendersystems.pipe.vanilla.model.data;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.location.TPDirection;

public class VanillaIronPipeModelData extends VanillaPipeModelData {

    private TPDirection outputDir;

    public VanillaIronPipeModelData(BaseDuctType<? extends Duct> baseDuctType, TPDirection outputDir) {
        super(baseDuctType.ductTypeOf("Iron"));
        this.outputDir = outputDir;
    }

    public TPDirection getOutputDir() {
        return outputDir;
    }

}
