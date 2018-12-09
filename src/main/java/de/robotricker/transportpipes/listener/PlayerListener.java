package de.robotricker.transportpipes.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.inject.Inject;

import de.robotricker.transportpipes.ducts.types.BaseDuctType;
import de.robotricker.transportpipes.rendersystems.RenderSystem;

public class PlayerListener implements Listener {

    @Inject
    private DuctService ductService;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        for (BaseDuctType ductBaseType : BaseDuctType.values()) {
            RenderSystem renderSystem = ductService.getRenderSystems(ductBaseType).get(0);
            renderSystem.getCurrentPlayers().add(event.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        for (BaseDuctType ductBaseType : BaseDuctType.values()) {
            RenderSystem renderSystem = ductService.getRenderSystem(e.getPlayer(), ductBaseType);
            renderSystem.getCurrentPlayers().remove(e.getPlayer());
        }
        ductService.getPlayerDucts(e.getPlayer()).clear();
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e){
        //make sure that all ducts that were visible to the player get removed so they will spawn again when the player is nearby
        ductService.getPlayerDucts(e.getPlayer()).clear();
    }

}
