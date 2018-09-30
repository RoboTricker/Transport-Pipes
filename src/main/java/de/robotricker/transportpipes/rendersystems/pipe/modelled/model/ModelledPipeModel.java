package de.robotricker.transportpipes.rendersystems.pipe.modelled.model;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

import de.robotricker.transportpipes.ducts.pipe.GoldenPipe;
import de.robotricker.transportpipes.ducts.types.BaseDuctType;
import de.robotricker.transportpipes.ducts.types.PipeType;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.rendersystems.pipe.modelled.model.data.ModelledExtractionPipeConnectionModelData;
import de.robotricker.transportpipes.rendersystems.pipe.modelled.model.data.ModelledIronPipeConnectionModelData;
import de.robotricker.transportpipes.rendersystems.pipe.modelled.model.data.ModelledPipeConnectionModelData;
import de.robotricker.transportpipes.location.RelativeLocation;
import de.robotricker.transportpipes.location.TPDirection;
import de.robotricker.transportpipes.utils.ItemUtils;

public class ModelledPipeModel {

    private static ItemStack ITEM_IRONPIPE_CONN_OUTPUT = ItemUtils.createModelledItem(22);
    private static ItemStack ITEM_EXTRACTIONPIPE_CONN_EXTRACT = ItemUtils.createModelledItem(39);
    private static Map<PipeType, ItemStack> midItems;
    private static Map<PipeType, ItemStack> connItems;
    private static Map<GoldenPipe.Color, ItemStack> goldenPipeConnItems;

    public ArmorStandData createMidASD(PipeType pipeType) {
        return new ArmorStandData(new RelativeLocation(0.5f, 0.5f - 1.1875f, 0.5f), false, new Vector(1, 0, 0), new Vector(180f, 0f, 0f), new Vector(0f, 0f, 0f), midItems.get(pipeType), null);
    }

    public ArmorStandData createConnASD(ModelledPipeConnectionModelData connModelData) {
        ItemStack connItem = connItems.get(connModelData.getPipeType());
        if (connModelData.getPipeType().is("Iron") && ((ModelledIronPipeConnectionModelData) connModelData).isOutputSide()) {
            connItem = ITEM_IRONPIPE_CONN_OUTPUT;
        } else if (connModelData.getPipeType().is("Extraction") && ((ModelledExtractionPipeConnectionModelData) connModelData).isExtractionSide()) {
            connItem = ITEM_EXTRACTIONPIPE_CONN_EXTRACT;
        } else if (connModelData.getPipeType().is("Golden")) {
            connItem = goldenPipeConnItems.get(GoldenPipe.Color.getByDir(connModelData.getConnectionDir()));
        }

        ArmorStandData asd;
        if (connModelData.getConnectionDir() == TPDirection.UP) {
            asd = new ArmorStandData(new RelativeLocation(0.75f, 0.5f - 1.4369f, 0.5f), false, new Vector(1, 0, 0), new Vector(-90f, 0f, 0f), new Vector(0f, 0f, 0f), connItem, null);
        } else if (connModelData.getConnectionDir() == TPDirection.DOWN) {
            asd = new ArmorStandData(new RelativeLocation(0.25f, 0.5f - 1.1885f - 0.25f, 0.5f), false, new Vector(1, 0, 0), new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f), connItem, null);
        } else {
            asd = new ArmorStandData(new RelativeLocation(0.5f, 0.5f - 1.1875f, 0.5f), false, new Vector(connModelData.getConnectionDir().getX(), 0, connModelData.getConnectionDir().getZ()), new Vector(180f, 180f, 0f), new Vector(0f, 0f, 0f), connItem, null);
        }
        return asd;
    }

    public static void init() {
        midItems = new HashMap<>();
        midItems.put(BaseDuctType.valueOf("Pipe").ductTypeValueOf("White"), ItemUtils.createModelledItem(1));
        midItems.put(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Blue"), ItemUtils.createModelledItem(2));
        midItems.put(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Red"), ItemUtils.createModelledItem(3));
        midItems.put(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Yellow"), ItemUtils.createModelledItem(4));
        midItems.put(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Green"), ItemUtils.createModelledItem(5));
        midItems.put(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Black"), ItemUtils.createModelledItem(6));
        midItems.put(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Golden"), ItemUtils.createModelledItem(13));
        midItems.put(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Iron"), ItemUtils.createModelledItem(20));
        midItems.put(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Ice"), ItemUtils.createModelledItem(23));
        midItems.put(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Void"), ItemUtils.createModelledItem(35));
        midItems.put(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Extraction"), ItemUtils.createModelledItem(37));
        midItems.put(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Crafting"), ItemUtils.createModelledItem(42));

        connItems = new HashMap<>();
        connItems.put(BaseDuctType.valueOf("Pipe").ductTypeValueOf("White"), ItemUtils.createModelledItem(7));
        connItems.put(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Blue"), ItemUtils.createModelledItem(8));
        connItems.put(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Red"), ItemUtils.createModelledItem(9));
        connItems.put(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Yellow"), ItemUtils.createModelledItem(10));
        connItems.put(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Green"), ItemUtils.createModelledItem(11));
        connItems.put(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Black"), ItemUtils.createModelledItem(12));
        connItems.put(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Iron"), ItemUtils.createModelledItem(21));
        connItems.put(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Ice"), ItemUtils.createModelledItem(24));
        connItems.put(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Void"), ItemUtils.createModelledItem(36));
        connItems.put(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Extraction"), ItemUtils.createModelledItem(38));
        connItems.put(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Crafting"), ItemUtils.createModelledItem(43));

        goldenPipeConnItems = new HashMap<>();
        goldenPipeConnItems.put(GoldenPipe.Color.WHITE, ItemUtils.createModelledItem(14));
        goldenPipeConnItems.put(GoldenPipe.Color.BLUE, ItemUtils.createModelledItem(15));
        goldenPipeConnItems.put(GoldenPipe.Color.RED, ItemUtils.createModelledItem(16));
        goldenPipeConnItems.put(GoldenPipe.Color.YELLOW, ItemUtils.createModelledItem(17));
        goldenPipeConnItems.put(GoldenPipe.Color.GREEN, ItemUtils.createModelledItem(18));
        goldenPipeConnItems.put(GoldenPipe.Color.BLACK, ItemUtils.createModelledItem(19));
    }

}
