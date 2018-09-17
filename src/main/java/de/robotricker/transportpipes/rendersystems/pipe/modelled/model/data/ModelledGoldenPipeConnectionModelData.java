package de.robotricker.transportpipes.rendersystems.pipe.modelled.model.data;

import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.ducts.pipe.PipeType;
import de.robotricker.transportpipes.rendersystems.pipe.modelled.model.ModelledPipeModel;
import de.robotricker.transportpipes.utils.TPDirection;

public class ModelledGoldenPipeConnectionModelData extends ModelledPipeConnectionModelData {

    private ItemStack modelItem;

    public ModelledGoldenPipeConnectionModelData(TPDirection connectionDir) {
        super(PipeType.valueOf("Golden"), connectionDir);
        switch (connectionDir) {
            case EAST:
                modelItem = ModelledPipeModel.ITEM_GOLDENPIPE_CONN_BLUE;
                break;
            case WEST:
                modelItem = ModelledPipeModel.ITEM_GOLDENPIPE_CONN_YELLOW;
                break;
            case SOUTH:
                modelItem = ModelledPipeModel.ITEM_GOLDENPIPE_CONN_RED;
                break;
            case NORTH:
                modelItem = ModelledPipeModel.ITEM_GOLDENPIPE_CONN_WHITE;
                break;
            case UP:
                modelItem = ModelledPipeModel.ITEM_GOLDENPIPE_CONN_GREEN;
                break;
            case DOWN:
                modelItem = ModelledPipeModel.ITEM_GOLDENPIPE_CONN_BLACK;
                break;
        }
    }

    public ItemStack getModelItem() {
        return modelItem;
    }
}
