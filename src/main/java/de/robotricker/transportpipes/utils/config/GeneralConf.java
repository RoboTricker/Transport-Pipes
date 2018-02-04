package de.robotricker.transportpipes.utils.config;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.rendersystem.modelled.utils.ModelledPipeRenderSystem;

public class GeneralConf extends Conf {

	public GeneralConf() {
		super(new File(TransportPipes.instance.getDataFolder().getAbsolutePath() + File.separator + "config.yml"), TransportPipes.instance);
		saveAsDefault("max_items_per_pipe", 20);
		saveAsDefault("crafting_enabled", true);
		saveAsDefault("check_updates", true);
		saveAsDefault("destroy_pipe_on_explosion", true);
		saveAsDefault("anticheat_plugins", Arrays.asList("NoCheatPlus", "AAC", "CompatNoCheatPlus", "AntiCheatPlus"));
		saveAsDefault("default_rendersystemId", 1);
		saveAsDefault("force_default_rendersystem", false);
		saveAsDefault("default_renderdistance", 25);
		saveAsDefault("default_showitems", true);
		saveAsDefault("resourcepack", "default"); // default, server, none, [URL]
		saveAsDefault("disabled_worlds", Arrays.asList());
		saveAsDefault("wrench.item", "280:0");
		saveAsDefault("wrench.enchanted", true);
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

	public boolean isResourcepackEnabled() {
		String customResourcePack = (String) read("resourcepack");
		return !customResourcePack.equalsIgnoreCase("none");
	}

	public String getResourcepack() {
		String resourcepack = (String) read("resourcepack");
		if (resourcepack.equalsIgnoreCase("default")) {
			return TransportPipes.RESOURCEPACK_URL;
		} else if (resourcepack.equalsIgnoreCase("server") || resourcepack.equalsIgnoreCase("none")) {
			return null;
		} else {
			return resourcepack;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getDisabledWorlds(){
		return (List<String>) read("disabled_worlds");
	}
	
	public String getWrenchItem() {
		return (String) read("wrench.item");
	}
	
	public boolean getWrenchEnchanted() {
		return (boolean) read("wrench.enchanted");
	}

}
