package de.robotricker.transportpipes.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.inject.Inject;

import de.robotricker.transportpipes.PlayerSettingsService;
import de.robotricker.transportpipes.config.PlayerSettingsConf;
import de.robotricker.transportpipes.ducts.Duct;
import de.robotricker.transportpipes.ducts.DuctRegister;
import de.robotricker.transportpipes.ducts.manager.DuctManager;
import de.robotricker.transportpipes.ducts.manager.GlobalDuctManager;
import de.robotricker.transportpipes.ducts.manager.PipeManager;
import de.robotricker.transportpipes.ducts.types.BaseDuctType;
import de.robotricker.transportpipes.rendersystems.RenderSystem;

public class PlayerListener implements Listener {

    @Inject
    private GlobalDuctManager globalDuctManager;

    @Inject
    private DuctRegister ductRegister;

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        globalDuctManager.getPlayerDucts(e.getPlayer()).clear();
        ((PipeManager) (DuctManager<? extends Duct>) ductRegister.baseDuctTypeOf("pipe").getDuctManager()).getPlayerPipeItems(e.getPlayer()).clear();
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        //make sure that all ducts that were visible to the player get removed so they will spawn again when the player is nearby
        globalDuctManager.getPlayerDucts(e.getPlayer()).clear();
        ((PipeManager) (DuctManager<? extends Duct>) ductRegister.baseDuctTypeOf("pipe").getDuctManager()).getPlayerPipeItems(e.getPlayer()).clear();
    }

}
