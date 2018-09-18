package de.robotricker.transportpipes.ducts.pipe;

import org.bukkit.Chunk;

import de.robotricker.transportpipes.ducts.Duct;
import de.robotricker.transportpipes.ducts.types.BasicDuctType;
import de.robotricker.transportpipes.ducts.types.DuctType;
import de.robotricker.transportpipes.ducts.types.PipeType;
import de.robotricker.transportpipes.utils.BlockLoc;

public class Pipe extends Duct {

    public Pipe(BlockLoc blockLoc, Chunk chunk, DuctType pipeType) {
        super(BasicDuctType.valueOf("Pipe"), pipeType, blockLoc, chunk);
    }

    @Override
    public PipeType getDuctType() {
        return (PipeType) super.getDuctType();
    }

}
