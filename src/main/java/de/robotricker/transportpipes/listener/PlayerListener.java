package de.robotricker.transportpipes.listener;


import de.robotricker.transportpipes.DuctManager;
import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.rendersystem.RenderSystem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.inject.Inject;

public class PlayerListener implements Listener {

    @Inject
    private DuctManager ductManager;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        for (BaseDuctType ductBaseType : BaseDuctType.values()) {
            RenderSystem renderSystem = ductManager.getRenderSystems(ductBaseType).get(0);
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
            RenderSystem renderSystem = ductManager.getRenderSystem(player, ductBaseType);
            renderSystem.getCurrentPlayers().remove(player);
        }
    }
}
