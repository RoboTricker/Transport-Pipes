package de.robotricker.transportpipes.listener;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import javax.inject.Inject;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.container.TPContainer;
import de.robotricker.transportpipes.ducts.Duct;
import de.robotricker.transportpipes.ducts.DuctRegister;
import de.robotricker.transportpipes.ducts.manager.DuctManager;
import de.robotricker.transportpipes.ducts.manager.GlobalDuctManager;
import de.robotricker.transportpipes.ducts.manager.PipeManager;
import de.robotricker.transportpipes.ducts.pipe.Pipe;
import de.robotricker.transportpipes.ducts.pipe.items.PipeItem;
import de.robotricker.transportpipes.ducts.types.BaseDuctType;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.TPDirection;
import de.robotricker.transportpipes.rendersystems.RenderSystem;

public class PlayerListener implements Listener {

    @Inject
    private GlobalDuctManager globalDuctManager;

    @Inject
    private DuctRegister ductRegister;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        for (BaseDuctType<? extends Duct> ductBaseType : ductRegister.baseDuctTypes()) {
            RenderSystem renderSystem = ductBaseType.getRenderSystems().stream().findFirst().get();
            renderSystem.getCurrentPlayers().add(event.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        for (BaseDuctType<? extends Duct> ductBaseType : ductRegister.baseDuctTypes()) {
            RenderSystem renderSystem = globalDuctManager.getPlayerRenderSystem(e.getPlayer(), ductBaseType);
            renderSystem.getCurrentPlayers().remove(e.getPlayer());
        }
        globalDuctManager.getPlayerDucts(e.getPlayer()).clear();
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        //make sure that all ducts that were visible to the player get removed so they will spawn again when the player is nearby
        globalDuctManager.getPlayerDucts(e.getPlayer()).clear();
    }

    // TODO: REMOVE DEBUG
    @EventHandler
    public void onDebug(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getItem() != null && e.getItem().getType() == Material.APPLE) {
                Pipe pipe = (Pipe) globalDuctManager.getDuctAtLoc(e.getPlayer().getLocation());
                if (pipe != null) {
                    for (int i = 0; i < (e.getPlayer().isSneaking() ? 10 : 1); i++) {
                        PipeItem item = new PipeItem(new ItemStack(Material.APPLE), e.getPlayer().getWorld(), new BlockLocation(e.getPlayer().getLocation()), TPDirection.NORTH);
                        ((PipeManager) pipe.getDuctType().getBaseDuctType().getDuctManager()).createPipeItem(item);
                    }
                    System.out.println("placed item(s)");
                }
            }
        }
    }

}
