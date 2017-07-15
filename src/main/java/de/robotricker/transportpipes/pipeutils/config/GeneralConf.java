package de.robotricker.transportpipes.pipeutils.config;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import de.robotricker.transportpipes.TransportPipes;

public class GeneralConf extends Conf {

	public GeneralConf() {
		super(new File(TransportPipes.instance.getDataFolder().getAbsolutePath() + File.separator + "config.yml"));
		saveAsDefault("max_items_per_pipe", 10);
		saveAsDefault("crafting_enabled", true);
		saveAsDefault("check_updates", true);
		saveAsDefault("destroy_pipe_on_explosion", true);
		saveAsDefault("permissions.tps", "tp.tps");
		saveAsDefault("permissions.reload", "tp.reload");
		saveAsDefault("permissions.update", "tp.update");
		saveAsDefault("permissions.creative", "tp.creative");
		saveAsDefault("anticheat_plugins", Arrays.asList("NoCheatPlus", "AAC", "CompatNoCheatPlus", "AntiCheatPlus"));
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

	public String getPermissionTps() {
		return (String) read("permissions.tps");
	}

	public String getPermissionReload() {
		return (String) read("permissions.reload");
	}

	public String getPermissionUpdate() {
		return (String) read("permissions.update");
	}

	public String getPermissionCreative() {
		return (String) read("permissions.creative");
	}

	@SuppressWarnings("unchecked")
	public List<String> getAnticheatPlugins() {
		return (List<String>) read("anticheat_plugins");
	}

}
