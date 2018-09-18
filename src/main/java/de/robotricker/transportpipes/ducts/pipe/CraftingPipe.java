package de.robotricker.transportpipes.ducts.pipe;

import org.bukkit.Chunk;

import de.robotricker.transportpipes.ducts.types.BasicDuctType;
import de.robotricker.transportpipes.utils.BlockLoc;

public class CraftingPipe extends Pipe {

    public CraftingPipe(BlockLoc blockLoc, Chunk chunk) {
        super(blockLoc, chunk, BasicDuctType.valueOf("Pipe").ductTypeValueOf("Crafting"));
    }
}
