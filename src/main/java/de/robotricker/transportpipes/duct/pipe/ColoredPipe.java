package de.robotricker.transportpipes.duct.pipe;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;

import de.robotricker.transportpipes.duct.manager.GlobalDuctManager;
import de.robotricker.transportpipes.duct.pipe.filter.ItemDistributorService;
import de.robotricker.transportpipes.duct.types.DuctType;
import de.robotricker.transportpipes.duct.types.pipetype.ColoredPipeType;
import de.robotricker.transportpipes.inventory.DuctSettingsInventory;
import de.robotricker.transportpipes.location.BlockLocation;

public class ColoredPipe extends Pipe {

    public ColoredPipe(DuctType ductType, BlockLocation blockLoc, World world, Chunk chunk, DuctSettingsInventory settingsInv, GlobalDuctManager globalDuctManager, ItemDistributorService itemDistributor) {
        super(ductType, blockLoc, world, chunk, settingsInv, globalDuctManager, itemDistributor);
    }

    @Override
    public Material getBreakParticleData() {
        return ((ColoredPipeType) getDuctType()).getColoringMaterial();
    }

}
