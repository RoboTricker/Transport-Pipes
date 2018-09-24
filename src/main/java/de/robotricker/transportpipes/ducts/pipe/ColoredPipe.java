package de.robotricker.transportpipes.ducts.pipe;

import org.bukkit.Chunk;
import org.bukkit.World;

import de.robotricker.transportpipes.ducts.types.ColoredPipeType;
import de.robotricker.transportpipes.utils.BlockLoc;

public class ColoredPipe extends Pipe {

    public ColoredPipe(BlockLoc blockLoc, World world, Chunk chunk, ColoredPipeType pipeType) {
        super(blockLoc, world, chunk, pipeType);
    }

}
