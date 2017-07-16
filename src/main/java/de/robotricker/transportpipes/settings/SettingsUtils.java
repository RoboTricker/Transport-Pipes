package de.robotricker.transportpipes.settings;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import de.robotricker.transportpipes.pipeutils.config.PlayerSettingsConf;

public class SettingsUtils {

	private static Map<Player, PlayerSettingsConf> cachedSettings = new HashMap<>();

	public static PlayerSettingsConf loadPlayerSettings(Player p) {
		if (!cachedSettings.containsKey(p)) {
			cachedSettings.put(p, new PlayerSettingsConf(p));
		}
		return cachedSettings.get(p);
	}

}
