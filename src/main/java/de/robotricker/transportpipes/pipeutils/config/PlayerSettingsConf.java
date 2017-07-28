package de.robotricker.transportpipes.pipeutils.config;

import java.io.File;

import org.bukkit.entity.Player;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.rendersystem.PipeRenderSystem;

public class PlayerSettingsConf extends Conf {

	public PlayerSettingsConf(Player p) {
		super(new File(TransportPipes.instance.getDataFolder().getAbsolutePath() + File.separator + "settings" + File.separator + p.getUniqueId().toString() + ".yml"));
		saveAsDefault("renderDistance", 25);
		saveAsDefault("renderSystemId", 1);
		finishDefault();
	}

	public int getRenderDistance() {
		return (int) read("renderDistance");
	}

	public void setRenderDistance(int renderDistance) {
		overrideAsync("renderDistance", renderDistance);
	}

	public PipeRenderSystem getRenderSystem() {
		return PipeRenderSystem.getRenderSystemFromId((int) read("renderSystemId"));
	}

	public void setRenderSystem(int renderSystemId) {
		overrideAsync("renderSystemId", renderSystemId);
	}

}
