package de.robotricker.transportpipes.ducts.manager;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.ducts.Duct;
import de.robotricker.transportpipes.ducts.DuctRegister;
import de.robotricker.transportpipes.ducts.types.BaseDuctType;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.TPDirection;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.protocol.ProtocolService;
import de.robotricker.transportpipes.rendersystems.RenderSystem;
import de.robotricker.transportpipes.utils.Constants;

public class GlobalDuctManager {

    protected TransportPipes transportPipes;
    protected ProtocolService protocolService;
    protected DuctRegister ductRegister;

    /**
     * ThreadSafe
     **/
    private Map<World, Map<BlockLocation, Duct>> ducts;
    /**
     * ThreadSafe
     **/
    private Map<Player, Set<Duct>> playerDucts;

    @Inject
    public GlobalDuctManager(TransportPipes transportPipes, ProtocolService protocolService, DuctRegister ductRegister) {
        this.transportPipes = transportPipes;
        this.protocolService = protocolService;
        this.ductRegister = ductRegister;
        this.ducts = Collections.synchronizedMap(new HashMap<>());
        this.playerDucts = Collections.synchronizedMap(new HashMap<>());
    }

    public Map<World, Map<BlockLocation, Duct>> getDucts() {
        return ducts;
    }

    public Map<BlockLocation, Duct> getDucts(World world) {
        return ducts.computeIfAbsent(world, v -> Collections.synchronizedMap(new TreeMap<>()));
    }

    public Duct getDuctAtLoc(World world, BlockLocation blockLoc) {
        Map<BlockLocation, Duct> ductMap = getDucts(world);
        return ductMap.get(blockLoc);
    }

    public Duct getDuctAtLoc(Location location) {
        return getDuctAtLoc(location.getWorld(), new BlockLocation(location));
    }

    public Set<Duct> getPlayerDucts(Player player) {
        return playerDucts.computeIfAbsent(player, p -> Collections.synchronizedSet(new HashSet<>()));
    }

    public RenderSystem getPlayerRenderSystem(Player player, BaseDuctType<? extends Duct> baseDuctType) {
        return baseDuctType.getRenderSystems().stream().filter(rs -> rs.getCurrentPlayers().contains(player)).findAny().orElse(null);
    }

    public void createDuct(Duct duct) {
        getDucts(duct.getWorld()).put(duct.getBlockLoc(), duct);
        updateDuctConnections(duct);
        updateContainerConnections(duct);
        for (RenderSystem renderSystem : duct.getDuctType().getBaseDuctType().getRenderSystems()) {
            renderSystem.createDuctASD(duct, duct.getAllConnections());
            synchronized (renderSystem.getCurrentPlayers()) {
                for (Player p : renderSystem.getCurrentPlayers()) {
                    if (duct.getBlockLoc().toLocation(p.getWorld()).distance(p.getLocation()) <= Constants.DEFAULT_RENDER_DISTANCE) {
                        protocolService.sendASD(p, duct.getBlockLoc(), renderSystem.getASDForDuct(duct));
                        getPlayerDucts(p).add(duct);
                    }
                }
            }
        }
        for (TPDirection ductConn : duct.getDuctConnections().keySet()) {
            updateDuct(duct.getDuctConnections().get(ductConn));
        }
    }

    public void updateDuct(Duct duct) {
        updateDuctConnections(duct);
        updateContainerConnections(duct);
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

    public void destroyDuct(Duct duct, Player destroyer) {
        getDucts(duct.getWorld()).remove(duct.getBlockLoc());
        for (RenderSystem renderSystem : duct.getDuctType().getBaseDuctType().getRenderSystems()) {
            synchronized (renderSystem.getCurrentPlayers()) {
                for (Player p : renderSystem.getCurrentPlayers()) {
                    if (getPlayerDucts(p).remove(duct)) {
                        protocolService.removeASD(p, renderSystem.getASDForDuct(duct));
                    }
                }
            }
            renderSystem.destroyDuctASD(duct);
        }
        for (TPDirection ductConn : duct.getDuctConnections().keySet()) {
            updateDuct(duct.getDuctConnections().get(ductConn));
        }
        //drop items
        List<ItemStack> dropItems = duct.destroyed(transportPipes, duct.getDuctType().getBaseDuctType().getDuctManager(), destroyer);
        transportPipes.runTaskSync(() -> {
            for (ItemStack is : dropItems) {
                duct.getWorld().dropItem(duct.getBlockLoc().toLocation(duct.getWorld()), is);
            }
        });
    }

    /**
     * recalculates the duct connections of this duct and saves them.
     * Also notifies the duct about this change
     */
    public void updateDuctConnections(Duct duct) {
        duct.getDuctConnections().clear();
        for (TPDirection tpDir : TPDirection.values()) {
            Duct neighborDuct = getDuctAtLoc(duct.getWorld(), duct.getBlockLoc().getNeighbor(tpDir));
            if (neighborDuct != null && duct.getDuctType().connectsTo(neighborDuct.getDuctType())) {
                duct.getDuctConnections().put(tpDir, neighborDuct);
            }
        }
        duct.notifyConnectionChange();
    }

    /**
     * recalculates the container connections of this duct and saves them.
     * Also notifies the duct about this change
     */
    public void updateContainerConnections(Duct duct) {
        duct.notifyConnectionChange();
    }

    public void tick() {
        for (BaseDuctType<? extends Duct> baseDuctType : ductRegister.baseDuctTypes()) {
            Map<World, Map<BlockLocation, Duct>> ducts = new HashMap<>();
            for (World world : this.ducts.keySet()) {
                Map<BlockLocation, Duct> ductMap = new TreeMap<>();
                this.ducts.get(world).values().stream().filter(duct -> duct.getDuctType().getBaseDuctType().equals(baseDuctType)).forEach(duct -> ductMap.put(duct.getBlockLoc(), duct));
                ducts.put(world, ductMap);
            }
            baseDuctType.getDuctManager().tick(ducts);
        }
    }

}
