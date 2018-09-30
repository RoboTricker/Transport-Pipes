package de.robotricker.transportpipes.rendersystems;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

import de.robotricker.transportpipes.ducts.types.BaseDuctType;

public abstract class ResourcepackRenderSystem extends RenderSystem {

    private static Set<Player> resourcepackLoaded;

    public ResourcepackRenderSystem(BaseDuctType baseDuctType) {
        super(baseDuctType);
        resourcepackLoaded = new HashSet<>();
    }
}
