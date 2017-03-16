package de.robotricker.transportpipes.manager.settings;

import java.io.File;
import java.util.HashMap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class SettingsManager {

	public static final int DEFAULT_VIEW_DISTANCE = 25;
	public static HashMap<String, Integer> viewDistances = new HashMap<String, Integer>();

	public static void cacheSettings() {
		File parentDir = new File("plugins/TransportPipes/settings/");
		FileConfiguration cfg = null;
		try {
			for (File file : parentDir.listFiles()) {
				if(cfg == null){
					cfg = YamlConfiguration.loadConfiguration(file);
					if(cfg.contains("viewDistance")){
						viewDistances.put(file.getName().toLowerCase(), (int) cfg.get("viewDistance"));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int getViewDistance(Player p) {
		if (viewDistances.containsKey(p.getName().toLowerCase())) {
			return viewDistances.get(p.getName().toLowerCase());
		} else {
			return DEFAULT_VIEW_DISTANCE;
		}
	}

	public static void saveViewDistance(Player p, int renderDistance) {
		viewDistances.put(p.getName().toLowerCase(), renderDistance);
		try {
			File file = new File("plugins/TransportPipes/settings/" + p.getName().toLowerCase() + ".yml");
			FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
			cfg.set("viewDistance", viewDistances);
			cfg.save(file);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
