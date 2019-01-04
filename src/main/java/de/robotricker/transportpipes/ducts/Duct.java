package de.robotricker.transportpipes.ducts;

import com.comphenix.packetwrapper.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;

import org.bukkit.Chunk;
import org.bukkit.GameMode;
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

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.container.TPContainer;
import de.robotricker.transportpipes.ducts.manager.DuctManager;
import de.robotricker.transportpipes.ducts.manager.GlobalDuctManager;
import de.robotricker.transportpipes.ducts.types.DuctType;
import de.robotricker.transportpipes.inventory.DuctSettingsInventory;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.TPDirection;

public abstract class Duct {

    protected GlobalDuctManager globalDuctManager;

    private DuctType ductType;
    private BlockLocation blockLoc;
    private World world;
    private Chunk chunk;

    private Map<TPDirection, Duct> connectedDucts;
    private Map<TPDirection, TPContainer> connectedContainers;

    private DuctSettingsInventory settingsInv;

    public Duct(DuctType ductType, BlockLocation blockLoc, World world, Chunk chunk, DuctSettingsInventory settingsInv, GlobalDuctManager globalDuctManager) {
        this.ductType = ductType;
        this.blockLoc = blockLoc;
        this.world = world;
        this.chunk = chunk;
        this.connectedDucts = Collections.synchronizedMap(new HashMap<>());
        this.connectedContainers = Collections.synchronizedMap(new HashMap<>());
        this.settingsInv = settingsInv;
        this.globalDuctManager = globalDuctManager;
        if (settingsInv != null) {
            settingsInv.setDuct(this);
        }
    }

    public void notifyClick(Player p, TPDirection face, boolean shift) {
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

    public void notifyConnectionChange() {
        if (settingsInv != null) {
            settingsInv.populate();
        }
    }

    public void tick(TransportPipes transportPipes, DuctManager ductManager, GlobalDuctManager globalDuctManager) {

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

    public int[] getBreakParticleData() {
        return null;
    }

    /**
     * just for the purpose of dropping inside items or other baseDuctType specific stuff
     */
    public List<ItemStack> destroyed(TransportPipes transportPipes, DuctManager ductManager, Player destroyer) {
        List<ItemStack> dropItems = new ArrayList<>();
        if (destroyer == null || destroyer.getGameMode() != GameMode.CREATIVE) {
            dropItems.add(getDuctType().getBaseDuctType().getItemManager().getClonedItem(getDuctType()));
        }

        //break particles
        if(getBreakParticleData() != null) {
            transportPipes.runTaskSync(() -> {
                if (destroyer != null) {
                    // show break particles
                    WrapperPlayServerWorldParticles wrapper = new WrapperPlayServerWorldParticles();
                    wrapper.setParticleType(EnumWrappers.Particle.ITEM_CRACK);
                    wrapper.setNumberOfParticles(30);
                    wrapper.setLongDistance(false);
                    wrapper.setX(getBlockLoc().getX() + 0.5f);
                    wrapper.setY(getBlockLoc().getY() + 0.5f);
                    wrapper.setZ(getBlockLoc().getZ() + 0.5f);
                    wrapper.setOffsetX(0.25f);
                    wrapper.setOffsetY(0.25f);
                    wrapper.setOffsetZ(0.25f);
                    wrapper.setParticleData(0.05f);
                    wrapper.setData(getBreakParticleData());
                    for (Player worldPl : getWorld().getPlayers()) {
                        wrapper.sendPacket(worldPl);
                    }
                }
            });
        }

        return dropItems;
    }

}
