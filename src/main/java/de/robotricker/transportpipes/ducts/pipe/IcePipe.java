package de.robotricker.transportpipes.ducts.pipe;

import org.bukkit.Chunk;
import org.bukkit.World;

import de.robotricker.transportpipes.ducts.types.BasicDuctType;
import de.robotricker.transportpipes.utils.BlockLoc;

public class IcePipe extends Pipe {

    public IcePipe(BlockLoc blockLoc, World world, Chunk chunk) {
        super(blockLoc, world, chunk, BasicDuctType.valueOf("Pipe").ductTypeValueOf("Ice"));
    }
}
