package de.robotricker.transportpipes.pipes;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.robotricker.transportpipes.rendersystem.RenderSystem;

public enum DuctType {

	PIPE(),
	WIRE();

	private Set<RenderSystem> renderSystems;

	DuctType() {
		renderSystems = Collections.synchronizedSet(new HashSet<RenderSystem>());
	}

	public Set<RenderSystem> getRenderSystems() {
		return renderSystems;
	}

	public void addRenderSystem(RenderSystem renderSystem) {
		renderSystems.add(renderSystem);
	}

}
