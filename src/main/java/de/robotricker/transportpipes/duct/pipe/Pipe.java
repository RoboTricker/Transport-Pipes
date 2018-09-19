package de.robotricker.transportpipes.duct.pipe;

import org.bukkit.Chunk;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.types.DuctType;
import de.robotricker.transportpipes.duct.types.PipeType;
import de.robotricker.transportpipes.location.BlockLocation;

public class Pipe extends Duct {

    public Pipe(BlockLocation blockLoc, Chunk chunk, DuctType pipeType) {
        super(pipeType, blockLoc, chunk);
    }

    @Override
    public PipeType getDuctType() {
        return (PipeType) super.getDuctType();
    }

}
