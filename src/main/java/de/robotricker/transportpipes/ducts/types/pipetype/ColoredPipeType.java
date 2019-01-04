package de.robotricker.transportpipes.ducts.types.pipetype;

import org.bukkit.DyeColor;

import de.robotricker.transportpipes.ducts.pipe.Pipe;
import de.robotricker.transportpipes.ducts.types.BaseDuctType;

public class ColoredPipeType extends PipeType {

    private DyeColor dyeColor;

    public ColoredPipeType(BaseDuctType<Pipe> baseDuctType, String name, char colorCode, DyeColor dyeColor) {
        super(baseDuctType, name, colorCode);
        this.dyeColor = dyeColor;
    }

    public DyeColor getDyeColor() {
        return dyeColor;
    }
}
