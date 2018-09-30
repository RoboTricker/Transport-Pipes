package de.robotricker.transportpipes.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.inject.Inject;

import de.robotricker.transportpipes.DuctService;
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
    public void onQuit(PlayerQuitEvent event) {
        handleQuit(event.getPlayer());
    }
    @EventHandler
    public void onKick(PlayerKickEvent event) {
        // TODO: not sure if this is right, i think kick triggers both events -sg
        handleQuit(event.getPlayer());
    }
    private void handleQuit(Player player) {
        for (BaseDuctType ductBaseType : BaseDuctType.values()) {
            RenderSystem renderSystem = ductService.getRenderSystem(player, ductBaseType);
            renderSystem.getCurrentPlayers().remove(player);
        }
    }

}
