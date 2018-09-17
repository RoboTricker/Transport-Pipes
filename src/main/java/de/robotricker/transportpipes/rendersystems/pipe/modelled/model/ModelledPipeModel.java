package de.robotricker.transportpipes.rendersystems.pipe.modelled.model;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

import de.robotricker.transportpipes.ducts.pipe.PipeType;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.rendersystems.pipe.modelled.model.data.ModelledExtractionPipeConnectionModelData;
import de.robotricker.transportpipes.rendersystems.pipe.modelled.model.data.ModelledGoldenPipeConnectionModelData;
import de.robotricker.transportpipes.rendersystems.pipe.modelled.model.data.ModelledIronPipeConnectionModelData;
import de.robotricker.transportpipes.rendersystems.pipe.modelled.model.data.ModelledPipeConnectionModelData;
import de.robotricker.transportpipes.utils.RelLoc;
import de.robotricker.transportpipes.utils.TPDirection;
import de.robotricker.transportpipes.utils.staticutils.ItemUtils;

public class ModelledPipeModel {

    public static final ItemStack ITEM_IRONPIPE_CONN_OUTPUT = ItemUtils.createModelledItem(22);
    public static final ItemStack ITEM_EXTRACTIONPIPE_CONN_EXTRACT = ItemUtils.createModelledItem(39);
    public static final ItemStack ITEM_GOLDENPIPE_CONN_WHITE = ItemUtils.createModelledItem(14);
    public static final ItemStack ITEM_GOLDENPIPE_CONN_BLUE = ItemUtils.createModelledItem(15);
    public static final ItemStack ITEM_GOLDENPIPE_CONN_RED = ItemUtils.createModelledItem(16);
    public static final ItemStack ITEM_GOLDENPIPE_CONN_YELLOW = ItemUtils.createModelledItem(17);
    public static final ItemStack ITEM_GOLDENPIPE_CONN_GREEN = ItemUtils.createModelledItem(18);
    public static final ItemStack ITEM_GOLDENPIPE_CONN_BLACK = ItemUtils.createModelledItem(19);

    private static Map<PipeType, ItemStack> midItems;
    private static Map<PipeType, ItemStack> connItems;

    public ArmorStandData createMidASD(PipeType pipeType) {
        return new ArmorStandData(new RelLoc(0.5f, 0.5f - 1.1875f, 0.5f), false, new Vector(1, 0, 0), new Vector(180f, 0f, 0f), new Vector(0f, 0f, 0f), midItems.get(pipeType), null);
    }

    public ArmorStandData createConnASD(ModelledPipeConnectionModelData connModelData) {
        ItemStack connItem = connItems.get(connModelData.getPipeType());
        if (connModelData.getPipeType().is("Iron") && ((ModelledIronPipeConnectionModelData) connModelData).isOutputSide()) {
            connItem = ITEM_IRONPIPE_CONN_OUTPUT;
        } else if (connModelData.getPipeType().is("Extraction") && ((ModelledExtractionPipeConnectionModelData) connModelData).isExtractionSide()) {
            connItem = ITEM_EXTRACTIONPIPE_CONN_EXTRACT;
        } else if (connModelData.getPipeType().is("Golden")) {
            connItem = ((ModelledGoldenPipeConnectionModelData) connModelData).getModelItem();
        }

        ArmorStandData asd;
        if (connModelData.getConnectionDir() == TPDirection.UP) {
            asd = new ArmorStandData(new RelLoc(0.75f, 0.5f - 1.4369f, 0.5f), false, new Vector(1, 0, 0), new Vector(-90f, 0f, 0f), new Vector(0f, 0f, 0f), connItem, null);
        } else if (connModelData.getConnectionDir() == TPDirection.DOWN) {
            asd = new ArmorStandData(new RelLoc(0.25f, 0.5f - 1.1885f - 0.25f, 0.5f), false, new Vector(1, 0, 0), new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f), connItem, null);
        } else {
            asd = new ArmorStandData(new RelLoc(0.5f, 0.5f - 1.1875f, 0.5f), false, new Vector(connModelData.getConnectionDir().getX(), 0, connModelData.getConnectionDir().getZ()), new Vector(180f, 180f, 0f), new Vector(0f, 0f, 0f), connItem, null);
        }
        return asd;
    }

    public static void init() {
        midItems = new HashMap<>();
        midItems.put(PipeType.valueOf("White"), ItemUtils.createModelledItem(1));
        midItems.put(PipeType.valueOf("Blue"), ItemUtils.createModelledItem(2));
        midItems.put(PipeType.valueOf("Red"), ItemUtils.createModelledItem(3));
        midItems.put(PipeType.valueOf("Yellow"), ItemUtils.createModelledItem(4));
        midItems.put(PipeType.valueOf("Green"), ItemUtils.createModelledItem(5));
        midItems.put(PipeType.valueOf("Black"), ItemUtils.createModelledItem(6));
        midItems.put(PipeType.valueOf("Golden"), ItemUtils.createModelledItem(13));
        midItems.put(PipeType.valueOf("Iron"), ItemUtils.createModelledItem(20));
        midItems.put(PipeType.valueOf("Ice"), ItemUtils.createModelledItem(23));
        midItems.put(PipeType.valueOf("Void"), ItemUtils.createModelledItem(35));
        midItems.put(PipeType.valueOf("Extraction"), ItemUtils.createModelledItem(37));
        midItems.put(PipeType.valueOf("Crafting"), ItemUtils.createModelledItem(42));

        connItems = new HashMap<>();
        connItems.put(PipeType.valueOf("White"), ItemUtils.createModelledItem(7));
        connItems.put(PipeType.valueOf("Blue"), ItemUtils.createModelledItem(8));
        connItems.put(PipeType.valueOf("Red"), ItemUtils.createModelledItem(9));
        connItems.put(PipeType.valueOf("Yellow"), ItemUtils.createModelledItem(10));
        connItems.put(PipeType.valueOf("Green"), ItemUtils.createModelledItem(11));
        connItems.put(PipeType.valueOf("Black"), ItemUtils.createModelledItem(12));
        connItems.put(PipeType.valueOf("Iron"), ItemUtils.createModelledItem(21));
        connItems.put(PipeType.valueOf("Ice"), ItemUtils.createModelledItem(24));
        connItems.put(PipeType.valueOf("Void"), ItemUtils.createModelledItem(36));
        connItems.put(PipeType.valueOf("Extraction"), ItemUtils.createModelledItem(38));
        connItems.put(PipeType.valueOf("Crafting"), ItemUtils.createModelledItem(43));
    }

}
