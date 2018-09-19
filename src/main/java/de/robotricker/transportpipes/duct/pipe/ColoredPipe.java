package de.robotricker.transportpipes.duct.pipe;

import org.bukkit.Chunk;

import de.robotricker.transportpipes.duct.types.ColoredPipeType;
import de.robotricker.transportpipes.location.BlockLocation;

public class ColoredPipe extends Pipe {

    public ColoredPipe(BlockLocation blockLoc, Chunk chunk, ColoredPipeType pipeType) {
        super(blockLoc, chunk, pipeType);
    }

}
