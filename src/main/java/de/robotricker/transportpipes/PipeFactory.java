package de.robotricker.transportpipes;

import org.bukkit.Chunk;
import org.bukkit.World;

import de.robotricker.transportpipes.ducts.pipe.Pipe;
import de.robotricker.transportpipes.ducts.types.DuctType;
import de.robotricker.transportpipes.location.BlockLocation;

public class PipeFactory extends DuctFactory<Pipe> {

    @Override
    public Pipe createDuct(DuctType ductType, BlockLocation blockLoc, World world, Chunk chunk) {
        return null;
    }

}
