package de.robotricker.transportpipes.rendersystems.pipe.vanilla.model.data;

import de.robotricker.transportpipes.ducts.types.BasicDuctType;
import de.robotricker.transportpipes.utils.TPDirection;

public class VanillaIronPipeModelData extends VanillaPipeModelData {

    private TPDirection outputDir;

    public VanillaIronPipeModelData(TPDirection outputDir) {
        super(BasicDuctType.valueOf("Pipe").ductTypeValueOf("Iron"));
        this.outputDir = outputDir;
    }

    public TPDirection getOutputDir() {
        return outputDir;
    }

}
