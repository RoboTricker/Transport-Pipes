package de.robotricker.transportpipes.ducts.pipe;

import org.bukkit.Chunk;
import org.bukkit.World;

import de.robotricker.transportpipes.ducts.types.pipetype.ColoredPipeType;
import de.robotricker.transportpipes.location.BlockLocation;

public class ColoredPipe extends Pipe {

    public ColoredPipe(DuctService ductService, BlockLocation blockLoc, World world, Chunk chunk, ColoredPipeType pipeType) {
        super(ductService, blockLoc, world, chunk, pipeType);
    }

}
