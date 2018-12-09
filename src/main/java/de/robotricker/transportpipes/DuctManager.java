package de.robotricker.transportpipes;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;

import de.robotricker.transportpipes.ducts.Duct;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.TPDirection;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.protocol.ProtocolService;
import de.robotricker.transportpipes.rendersystems.RenderSystem;
import de.robotricker.transportpipes.utils.Constants;

public abstract class DuctManager<T extends Duct> {

    protected ProtocolService protocolService;
    protected DuctRegister ductRegister;

    /**
     * ThreadSafe
     **/
    private Map<World, Map<BlockLocation, T>> ducts;
    /**
     * ThreadSafe
     **/
    private Map<Player, Set<T>> playerDucts;

    @Inject
    public DuctManager(ProtocolService protocolService, DuctRegister ductRegister) {
        this.protocolService = protocolService;
        this.ductRegister = ductRegister;
        this.ducts = Collections.synchronizedMap(new HashMap<>());
        this.playerDucts = Collections.synchronizedMap(new HashMap<>());
    }

    public Map<World, Map<BlockLocation, T>> getDucts() {
        return ducts;
    }

    public Map<BlockLocation, T> getDucts(World world) {
        return ducts.computeIfAbsent(world, v -> Collections.synchronizedMap(new TreeMap<>()));
    }

    public T getDuctAtLoc(World world, BlockLocation blockLoc) {
        Map<BlockLocation, T> ductMap = getDucts(world);
        return ductMap.get(blockLoc);
    }

    public T getDuctAtLoc(Location location) {
        return getDuctAtLoc(location.getWorld(), new BlockLocation(location));
    }

    public Set<T> getPlayerDucts(Player player) {
        return playerDucts.computeIfAbsent(player, p -> Collections.synchronizedSet(new HashSet<>()));
    }

    public void createDuct(T duct) {
        getDucts(duct.getWorld()).put(duct.getBlockLoc(), duct);
        updateDuctConnections(duct);
        duct.updateContainerConnections();
        for (RenderSystem renderSystem : duct.getDuctType().getBaseDuctType().getRenderSystems()) {
            renderSystem.createDuctASD(duct, duct.getAllConnections());
            synchronized (renderSystem.getCurrentPlayers()) {
                for (Player p : renderSystem.getCurrentPlayers()) {
                    if(duct.getBlockLoc().toLocation(p.getWorld()).distance(p.getLocation()) <= Constants.DEFAULT_RENDER_DISTANCE) {
                        protocolService.sendASD(p, duct.getBlockLoc(), renderSystem.getASDForDuct(duct));
                        getPlayerDucts(p).add(duct);
                    }
                }
            }
        }
        for (TPDirection ductConn : duct.getDuctConnections().keySet()) {
            updateDuct((T) duct.getDuctConnections().get(ductConn));
        }
    }

    public void updateDuct(T duct) {
        updateDuctConnections(duct);
        duct.updateContainerConnections();
        for (RenderSystem renderSystem : duct.getDuctType().getBaseDuctType().getRenderSystems()) {
            List<ArmorStandData> removeASD = new ArrayList<>();
            List<ArmorStandData> addASD = new ArrayList<>();
            renderSystem.updateDuctASD(duct, duct.getAllConnections(), removeASD, addASD);
            synchronized (renderSystem.getCurrentPlayers()) {
                for (Player p : renderSystem.getCurrentPlayers()) {
                    if (getPlayerDucts(p).contains(duct)) {
                        protocolService.removeASD(p, removeASD);
                        protocolService.sendASD(p, duct.getBlockLoc(), addASD);
                    }
                }
            }
        }
    }

    public void destroyDuct(T duct) {
        getDucts(duct.getWorld()).remove(duct.getBlockLoc());
        for (RenderSystem renderSystem : duct.getDuctType().getBaseDuctType().getRenderSystems()) {
            synchronized (renderSystem.getCurrentPlayers()) {
                for (Player p : renderSystem.getCurrentPlayers()) {
                    if(getPlayerDucts(p).remove(duct)) {
                        protocolService.removeASD(p, renderSystem.getASDForDuct(duct));
                    }
                }
            }
            renderSystem.destroyDuctASD(duct);
        }
        for (TPDirection ductConn : duct.getDuctConnections().keySet()) {
            updateDuct((T) duct.getDuctConnections().get(ductConn));
        }
    }

    public void updateDuctConnections(T duct) {
        duct.getDuctConnections().clear();
        for (TPDirection tpDir : TPDirection.values()) {
            Duct neighborDuct = getDuctAtLoc(duct.getWorld(), duct.getBlockLoc().getNeighbor(tpDir));
            if (neighborDuct != null && duct.getDuctType().connectsTo(neighborDuct.getDuctType())) {
                duct.getDuctConnections().put(tpDir, neighborDuct);
            }
        }
    }

    public abstract void registerDuctTypes();

    public abstract void tick();

}
