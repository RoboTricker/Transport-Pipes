package de.robotricker.transportpipes.config;

import org.bukkit.plugin.Plugin;

import java.util.List;

import javax.inject.Inject;

public class GeneralConf extends Conf {

    @Inject
    public GeneralConf(Plugin configPlugin) {
        super(configPlugin, "config.yml", "config.yml");
    }

    public int getMaxItemsPerPipe() {
        return (int) read("max_items_per_pipe");
    }

    public boolean isCraftingEnabled() {
        return (boolean) read("crafting_enabled");
    }

    public boolean isDestroyDuctsOnExplosion() {
        return (boolean) read("destroy_ducts_on_explosion");
    }

    @SuppressWarnings("unchecked")
    public List<String> getAnticheatPlugins() {
        return (List<String>) read("anticheat_plugins");
    }

    public String getDefaultPipeRenderSystemName() {
        return (String) read("default_pipe_render_system");
    }

    public boolean isDefaultShowItems() {
        return (boolean) read("default_show_items");
    }

    public int getDefaultRenderDistance() {
        return (int) read("default_render_distance");
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

    public String getLanguage() {
        return (String) read("language");
    }

}
