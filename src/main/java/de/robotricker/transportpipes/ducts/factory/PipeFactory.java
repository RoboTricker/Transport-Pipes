package de.robotricker.transportpipes.ducts.factory;

import org.bukkit.Chunk;
import org.bukkit.World;

import de.robotricker.transportpipes.ducts.pipe.ColoredPipe;
import de.robotricker.transportpipes.ducts.pipe.CraftingPipe;
import de.robotricker.transportpipes.ducts.pipe.ExtractionPipe;
import de.robotricker.transportpipes.ducts.pipe.GoldenPipe;
import de.robotricker.transportpipes.ducts.pipe.IcePipe;
import de.robotricker.transportpipes.ducts.pipe.IronPipe;
import de.robotricker.transportpipes.ducts.pipe.Pipe;
import de.robotricker.transportpipes.ducts.pipe.VoidPipe;
import de.robotricker.transportpipes.ducts.types.DuctType;
import de.robotricker.transportpipes.ducts.types.pipetype.ColoredPipeType;
import de.robotricker.transportpipes.inventory.ExtractionPipeSettingsInventory;
import de.robotricker.transportpipes.inventory.GoldenPipeSettingsInventory;
import de.robotricker.transportpipes.location.BlockLocation;

public class PipeFactory extends DuctFactory<Pipe> {

    @Override
    public Pipe createDuct(DuctType ductType, BlockLocation blockLoc, World world, Chunk chunk) {
        Pipe pipe = null;
        if (ductType instanceof ColoredPipeType) {
            pipe = new ColoredPipe(ductType, blockLoc, world, chunk, null, globalDuctManager);
        } else if (ductType.is("Golden")) {
            GoldenPipeSettingsInventory inv = transportPipes.getInjector().newInstance(GoldenPipeSettingsInventory.class);
            pipe = new GoldenPipe(ductType, blockLoc, world, chunk, inv, globalDuctManager);
            pipe.initSettingsInv(transportPipes);
        } else if (ductType.is("Iron")) {
            pipe = new IronPipe(ductType, blockLoc, world, chunk, null, globalDuctManager);
        } else if (ductType.is("Ice")) {
            pipe = new IcePipe(ductType, blockLoc, world, chunk, null, globalDuctManager);
        } else if (ductType.is("Void")) {
            pipe = new VoidPipe(ductType, blockLoc, world, chunk, null, globalDuctManager);
        } else if (ductType.is("Extraction")) {
            ExtractionPipeSettingsInventory inv = transportPipes.getInjector().newInstance(ExtractionPipeSettingsInventory.class);
            pipe = new ExtractionPipe(ductType, blockLoc, world, chunk, inv, globalDuctManager);
            pipe.initSettingsInv(transportPipes);
        } else if (ductType.is("Crafting")) {
            pipe = new CraftingPipe(ductType, blockLoc, world, chunk, null, globalDuctManager);
        }
        return pipe;
    }

}
