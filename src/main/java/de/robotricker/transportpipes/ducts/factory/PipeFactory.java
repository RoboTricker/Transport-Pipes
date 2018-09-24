package de.robotricker.transportpipes.ducts.factory;

import org.bukkit.Chunk;
import org.bukkit.World;

import de.robotricker.transportpipes.ducts.pipe.ColoredPipe;
import de.robotricker.transportpipes.ducts.pipe.CraftingPipe;
import de.robotricker.transportpipes.ducts.pipe.ExtractionPipe;
import de.robotricker.transportpipes.ducts.pipe.GoldenPipe;
import de.robotricker.transportpipes.ducts.pipe.IcePipe;
import de.robotricker.transportpipes.ducts.pipe.IronPipe;
import de.robotricker.transportpipes.ducts.pipe.Pipe;
import de.robotricker.transportpipes.ducts.pipe.VoidPipe;
import de.robotricker.transportpipes.ducts.types.ColoredPipeType;
import de.robotricker.transportpipes.ducts.types.DuctType;
import de.robotricker.transportpipes.utils.BlockLoc;

public class PipeFactory extends DuctFactory {

    @Override
    public Pipe createDuct(DuctType pipeType, BlockLoc blockLoc, World world, Chunk chunk) {
        if (pipeType instanceof ColoredPipeType) {
            return new ColoredPipe(blockLoc, world, chunk, (ColoredPipeType) pipeType);
        } else if (pipeType.is("Golden")) {
            return new GoldenPipe(blockLoc, world, chunk);
        } else if (pipeType.is("Iron")) {
            return new IronPipe(blockLoc, world, chunk);
        } else if (pipeType.is("Ice")) {
            return new IcePipe(blockLoc, world, chunk);
        } else if (pipeType.is("Void")) {
            return new VoidPipe(blockLoc, world, chunk);
        } else if (pipeType.is("Extraction")) {
            return new ExtractionPipe(blockLoc, world, chunk);
        } else if (pipeType.is("Crafting")) {
            return new CraftingPipe(blockLoc, world, chunk);
        }
        return null;
    }

}
