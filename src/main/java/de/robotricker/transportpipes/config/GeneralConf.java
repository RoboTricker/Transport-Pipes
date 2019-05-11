package de.robotricker.transportpipes.config;

import org.bukkit.plugin.Plugin;

import java.util.List;

import javax.inject.Inject;

import de.robotricker.transportpipes.ResourcepackService;

public class GeneralConf extends Conf {

    @Inject
    public GeneralConf(Plugin configPlugin) {
        super(configPlugin, "config.yml", "config.yml", true);
    }

    public int getMaxItemsPerPipe() {
        return (int) read("max_items_per_pipe");
    }

    public boolean isCraftingEnabled() {
        return (boolean) read("crafting_enabled");
    }

    @SuppressWarnings("unchecked")
    public List<String> getAnticheatPlugins() {
        return (List<String>) read("anticheat_plugins");
    }

    public String getDefaultRenderSystemName() {
        return (String) read("default_render_system");
    }

    public boolean isDefaultShowItems() {
        return (boolean) read("default_show_items");
    }

    public int getDefaultRenderDistance() {
        return (int) read("default_render_distance");
    }

    @SuppressWarnings("unchecked")
    public List<String> getDisabledWorlds() {
        return (List<String>) read("disabled_worlds");
    }

    public String getWrenchItem() {
        return (String) read("wrench.item");
    }

    public boolean getWrenchGlowing() {
        return (boolean) read("wrench.glowing");
    }

    public String getLanguage() {
        return (String) read("language");
    }

    public int getShowHiddenDuctsTime() {
        return (int) read("show_hidden_ducts_time");
    }

    public ResourcepackService.ResourcepackMode getResourcepackMode() {
        String url = (String) read("resourcepack_mode");
        if (url == null || url.equalsIgnoreCase("default")) {
            return ResourcepackService.ResourcepackMode.DEFAULT;
        } else if (url.equalsIgnoreCase("none")) {
            return ResourcepackService.ResourcepackMode.NONE;
        } else if (url.equalsIgnoreCase("server")) {
            return ResourcepackService.ResourcepackMode.SERVER;
        }
        return ResourcepackService.ResourcepackMode.DEFAULT;
    }

}
