package de.robotricker.transportpipes;

import org.bukkit.plugin.java.JavaPlugin;

public class TransportPipes extends JavaPlugin {

	public static TransportPipes instance;

	@Override
	public void onEnable() {
		instance = this;
	}

	@Override
	public void onDisable() {

	}

}
