package de.robotricker.transportpipes.rendersystem;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

import de.robotricker.transportpipes.duct.types.BaseDuctType;

public abstract class ResourcePackRenderSystem extends RenderSystem {

    private static Set<Player> resourcePackLoaded;

    public ResourcePackRenderSystem(BaseDuctType basicDuctType) {
        super(basicDuctType);
        resourcePackLoaded = new HashSet<>();
    }
}
