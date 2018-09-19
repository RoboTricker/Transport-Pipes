package de.robotricker.transportpipes.duct.factory;

import org.bukkit.Chunk;

import de.robotricker.transportpipes.duct.pipe.ColoredPipe;
import de.robotricker.transportpipes.duct.pipe.CraftingPipe;
import de.robotricker.transportpipes.duct.pipe.ExtractionPipe;
import de.robotricker.transportpipes.duct.pipe.GoldenPipe;
import de.robotricker.transportpipes.duct.pipe.IcePipe;
import de.robotricker.transportpipes.duct.pipe.IronPipe;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.duct.pipe.VoidPipe;
import de.robotricker.transportpipes.duct.types.ColoredPipeType;
import de.robotricker.transportpipes.duct.types.DuctType;
import de.robotricker.transportpipes.location.BlockLocation;

public class PipeFactory extends DuctFactory {

    @Override
    public Pipe createDuct(DuctType pipeType, BlockLocation blockLoc, Chunk chunk) {
        if (pipeType instanceof ColoredPipeType) {
            return new ColoredPipe(blockLoc, chunk, (ColoredPipeType) pipeType);
        } else if (pipeType.is("Golden")) {
            return new GoldenPipe(blockLoc, chunk);
        } else if (pipeType.is("Iron")) {
            return new IronPipe(blockLoc, chunk);
        } else if (pipeType.is("Ice")) {
            return new IcePipe(blockLoc, chunk);
        } else if (pipeType.is("Void")) {
            return new VoidPipe(blockLoc, chunk);
        } else if (pipeType.is("Extraction")) {
            return new ExtractionPipe(blockLoc, chunk);
        } else if (pipeType.is("Crafting")) {
            return new CraftingPipe(blockLoc, chunk);
        }
        return null;
    }

}
