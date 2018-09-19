package de.robotricker.transportpipes.duct.factory;

import org.bukkit.Chunk;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.types.DuctType;
import de.robotricker.transportpipes.location.BlockLocation;

public abstract class DuctFactory {

    public abstract Duct createDuct(DuctType ductType, BlockLocation blockLoc, Chunk chunk);

}
