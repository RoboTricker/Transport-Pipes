package de.robotricker.transportpipes.ducts.pipe;

import org.bukkit.Chunk;

import de.robotricker.transportpipes.ducts.Duct;
import de.robotricker.transportpipes.utils.BlockLoc;

public class Pipe extends Duct {

    private PipeType pipeType;

    public Pipe(BlockLoc blockLoc, Chunk chunk, PipeType pipeType) {
        super(blockLoc, chunk);
        this.pipeType = pipeType;
    }

    public PipeType getPipeType() {
        return pipeType;
    }
}
