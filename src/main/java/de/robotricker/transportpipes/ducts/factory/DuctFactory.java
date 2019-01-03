package de.robotricker.transportpipes.ducts.factory;

import org.bukkit.Chunk;
import org.bukkit.World;

import javax.inject.Inject;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.ducts.Duct;
import de.robotricker.transportpipes.ducts.types.DuctType;
import de.robotricker.transportpipes.location.BlockLocation;

public abstract class DuctFactory<T extends Duct> {

    @Inject
    protected TransportPipes transportPipes;

    public abstract T createDuct(DuctType ductType, BlockLocation blockLoc, World world, Chunk chunk);

}
