package de.robotricker.transportpipes.ducts;

import org.bukkit.Chunk;

import de.robotricker.transportpipes.utils.BlockLoc;

public abstract class Duct {

    private BlockLoc blockLoc;
    private Chunk chunk;

    public Duct(BlockLoc blockLoc, Chunk chunk){
        this.blockLoc = blockLoc;
        this.chunk = chunk;
    }

    public BlockLoc getBlockLoc() {
        return blockLoc;
    }

    public boolean isInLoadedChunk(){
        return chunk.isLoaded();
    }

}
