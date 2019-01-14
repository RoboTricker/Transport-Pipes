package de.robotricker.transportpipes.rendersystems.pipe.vanilla.model;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

import de.robotricker.transportpipes.hitbox.AxisAlignedBB;
import de.robotricker.transportpipes.location.RelativeLocation;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.rendersystems.pipe.vanilla.model.data.VanillaPipeModelData;

public class VanillaPipeModelNS extends VanillaPipeModel {

    public VanillaPipeModelNS() {
        super();
        aabb = new AxisAlignedBB(0.22, 0.22, 0, 0.78, 0.78, 1);
    }

    @Override
    public List<ArmorStandData> createASD(VanillaPipeModelData data) {
        return createSimplePipeASD(pipeBlocks.get(data.getPipeType()));
    }

    private List<ArmorStandData> createSimplePipeASD(ItemStack block) {
        List<ArmorStandData> asd = new ArrayList<>();

        asd.add(new ArmorStandData(new RelativeLocation(0.5f - 0.44f, -0.35f, 1f), false, new Vector(0, 0, -1), new Vector(0f, 0f, 0f), new Vector(-10f, 0f, 45f), null, ITEM_BLAZE_ROD));
        asd.add(new ArmorStandData(new RelativeLocation(0.5f - 0.86f, -1.0307f, 1f), false, new Vector(0, 0, -1), new Vector(0f, 0f, 0f), new Vector(-10f, 0f, 135f), null, ITEM_BLAZE_ROD));
        asd.add(new ArmorStandData(new RelativeLocation(0.5f - 0.37f, -1.0307f - 0.45f, 1f), false, new Vector(0, 0, -1), new Vector(0f, 0f, 0f), new Vector(-10f, 0f, 135f), null, ITEM_BLAZE_ROD));
        asd.add(new ArmorStandData(new RelativeLocation(0.5f - 0.93f, -0.35f - 0.45f, 1f), false, new Vector(0, 0, -1), new Vector(0f, 0f, 0f), new Vector(-10f, 0f, 45f), null, ITEM_BLAZE_ROD));
        asd.add(new ArmorStandData(new RelativeLocation(0.5f, -0.43f, 0.5f + 0.3f), true, new Vector(0, 0, -1), new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f), block, null));
        asd.add(new ArmorStandData(new RelativeLocation(0.5f, -0.43f, 0.5f - 0.2f), true, new Vector(0, 0, -1), new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f), block, null));

        return asd;
    }

}
