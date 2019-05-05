package de.robotricker.transportpipes.utils;

import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCProtectionRegistrationPostEvent;

import org.bukkit.Location;

import java.util.Map;

import javax.inject.Inject;

import de.robotricker.transportpipes.config.LangConf;
import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctRegister;
import de.robotricker.transportpipes.duct.manager.DuctManager;
import de.robotricker.transportpipes.duct.manager.GlobalDuctManager;
import de.robotricker.transportpipes.duct.manager.PipeManager;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.TPDirection;

public class LWCUtils extends JavaModule {

    @Inject
    private DuctRegister ductRegister;
    @Inject
    private GlobalDuctManager globalDuctManager;

    @Override
    public void onPostRegistration(LWCProtectionRegistrationPostEvent e) {
        if (e.getProtection().getType() == Protection.Type.PUBLIC) {
            return;
        }
        boolean destroyedAtLeastOneDuct = false;

        PipeManager pipeManager = ((PipeManager) (DuctManager<?>) ductRegister.baseDuctTypeOf("pipe").getDuctManager());

        Location protectionLoc = e.getProtection().getBlock().getLocation();
        BlockLocation protectionBlockLoc = new BlockLocation(protectionLoc);
        Map<BlockLocation, TransportPipesContainer> containerMap = pipeManager.getContainers(e.getProtection().getBukkitWorld());
        if (containerMap != null && containerMap.containsKey(protectionBlockLoc)) {
            Map<BlockLocation, Duct> ductMap = globalDuctManager.getDucts(e.getProtection().getBukkitWorld());
            if (ductMap != null) {
                for (TPDirection dir : TPDirection.values()) {
                    if (ductMap.containsKey(protectionBlockLoc.getNeighbor(dir))) {
                        Duct duct = ductMap.get(protectionBlockLoc.getNeighbor(dir));
                        if (duct instanceof Pipe) {
                            globalDuctManager.unregisterDuct(duct);
                            globalDuctManager.unregisterDuctInRenderSystem(duct, true);
                            globalDuctManager.updateNeighborDuctsConnections(duct);
                            globalDuctManager.updateNeighborDuctsInRenderSystems(duct, true);
                            globalDuctManager.playDuctDestroyActions(duct, null);
                            destroyedAtLeastOneDuct = true;
                        }
                    }
                }
            }
        }

        if (destroyedAtLeastOneDuct) {
            LangConf.Key.PROTECTED_BLOCK.sendMessage(e.getPlayer());
        }

    }
}
