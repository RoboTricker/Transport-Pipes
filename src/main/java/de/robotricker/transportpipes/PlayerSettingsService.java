package de.robotricker.transportpipes;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import de.robotricker.transportpipes.config.GeneralConf;
import de.robotricker.transportpipes.config.PlayerSettingsConf;

public class PlayerSettingsService {

    @Inject
    private TransportPipes transportPipes;
    @Inject
    private GeneralConf generalConf;

    private Map<Player, PlayerSettingsConf> cachedSettingsConfs;

    public PlayerSettingsService() {
        this.cachedSettingsConfs = new HashMap<>();
    }

    public PlayerSettingsConf getOrCreateSettingsConf(Player p) {
        if (!cachedSettingsConfs.containsKey(p)) {
            cachedSettingsConfs.put(p, new PlayerSettingsConf(transportPipes, generalConf, p));
        }
        return cachedSettingsConfs.get(p);
    }

}
