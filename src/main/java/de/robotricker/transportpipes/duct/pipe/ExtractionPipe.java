package de.robotricker.transportpipes.duct.pipe;

import org.bukkit.Chunk;

import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.location.BlockLocation;

public class ExtractionPipe extends Pipe {

    public ExtractionPipe(BlockLocation blockLoc, Chunk chunk) {
        super(blockLoc, chunk, BaseDuctType.valueOf("Pipe").ductTypeValueOf("Extraction"));
    }
}
