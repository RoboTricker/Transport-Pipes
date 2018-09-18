package de.robotricker.transportpipes.ducts.pipe;

import org.bukkit.Chunk;

import de.robotricker.transportpipes.ducts.types.BasicDuctType;
import de.robotricker.transportpipes.utils.BlockLoc;

public class IcePipe extends Pipe {

    public IcePipe(BlockLoc blockLoc, Chunk chunk) {
        super(blockLoc, chunk, BasicDuctType.valueOf("Pipe").ductTypeValueOf("Ice"));
    }
}
