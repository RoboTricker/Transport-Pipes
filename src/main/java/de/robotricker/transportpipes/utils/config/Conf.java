package de.robotricker.transportpipes.utils.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import de.robotricker.transportpipes.TransportPipes;

public abstract class Conf {

	private Plugin plugin;
	private File configFile;
	private YamlConfiguration yamlConf;
	private Map<String, Object> defaultValues = new HashMap<>();
	private Map<String, Object> cachedValues = new HashMap<>();

	public Conf(File configFile, Plugin plugin) {
		this.configFile = configFile;
		this.plugin = plugin;
		yamlConf = YamlConfiguration.loadConfiguration(configFile);
	}

	protected YamlConfiguration getYamlConf(){
		return yamlConf;
	}
	
	protected void saveAsDefault(String key, Object value) {
		if (!yamlConf.contains(key)) {
			yamlConf.set(key, value);
		}
		defaultValues.put(key, value);
	}
	
	protected void finishDefault() {
		removeUnusedValues(yamlConf);
		saveToFile();
	}

	private void removeUnusedValues(ConfigurationSection cs) {
		Map<String, Object> objs = cs.getValues(false);
		for (String key : objs.keySet()) {
			if (cs.isConfigurationSection(key)) {
				removeUnusedValues(cs.getConfigurationSection(key));
				if (cs.getConfigurationSection(key) == null || cs.getConfigurationSection(key).getValues(false).isEmpty()) {
					cs.set(key, null);
				}
			} else if (!defaultValues.containsKey(cs.getCurrentPath() + (cs.getCurrentPath().isEmpty() ? "" : ".") + key)) {
				cs.set(key, null);
			}
		}
	}

	public void overrideSync(String key, Object value) {
		cachedValues.put(key, value);
		yamlConf.set(key, value);
		saveToFile();
	}

	public void overrideAsync(String key, Object value) {
		cachedValues.put(key, value);
		yamlConf.set(key, value);
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				saveToFile();
			}
		});
	}

	public Object read(String key) {
		if (cachedValues.containsKey(key)) {
			return cachedValues.get(key);
		}
		if (yamlConf.contains(key)) {
			Object val = yamlConf.get(key);
			cachedValues.put(key, val);
			return val;
		}
		Object val = defaultValues.get(key);
		cachedValues.put(key, val);
		return val;
	}

	public Collection<String> readSubKeys(String path) {
		try {
			return yamlConf.getConfigurationSection(path).getKeys(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<String>();
	}

	public void reload() {
		cachedValues.clear();
		try {
			yamlConf.load(configFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveToFile() {
		try {
			yamlConf.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
