package de.robotricker.transportpipes.ducts;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.robotricker.transportpipes.container.TPContainer;
import de.robotricker.transportpipes.ducts.types.DuctType;
import de.robotricker.transportpipes.inventory.DuctSettingsInventory;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.TPDirection;

public abstract class Duct {

    private DuctType ductType;
    private BlockLocation blockLoc;
    private World world;
    private Chunk chunk;

    private Map<TPDirection, Duct> connectedDucts;
    private Map<TPDirection, TPContainer> connectedContainers;

    private DuctSettingsInventory settingsInv;

    public Duct(DuctType ductType, BlockLocation blockLoc, World world, Chunk chunk) {
        this.ductType = ductType;
        this.blockLoc = blockLoc;
        this.world = world;
        this.chunk = chunk;
        this.connectedDucts = Collections.synchronizedMap(new HashMap<>());
        this.connectedContainers = Collections.synchronizedMap(new HashMap<>());
    }

    public void notifyClick(Player p, TPDirection face) {
        if (settingsInv != null)
            settingsInv.openInv(p);
    }

    public DuctType getDuctType() {
        return ductType;
    }

    public BlockLocation getBlockLoc() {
        return blockLoc;
    }

    public World getWorld() {
        return world;
    }

    public boolean isInLoadedChunk() {
        return chunk.isLoaded();
    }

    public void tick() {

    }

    public void updateContainerConnections() {

    }

    public Map<TPDirection, Duct> getDuctConnections() {
        return connectedDucts;
    }

    public Map<TPDirection, TPContainer> getContainerConnections() {
        return connectedContainers;
    }

    public Set<TPDirection> getAllConnections() {
        Set<TPDirection> connections = new HashSet<>();
        connections.addAll(getDuctConnections().keySet());
        connections.addAll(getContainerConnections().keySet());
        return connections;
    }

}
