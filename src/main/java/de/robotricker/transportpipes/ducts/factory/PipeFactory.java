package de.robotricker.transportpipes.ducts.factory;

import org.bukkit.Chunk;

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
    public Pipe createDuct(DuctType pipeType, BlockLoc blockLoc, Chunk chunk) {
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
