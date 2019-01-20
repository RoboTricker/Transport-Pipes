package de.robotricker.transportpipes.rendersystems.pipe.modelled.model.data;

import de.robotricker.transportpipes.duct.pipe.ExtractionPipe;
import de.robotricker.transportpipes.duct.pipe.IronPipe;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.duct.types.pipetype.PipeType;
import de.robotricker.transportpipes.location.TPDirection;

public class ModelledPipeConnectionModelData {

    private PipeType pipeType;
    private TPDirection connectionDir;

    public ModelledPipeConnectionModelData(PipeType pipeType, TPDirection connectionDir) {
        this.pipeType = pipeType;
        this.connectionDir = connectionDir;
    }

    public TPDirection getConnectionDir() {
        return connectionDir;
    }

    public PipeType getPipeType() {
        return pipeType;
    }

    public static ModelledPipeConnectionModelData createConnectionModelData(Pipe pipe, TPDirection connectionDir) {
        if (pipe.getDuctType().is("Iron")) {
            return new ModelledIronPipeConnectionModelData(pipe.getDuctType().getBaseDuctType(), connectionDir, ((IronPipe) pipe).getCurrentOutputDirection() == connectionDir);
        } else if (pipe.getDuctType().is("Extraction")) {
            return new ModelledExtractionPipeConnectionModelData(pipe.getDuctType().getBaseDuctType(), connectionDir, ((ExtractionPipe) pipe).getExtractDirection() == connectionDir);
        } else {
            return new ModelledPipeConnectionModelData(pipe.getDuctType(), connectionDir);
        }
    }

}
