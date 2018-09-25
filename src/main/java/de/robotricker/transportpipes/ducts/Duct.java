package de.robotricker.transportpipes.ducts;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.ducts.types.DuctType;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.TPDirection;

public abstract class Duct {

    private DuctType ductType;
    private BlockLoc blockLoc;
    private World world;
    private Chunk chunk;

    public Duct(DuctType ductType, BlockLoc blockLoc, World world, Chunk chunk){
        this.ductType = ductType;
        this.blockLoc = blockLoc;
        this.world = world;
        this.chunk = chunk;
    }

    public DuctType getDuctType() {
        return ductType;
    }

    public BlockLoc getBlockLoc() {
        return blockLoc;
    }

    public World getWorld() {
        return world;
    }

    public boolean isInLoadedChunk(){
        return chunk.isLoaded();
    }

    public List<TPDirection> getDuctConnections(){
        List<TPDirection> ductConns = new ArrayList<>();
        for (TPDirection tpDir : TPDirection.values()) {
            Duct neighborDuct = TransportPipes.instance.getDuctManager().getDuctAtLoc(world, blockLoc.getNeighbor(tpDir));
            if (neighborDuct != null && getDuctType().connectsTo(neighborDuct.getDuctType())) {
                ductConns.add(tpDir);
            }
        }
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
