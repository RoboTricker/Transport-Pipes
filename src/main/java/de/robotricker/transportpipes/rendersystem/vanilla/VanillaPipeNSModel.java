package de.robotricker.transportpipes.rendersystem.vanilla;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.pipeitems.RelLoc;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipes.colored.PipeColor;
import de.robotricker.transportpipes.pipeutils.hitbox.AxisAlignedBB;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.rendersystem.vanilla.utils.VanillaPipeModelData;

public class VanillaPipeNSModel extends VanillaPipeModel {

	public VanillaPipeNSModel() {
		super();
		aabb = new AxisAlignedBB(0.22, 0.22, 0, 0.78, 0.78, 1);
	}
	
	@Override
	public List<ArmorStandData> createASD(VanillaPipeModelData data) {
		if (data.getPipeType() == PipeType.COLORED) {
			return createColoredASD(data.getColoredPipe_pipeColor());
		} else if (data.getPipeType() == PipeType.ICE) {
			return createIceASD();
		}
		return null;
	}

	private List<ArmorStandData> createColoredASD(PipeColor pc) {
		List<ArmorStandData> asds = new ArrayList<>();
		ItemStack block = pc.getVanillaModel_GlassItem();

		asds.add(new ArmorStandData(new RelLoc(0.5f - 0.44f, -0.35f, 1f), new Vector(0, 0, -1), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 0f, 45f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f - 0.86f, -1.0307f, 1f), new Vector(0, 0, -1), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 0f, 135f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f - 0.37f, -1.0307f - 0.45f, 1f), new Vector(0, 0, -1), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 0f, 135f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f - 0.93f, -0.35f - 0.45f, 1f), new Vector(0, 0, -1), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 0f, 45f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.43f, 0.5f + 0.3f), new Vector(0, 0, -1), true, block, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.43f, 0.5f - 0.2f), new Vector(0, 0, -1), true, block, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));

		return asds;

	}

	private List<ArmorStandData> createIceASD() {
		List<ArmorStandData> asds = new ArrayList<>();
		ItemStack block = ITEM_ICE_BLOCK;

		asds.add(new ArmorStandData(new RelLoc(0.5f - 0.44f, -0.35f, 1f), new Vector(0, 0, -1), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 0f, 45f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f - 0.86f, -1.0307f, 1f), new Vector(0, 0, -1), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 0f, 135f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f - 0.37f, -1.0307f - 0.45f, 1f), new Vector(0, 0, -1), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 0f, 135f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f - 0.93f, -0.35f - 0.45f, 1f), new Vector(0, 0, -1), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 0f, 45f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.43f, 0.5f + 0.3f), new Vector(0, 0, -1), true, block, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));
		asds.add(new ArmorStandData(new RelLoc(0.5f, -0.43f, 0.5f - 0.2f), new Vector(0, 0, -1), true, block, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));

		return asds;

	}

}
