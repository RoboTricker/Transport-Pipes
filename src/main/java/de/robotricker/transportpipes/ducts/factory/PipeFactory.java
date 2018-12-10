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
import de.robotricker.transportpipes.ducts.types.DuctType;
import de.robotricker.transportpipes.ducts.types.pipetype.ColoredPipeType;
import de.robotricker.transportpipes.location.BlockLocation;

public class PipeFactory extends DuctFactory<Pipe> {

    @Override
    public Pipe createDuct(DuctType ductType, BlockLocation blockLoc, World world, Chunk chunk) {
        if (ductType instanceof ColoredPipeType) {
            return new ColoredPipe(ductType, blockLoc, world, chunk);
        } else if (ductType.is("Golden")) {
            return new GoldenPipe(ductType, blockLoc, world, chunk);
        } else if (ductType.is("Iron")) {
            return new IronPipe(ductType, blockLoc, world, chunk);
        } else if (ductType.is("Ice")) {
            return new IcePipe(ductType, blockLoc, world, chunk);
        } else if (ductType.is("Void")) {
            return new VoidPipe(ductType, blockLoc, world, chunk);
        } else if (ductType.is("Extraction")) {
            return new ExtractionPipe(ductType, blockLoc, world, chunk);
        } else if (ductType.is("Crafting")) {
            return new CraftingPipe(ductType, blockLoc, world, chunk);
        }
        return null;
    }

}
