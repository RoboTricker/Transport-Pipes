package de.robotricker.transportpipes.rendersystems.pipe.vanilla.model.data;

import de.robotricker.transportpipes.ducts.pipe.PipeType;
import de.robotricker.transportpipes.utils.TPDirection;

public class VanillaIronPipeModelData extends VanillaPipeModelData {

    private TPDirection outputDir;

    public VanillaIronPipeModelData(TPDirection outputDir) {
        super(PipeType.valueOf("Iron"));
        this.outputDir = outputDir;
    }

    public TPDirection getOutputDir() {
        return outputDir;
    }

}
