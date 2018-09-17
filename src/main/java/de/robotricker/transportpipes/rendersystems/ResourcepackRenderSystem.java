package de.robotricker.transportpipes.rendersystems;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

import de.robotricker.transportpipes.ducts.DuctType;

public abstract class ResourcepackRenderSystem extends RenderSystem {

    private static Set<Player> resourcepackLoaded;

    public ResourcepackRenderSystem(DuctType ductType) {
        super(ductType);
        resourcepackLoaded = new HashSet<>();
    }
}
