package de.robotricker.transportpipes.rendersystems.pipe.vanilla.model;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

import de.robotricker.transportpipes.hitbox.AxisAlignedBB;
import de.robotricker.transportpipes.location.RelativeLocation;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.rendersystems.pipe.vanilla.model.data.VanillaPipeModelData;

public class VanillaPipeModelUD extends VanillaPipeModel {

    public VanillaPipeModelUD() {
        super();
        aabb = new AxisAlignedBB(0.22, 0, 0.22, 0.78, 1, 0.78);
    }

    @Override
    public List<ArmorStandData> createASD(VanillaPipeModelData data) {
        return createSimplePipeASD(pipeBlocks.get(data.getPipeType()));
    }

    private List<ArmorStandData> createSimplePipeASD(ItemStack block) {
        List<ArmorStandData> asd = new ArrayList<>();

        asd.add(new ArmorStandData(new RelativeLocation(0.05f + 1.3f, -1.3f, 0.5f - 0.25f), false, new Vector(1, 0, 1), new Vector(0f, 0f, 0f), new Vector(-10f, 90f, 90f), null, ITEM_BLAZE_ROD));
        asd.add(new ArmorStandData(new RelativeLocation(0.05f + 0.8f, -1.3f, 0.5f - 0.75f), false, new Vector(1, 0, 1), new Vector(0f, 0f, 0f), new Vector(-10f, 90f, 90f), null, ITEM_BLAZE_ROD));
        asd.add(new ArmorStandData(new RelativeLocation(0.05f + 1.2f, -1.3f, 0.5f + 0.4f), false, new Vector(-1, 0, 1), new Vector(0f, 0f, 0f), new Vector(-10f, 90f, 90f), null, ITEM_BLAZE_ROD));
        asd.add(new ArmorStandData(new RelativeLocation(0.05f + 0.74f, -1.3f, 0.5f + 0.84f), false, new Vector(-1, 0, 1), new Vector(0f, 0f, 0f), new Vector(-10f, 90f, 90f), null, ITEM_BLAZE_ROD));
        asd.add(new ArmorStandData(new RelativeLocation(0.5f, -0.675f, 0.5f), true, new Vector(1, 0, 0), new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f), block, null));
        asd.add(new ArmorStandData(new RelativeLocation(0.5f, -0.175f, 0.5f), true, new Vector(1, 0, 0), new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f), block, null));

        return asd;
    }

}
