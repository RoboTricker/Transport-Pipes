package de.robotricker.transportpipes.config;

import org.bukkit.entity.Player;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.ducts.types.BaseDuctType;
import de.robotricker.transportpipes.rendersystems.RenderSystem;

public class PlayerSettingsConf extends Conf {

    public PlayerSettingsConf(TransportPipes transportPipes, Player p) {
        super(transportPipes, "playerconfig.yml", "playersettings/" + p.getUniqueId().toString() + ".yml");
    }

    public int getRenderDistance() {
        return (int) read("render_distance");
    }

    public void setRenderDistance(int renderDistance) {
        overrideAsync("render_distance", renderDistance);
    }

    public String getRenderSystemName() {
        return (String) read("render_system");
    }

    public RenderSystem getRenderSystem(BaseDuctType baseDuctType) {
        return RenderSystem.getRenderSystem(getRenderSystemName(), baseDuctType);
    }

    public void setRenderSystemName(String name) {
        overrideAsync("render_system", name);
    }

    public boolean isShowItems() {
        return (int) read("show_items") == 1;
    }

    public void setShowItems(boolean showItems) {
        overrideAsync("show_items", showItems ? 1 : 0);
    }

}
