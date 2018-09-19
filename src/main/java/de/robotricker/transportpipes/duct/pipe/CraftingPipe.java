package de.robotricker.transportpipes.duct.pipe;

import org.bukkit.Chunk;

import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.location.BlockLocation;

public class CraftingPipe extends Pipe {

    public CraftingPipe(BlockLocation blockLoc, Chunk chunk) {
        super(blockLoc, chunk, BaseDuctType.valueOf("Pipe").ductTypeValueOf("Crafting"));
    }
}
