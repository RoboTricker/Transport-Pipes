package de.robotricker.transportpipes.ducts;

import org.bukkit.Chunk;

import java.util.ArrayList;
import java.util.List;

import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.TPDirection;

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

    public List<TPDirection> getDuctConnections(){
        List<TPDirection> ductConns = new ArrayList<>();
        return ductConns;
    }

    public List<TPDirection> getContainerBlockConnections(){
        List<TPDirection> containerBlockConns = new ArrayList<>();
        return containerBlockConns;
    }

    public List<TPDirection> getAllConnections(){
        List<TPDirection> allConns = new ArrayList<>();
        allConns.addAll(getDuctConnections());
        allConns.addAll(getContainerBlockConnections());
        return allConns;
    }

}
