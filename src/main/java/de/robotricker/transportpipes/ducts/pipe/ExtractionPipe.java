package de.robotricker.transportpipes.ducts.pipe;

import org.bukkit.Chunk;
import org.bukkit.World;

import de.robotricker.transportpipes.ducts.manager.GlobalDuctManager;
import de.robotricker.transportpipes.ducts.types.DuctType;
import de.robotricker.transportpipes.inventory.DuctSettingsInventory;
import de.robotricker.transportpipes.location.BlockLocation;

public class ExtractionPipe extends Pipe {

    public ExtractionPipe(DuctType ductType, BlockLocation blockLoc, World world, Chunk chunk, DuctSettingsInventory settingsInv, GlobalDuctManager globalDuctManager) {
        super(ductType, blockLoc, world, chunk, settingsInv, globalDuctManager);
    }

    @Override
    public int[] getBreakParticleData() {
        return new int[] { 5, 0 };
    }

}
