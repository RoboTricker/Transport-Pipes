package de.robotricker.transportpipes.ducts;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.container.TPContainer;
import de.robotricker.transportpipes.ducts.types.DuctType;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.TPDirection;

public abstract class Duct {

    private DuctType ductType;
    private BlockLoc blockLoc;
    private World world;
    private Chunk chunk;

    private Map<TPDirection, Duct> connectedDucts;
    private Map<TPDirection, TPContainer> connectedContainers;

    public Duct(DuctType ductType, BlockLoc blockLoc, World world, Chunk chunk){
        this.ductType = ductType;
        this.blockLoc = blockLoc;
        this.world = world;
        this.chunk = chunk;
        this.connectedDucts = Collections.synchronizedMap(new HashMap<>());
        this.connectedContainers = Collections.synchronizedMap(new HashMap<>());
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

    public void updateDuctConnections(){
        connectedDucts.clear();
        for (TPDirection tpDir : TPDirection.values()) {
            Duct neighborDuct = TransportPipes.instance.getDuctManager().getDuctAtLoc(world, blockLoc.getNeighbor(tpDir));
            if (neighborDuct != null && getDuctType().connectsTo(neighborDuct.getDuctType())) {
                connectedDucts.put(tpDir, neighborDuct);
            }
        }
    }

    public void updateContainerConnections(){

    }

    public Map<TPDirection, Duct> getDuctConnections() {
        return connectedDucts;
    }

    public Map<TPDirection, TPContainer> getContainerConnections() {
        return connectedContainers;
    }

    public Set<TPDirection> getAllConnections(){
        Set<TPDirection> connections = new HashSet<>();
        connections.addAll(getDuctConnections().keySet());
        connections.addAll(getContainerConnections().keySet());
        return connections;
    }

}
