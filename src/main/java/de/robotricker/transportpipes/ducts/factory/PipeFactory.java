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
import de.robotricker.transportpipes.ducts.types.pipetype.ColoredPipeType;
import de.robotricker.transportpipes.ducts.types.DuctType;
import de.robotricker.transportpipes.location.BlockLocation;

public class PipeFactory extends DuctFactory {

    @Override
    public Pipe createDuct(DuctService ductService, DuctType pipeType, BlockLocation blockLoc, World world, Chunk chunk) {
        if (pipeType instanceof ColoredPipeType) {
            return new ColoredPipe(ductService, blockLoc, world, chunk, (ColoredPipeType) pipeType);
        } else if (pipeType.is("Golden")) {
            return new GoldenPipe(ductService, blockLoc, world, chunk);
        } else if (pipeType.is("Iron")) {
            return new IronPipe(ductService, blockLoc, world, chunk);
        } else if (pipeType.is("Ice")) {
            return new IcePipe(ductService, blockLoc, world, chunk);
        } else if (pipeType.is("Void")) {
            return new VoidPipe(ductService, blockLoc, world, chunk);
        } else if (pipeType.is("Extraction")) {
            return new ExtractionPipe(ductService, blockLoc, world, chunk);
        } else if (pipeType.is("Crafting")) {
            return new CraftingPipe(ductService, blockLoc, world, chunk);
        }
        return null;
    }

}
