package de.robotricker.transportpipes.ducts.pipe;

import org.bukkit.Chunk;
import org.bukkit.World;

import de.robotricker.transportpipes.ducts.manager.GlobalDuctManager;
import de.robotricker.transportpipes.ducts.types.DuctType;
import de.robotricker.transportpipes.inventory.DuctSettingsInventory;
import de.robotricker.transportpipes.location.BlockLocation;

public class IcePipe extends Pipe {

    public IcePipe(DuctType ductType, BlockLocation blockLoc, World world, Chunk chunk, DuctSettingsInventory settingsInv, GlobalDuctManager globalDuctManager) {
        super(ductType, blockLoc, world, chunk, settingsInv, globalDuctManager);
    }

    @Override
    double getPipeItemSpeed() {
        return super.getPipeItemSpeed() * 4;
    }

    @Override
    public int[] getBreakParticleData() {
        return new int[]{79, 0};
    }

}
