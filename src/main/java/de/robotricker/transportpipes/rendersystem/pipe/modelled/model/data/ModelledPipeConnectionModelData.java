package de.robotricker.transportpipes.rendersystem.pipe.modelled.model.data;

import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.duct.types.PipeType;
import de.robotricker.transportpipes.location.Direction;

public class ModelledPipeConnectionModelData {

    private PipeType pipeType;
    private Direction connectionDir;

    public ModelledPipeConnectionModelData(PipeType pipeType, Direction connectionDir) {
        this.pipeType = pipeType;
        this.connectionDir = connectionDir;
    }

    public Direction getConnectionDir() {
        return connectionDir;
    }

    public PipeType getPipeType() {
        return pipeType;
    }

    public static ModelledPipeConnectionModelData createConnectionModelData(Pipe pipe, Direction connectionDir) {
        if (pipe.getDuctType().is("Iron")) {
            return new ModelledIronPipeConnectionModelData(connectionDir, false);
        } else if (pipe.getDuctType().is("Extraction")) {
            return new ModelledExtractionPipeConnectionModelData(connectionDir, false);
        } else {
            return new ModelledPipeConnectionModelData(pipe.getDuctType(), connectionDir);
        }
    }

}
