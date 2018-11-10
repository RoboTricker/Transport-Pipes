package de.robotricker.transportpipes.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;

import javax.inject.Inject;

import de.robotricker.transportpipes.TransportPipes;

public class WorldListener implements Listener {

    @Inject
    private TransportPipes plugin;

    @EventHandler
    public void onWorldSave(WorldSaveEvent e) {
        plugin.saveWorld(e.getWorld());
    }

}
