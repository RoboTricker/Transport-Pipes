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
		saveAsDefault("permissions.tps", "tp.tps");
		saveAsDefault("permissions.reload", "tp.reload");
		saveAsDefault("permissions.update", "tp.update");
		saveAsDefault("permissions.creative", "tp.creative");
		saveAsDefault("permissions.save", "tp.save");
		saveAsDefault("permissions.delete", "tp.delete");
		saveAsDefault("permissions.craft.pipe", "tp.craft.pipe");
		saveAsDefault("permissions.craft.colored", "tp.craft.colored");
		saveAsDefault("permissions.craft.golden", "tp.craft.golden");
		saveAsDefault("permissions.craft.iron", "tp.craft.iron");
		saveAsDefault("permissions.craft.ice", "tp.craft.ice");
		saveAsDefault("permissions.craft.void", "tp.craft.void");
		saveAsDefault("permissions.craft.extraction", "tp.craft.extraction");
		saveAsDefault("anticheat_plugins", Arrays.asList("NoCheatPlus", "AAC", "CompatNoCheatPlus", "AntiCheatPlus"));
		saveAsDefault("default_rendersystemId", 1);
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

	public String getPermissionSave() {
		return (String) read("permissions.save");
	}

	public String getPermissionDelete() {
		return (String) read("permissions.delete");
	}

	public String getPermissionCraftPipe() {
		return (String) read("permissions.craft.pipe");
	}

	public String getPermissionCraftColored() {
		return (String) read("permissions.craft.colored");
	}

	public String getPermissionCraftGolden() {
		return (String) read("permissions.craft.golden");
	}

	public String getPermissionCraftIron() {
		return (String) read("permissions.craft.iron");
	}

	public String getPermissionCraftIce() {
		return (String) read("permissions.craft.ice");
	}

	public String getPermissionCraftVoid() {
		return (String) read("permissions.craft.void");
	}

	public String getPermissionCraftExtraction() {
		return (String) read("permissions.craft.extraction");
	}

	@SuppressWarnings("unchecked")
	public List<String> getAnticheatPlugins() {
		return (List<String>) read("anticheat_plugins");
	}

	public int getDefaultRenderSystemId() {
		return (int) read("default_rendersystemId");
	}

	public boolean getDefaultShowItems() {
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
