package de.robotricker.transportpipes.rendersystems.pipe.vanilla.model.data;

import de.robotricker.transportpipes.ducts.pipe.Pipe;
import de.robotricker.transportpipes.ducts.types.PipeType;

public class VanillaPipeModelData {

    private PipeType pipeType;

    public VanillaPipeModelData(PipeType pipeType) {
        this.pipeType = pipeType;
    }

    public PipeType getPipeType() {
        return pipeType;
    }

    public static VanillaPipeModelData createModelData(Pipe pipe) {
        if (pipe.getDuctType().is("Iron")) {
            return new VanillaIronPipeModelData(null); //TODO: add outputDir
        } else {
            return new VanillaPipeModelData(pipe.getDuctType());
        }
    }

}
