package de.robotricker.transportpipes.ducts.pipe;

import org.bukkit.Chunk;
import org.bukkit.World;

import de.robotricker.transportpipes.ducts.Duct;
import de.robotricker.transportpipes.ducts.types.DuctType;
import de.robotricker.transportpipes.ducts.types.PipeType;
import de.robotricker.transportpipes.utils.BlockLoc;

public class Pipe extends Duct {

    public Pipe(BlockLoc blockLoc, World world, Chunk chunk, DuctType pipeType) {
        super(pipeType, blockLoc, world, chunk);
    }

    @Override
    public PipeType getDuctType() {
        return (PipeType) super.getDuctType();
    }

}
