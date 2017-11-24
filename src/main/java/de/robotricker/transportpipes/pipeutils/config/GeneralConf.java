package de.robotricker.transportpipes.pipeutils.config;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.rendersystem.modelled.utils.ModelledPipeRenderSystem;

public class GeneralConf extends Conf {

	public GeneralConf() {
		super(new File(TransportPipes.instance.getDataFolder().getAbsolutePath() + File.separator + "config.yml"));
		saveAsDefault("max_items_per_pipe", 10);
		saveAsDefault("crafting_enabled", true);
		saveAsDefault("check_updates", true);
		saveAsDefault("destroy_pipe_on_explosion", true);
		saveAsDefault("anticheat_plugins", Arrays.asList("NoCheatPlus", "AAC", "CompatNoCheatPlus", "AntiCheatPlus"));
		saveAsDefault("default_rendersystemId", 1);
		saveAsDefault("force_default_rendersystem", false);
		saveAsDefault("default_renderdistance", 25);
		saveAsDefault("default_showitems", true);
		saveAsDefault("custom_resourcepack", "default");
		finishDefault();
	}

	public int getMaxItemsPerPipe() {
		return (int) read("max_items_per_pipe");
	}

	public boolean isCraftingEnabled() {
		return (boolean) read("crafting_enabled");
	}

	public boolean isCheckUpdates() {
		return (boolean) read("check_updates");
	}

	public boolean isDestroyPipeOnExplosion() {
		return (boolean) read("destroy_pipe_on_explosion");
	}

	@SuppressWarnings("unchecked")
	public List<String> getAnticheatPlugins() {
		return (List<String>) read("anticheat_plugins");
	}

	public int getDefaultRenderSystemId() {
		return (int) read("default_rendersystemId");
	}

	public boolean isForceDefaultRenderSystem() {
		return (boolean) read("force_default_rendersystem");
	}

	public boolean isDefaultShowItems() {
		return (boolean) read("default_showitems");
	}

	public int getDefaultRenderDistance() {
		return (int) read("default_renderdistance");
	}

	public String getResourcepackURL() {
		String customResourcePack = (String) read("custom_resourcepack");
		if (customResourcePack.equalsIgnoreCase("default")) {
			return ModelledPipeRenderSystem.RESOURCEPACK_URL;
		} else {
			return customResourcePack;
		}
	}

}
