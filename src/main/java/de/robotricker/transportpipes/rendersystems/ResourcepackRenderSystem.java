package de.robotricker.transportpipes.rendersystems;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

import de.robotricker.transportpipes.ducts.types.BasicDuctType;

public abstract class ResourcepackRenderSystem extends RenderSystem {

    private static Set<Player> resourcepackLoaded;

    public ResourcepackRenderSystem(BasicDuctType basicDuctType) {
        super(basicDuctType);
        resourcepackLoaded = new HashSet<>();
    }
}
