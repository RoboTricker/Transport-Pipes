package de.robotricker.transportpipes.ducts.pipe;

import org.bukkit.Chunk;

import de.robotricker.transportpipes.ducts.types.ColoredPipeType;
import de.robotricker.transportpipes.utils.BlockLoc;

public class ColoredPipe extends Pipe {

    public ColoredPipe(BlockLoc blockLoc, Chunk chunk, ColoredPipeType pipeType) {
        super(blockLoc, chunk, pipeType);
    }

}
