package de.robotricker.transportpipes.ducts;

import org.bukkit.Chunk;

import java.util.ArrayList;
import java.util.List;

import de.robotricker.transportpipes.ducts.types.BasicDuctType;
import de.robotricker.transportpipes.ducts.types.DuctType;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.TPDirection;

public abstract class Duct {

    private BasicDuctType basicDuctType;
    private DuctType ductType;
    private BlockLoc blockLoc;
    private Chunk chunk;

    public Duct(BasicDuctType basicDuctType, DuctType ductType, BlockLoc blockLoc, Chunk chunk){
        this.basicDuctType = basicDuctType;
        this.ductType = ductType;
        this.blockLoc = blockLoc;
        this.chunk = chunk;
    }

    public BasicDuctType getBasicDuctType() {
        return basicDuctType;
    }

    public DuctType getDuctType() {
        return ductType;
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
