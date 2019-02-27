package de.robotricker.transportpipes.duct.types.pipetype;

import org.bukkit.DyeColor;
import org.bukkit.inventory.Recipe;

import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.duct.types.BaseDuctType;

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
