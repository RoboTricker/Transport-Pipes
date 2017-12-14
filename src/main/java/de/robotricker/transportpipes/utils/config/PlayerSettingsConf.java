package de.robotricker.transportpipes.utils.config;

import java.io.File;
import java.util.List;

import org.bukkit.entity.Player;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.duct.DuctType;
import de.robotricker.transportpipes.rendersystem.RenderSystem;

public class PlayerSettingsConf extends Conf {

	public PlayerSettingsConf(Player p) {
		super(new File(TransportPipes.instance.getDataFolder().getAbsolutePath() + File.separator + "settings" + File.separator + p.getUniqueId().toString() + ".yml"), TransportPipes.instance);
		saveAsDefault("renderDistance", TransportPipes.instance.generalConf.getDefaultRenderDistance());
		saveAsDefault("renderSystemId", TransportPipes.instance.generalConf.getDefaultRenderSystemId());
		saveAsDefault("showItems", TransportPipes.instance.generalConf.isDefaultShowItems());
		finishDefault();
	}

	public int getRenderDistance() {
		return (int) read("renderDistance");
	}

	public void setRenderDistance(int renderDistance) {
		overrideAsync("renderDistance", renderDistance);
	}

	public int getRenderSystemId() {
		int renderSystemId = (int) read("renderSystemId");
		// overwrite player specific render system with default one if forced to.
		int defaultRenderSystemId = TransportPipes.instance.generalConf.getDefaultRenderSystemId();
		if (TransportPipes.instance.generalConf.isForceDefaultRenderSystem()) {
			renderSystemId = defaultRenderSystemId;
		}
		return renderSystemId;
	}

	public RenderSystem getRenderSystem(DuctType ductType) {
		int renderSystemId = getRenderSystemId();
		return RenderSystem.getRenderSystemFromId(renderSystemId, ductType);
	}

	public void setRenderSystem(int renderSystemId) {
		overrideAsync("renderSystemId", renderSystemId);
	}

	public boolean isShowItems() {
		return (boolean) read("showItems");
	}

	public void setShowItems(boolean showItems) {
		overrideAsync("showItems", showItems);
	}

}
