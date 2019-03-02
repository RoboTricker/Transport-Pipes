package de.robotricker.transportpipes.duct.pipe;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;

import de.robotricker.transportpipes.duct.manager.GlobalDuctManager;
import de.robotricker.transportpipes.duct.pipe.filter.ItemDistributorService;
import de.robotricker.transportpipes.duct.types.DuctType;
import de.robotricker.transportpipes.inventory.DuctSettingsInventory;
import de.robotricker.transportpipes.location.BlockLocation;

public class IcePipe extends Pipe {

    public IcePipe(DuctType ductType, BlockLocation blockLoc, World world, Chunk chunk, DuctSettingsInventory settingsInv, GlobalDuctManager globalDuctManager, ItemDistributorService itemDistributor) {
        super(ductType, blockLoc, world, chunk, settingsInv, globalDuctManager, itemDistributor);
    }

    @Override
    double getPipeItemSpeed() {
        return super.getPipeItemSpeed() * 4;
    }

    @Override
    public Material getBreakParticleData() {
        return Material.ICE;
    }

}
