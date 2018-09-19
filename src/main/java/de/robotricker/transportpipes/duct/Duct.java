package de.robotricker.transportpipes.duct;

import org.bukkit.Chunk;

import java.util.ArrayList;
import java.util.List;

import de.robotricker.transportpipes.duct.types.DuctType;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.Direction;

public abstract class Duct {

    private DuctType ductType;
    private BlockLocation blockLoc;
    private Chunk chunk;

    public Duct(DuctType ductType, BlockLocation blockLoc, Chunk chunk){
        this.ductType = ductType;
        this.blockLoc = blockLoc;
        this.chunk = chunk;
    }

    public DuctType getDuctType() {
        return ductType;
    }

    public BlockLocation getBlockLoc() {
        return blockLoc;
    }

    public boolean isInLoadedChunk(){
        return chunk.isLoaded();
    }

    public List<Direction> getDuctConnections(){
        List<Direction> ductConns = new ArrayList<>();
        return ductConns;
    }

    public List<Direction> getContainerBlockConnections(){
        List<Direction> containerBlockConns = new ArrayList<>();
        return containerBlockConns;
    }

    public List<Direction> getAllConnections(){
        List<Direction> allConns = new ArrayList<>();
        allConns.addAll(getDuctConnections());
        allConns.addAll(getContainerBlockConnections());
        return allConns;
    }

}
