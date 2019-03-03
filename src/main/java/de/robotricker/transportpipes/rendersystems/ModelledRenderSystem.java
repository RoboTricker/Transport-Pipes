package de.robotricker.transportpipes.rendersystems;

import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.duct.types.BaseDuctType;
import de.robotricker.transportpipes.items.ItemService;

public abstract class ModelledRenderSystem extends RenderSystem {

    public ModelledRenderSystem(BaseDuctType baseDuctType) {
        super(baseDuctType);
    }

    public static ItemStack getItem(ItemService itemService) {
        return itemService.createModelledItem(25);
    }

    public static String getDisplayName() {
        return "MODELLED";
    }

}
