package de.robotricker.transportpipes.ducts.pipe;

import org.bukkit.Chunk;
import org.bukkit.World;

import de.robotricker.transportpipes.ducts.Duct;
import de.robotricker.transportpipes.ducts.types.DuctType;
import de.robotricker.transportpipes.ducts.types.pipetype.PipeType;
import de.robotricker.transportpipes.location.BlockLocation;

public class Pipe extends Duct {

    public Pipe(DuctService ductService, BlockLocation blockLoc, World world, Chunk chunk, DuctType pipeType) {
        super(ductService, pipeType, blockLoc, world, chunk);
    }

    @Override
    public PipeType getDuctType() {
        return (PipeType) super.getDuctType();
    }

}
