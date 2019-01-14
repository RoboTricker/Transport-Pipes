package de.robotricker.transportpipes.ducts.types.pipetype;

import org.bukkit.DyeColor;

import de.robotricker.transportpipes.ducts.pipe.Pipe;
import de.robotricker.transportpipes.ducts.types.BaseDuctType;

public class ColoredPipeType extends PipeType {

    private DyeColor dyeColor;

    public ColoredPipeType(BaseDuctType<Pipe> baseDuctType, String name, String displayName, DyeColor dyeColor) {
        super(baseDuctType, name, displayName);
        this.dyeColor = dyeColor;
    }

    public DyeColor getDyeColor() {
        return dyeColor;
    }
}
