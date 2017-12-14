package de.robotricker.transportpipes.pipes;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.pipeutils.DuctDetails;
import de.robotricker.transportpipes.rendersystem.RenderSystem;
import io.sentry.Sentry;

public enum DuctType {

	PIPE("TransportPipes"),
	WIRE("ElectricWires");

	private Set<RenderSystem> renderSystems;
	private Class<? extends DuctDetails> ductDetailsClass;
	private String pluginName;
	private boolean enabled = false;

	DuctType(String pluginName) {
		this.renderSystems = Collections.synchronizedSet(new HashSet<RenderSystem>());
		this.pluginName = pluginName;
	}

	public Set<RenderSystem> getRenderSystems() {
		return renderSystems;
	}

	public void addRenderSystem(RenderSystem renderSystem) {
		renderSystems.add(renderSystem);
	}

	public void setDuctDetailsClass(Class<? extends DuctDetails> ductDetailsClass) {
		this.ductDetailsClass = ductDetailsClass;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public DuctDetails createDuctDetails(String serialization) {
		try {
			DuctDetails ductDetails = ductDetailsClass.newInstance();
			ductDetails.fromString(serialization);
			return ductDetails;
		} catch (Exception e) {
			e.printStackTrace();
			Sentry.capture(e);
		}
		return null;
	}

	public static void checkEnabledPlugins() {
		for (DuctType dt : DuctType.values()) {
			dt.enabled = Bukkit.getPluginManager().isPluginEnabled(dt.pluginName);
		}
	}

}
