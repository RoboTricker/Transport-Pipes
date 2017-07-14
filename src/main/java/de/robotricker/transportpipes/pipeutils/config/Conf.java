package de.robotricker.transportpipes.pipeutils.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;

public abstract class Conf {

	protected File configFile;
	protected YamlConfiguration yamlConf;
	private Map<String, Object> defaultKeys = new HashMap<String, Object>();

	public Conf(File configFile) {
		this.configFile = configFile;
		yamlConf = YamlConfiguration.loadConfiguration(configFile);
	}

	protected void saveAsDefault(String key, Object value) {
		if (!yamlConf.contains(key)) {
			yamlConf.set(key, value);
		}
		defaultKeys.put(key, value);
	}

	protected void finishDefault() {
		Map<String, Object> objs = yamlConf.getValues(true);
		for (String key : objs.keySet()) {
			System.out.println(key);
			if (!defaultKeys.containsKey(key)) {
				//TODO: remove "key" from yamlConf
			}
		}
		System.out.println("---------");
		saveToFile();
	}

	public void override(String key, Object value) {
		yamlConf.set(key, value);
		saveToFile();
	}

	public Object read(String key) {
		if (yamlConf.contains(key)) {
			return yamlConf.get(key);
		}
		return defaultKeys.get(key);
	}

	public void saveToFile() {
		try {
			yamlConf.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
