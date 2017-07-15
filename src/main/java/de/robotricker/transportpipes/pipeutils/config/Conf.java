package de.robotricker.transportpipes.pipeutils.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public abstract class Conf {

	protected File configFile;
	protected YamlConfiguration yamlConf;
	private Map<String, Object> defaultValues = new HashMap<String, Object>();
	private Map<String, Object> cachedValues = new HashMap<String, Object>();

	public Conf(File configFile) {
		this.configFile = configFile;
		yamlConf = YamlConfiguration.loadConfiguration(configFile);
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

	public void override(String key, Object value) {
		cachedValues.put(key, value);
		yamlConf.set(key, value);
		saveToFile();
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
