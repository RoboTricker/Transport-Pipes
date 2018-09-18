package de.robotricker.transportpipes.ducts.factory;

import org.bukkit.Chunk;

import de.robotricker.transportpipes.ducts.Duct;
import de.robotricker.transportpipes.ducts.types.DuctType;
import de.robotricker.transportpipes.utils.BlockLoc;

public abstract class DuctFactory {

    public abstract Duct createDuct(DuctType ductType, BlockLoc blockLoc, Chunk chunk);

}
