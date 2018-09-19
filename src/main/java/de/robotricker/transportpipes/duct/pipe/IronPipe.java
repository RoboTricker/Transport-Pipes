package de.robotricker.transportpipes.duct.pipe;

import org.bukkit.Chunk;

import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.location.BlockLocation;

public class IronPipe extends Pipe {

    public IronPipe(BlockLocation blockLoc, Chunk chunk) {
        super(blockLoc, chunk, BaseDuctType.valueOf("Pipe").ductTypeValueOf("Iron"));
    }
}
