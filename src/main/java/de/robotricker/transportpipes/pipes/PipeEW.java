package de.robotricker.transportpipes.pipes;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jnbt.CompoundTag;
import org.jnbt.Tag;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipeutils.PipeColor;
import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.pipeutils.RelLoc;
import de.robotricker.transportpipes.pipeutils.hitbox.AxisAlignedBB;
import de.robotricker.transportpipes.protocol.ArmorStandData;

public class PipeEW extends Pipe {

	public PipeEW(Location blockLoc, List<PipeDirection> pipeNeighborBlocks, PipeColor pipeColor) {
		//PipeLoc | Body Direction | isSmall | HeadItem | HandItem | headRotation | handRotation
		//@formatter:off
		super(pipeColor, blockLoc, new AxisAlignedBB(0, 0.22, 0.22, 1, 0.78, 0.78), pipeNeighborBlocks,
			  new ArmorStandData(new RelLoc(0.05f, -0.35f, 0.5f - 0.44f), new Vector(1, 0, 0), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 0f, 45f)),
			  new ArmorStandData(new RelLoc(0.05f, -1.0307f, 0.5f - 0.86f), new Vector(1, 0, 0), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 0f, 135f)),
			  new ArmorStandData(new RelLoc(0.05f, -1.0307f - 0.45f, 0.5f - 0.37f), new Vector(1, 0, 0), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 0f, 135f)),
			  new ArmorStandData(new RelLoc(0.05f, -0.35f - 0.45f, 0.5f - 0.93f), new Vector(1, 0, 0), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 0f, 45f)),
			  new ArmorStandData(new RelLoc(0.55f - 0.3f, -0.43f, 0.5f), new Vector(1, 0, 0), true, pipeColor.getGlassItem(), null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)),
			  new ArmorStandData(new RelLoc(0.55f + 0.2f, -0.43f, 0.5f), new Vector(1, 0, 0), true, pipeColor.getGlassItem(), null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));
		//@formatter:on
	}

	@Override
	public PipeDirection itemArrivedAtMiddle(PipeItem item, PipeDirection before, List<PipeDirection> dirs) {
		return before;
	}

	@Override
	public void saveToNBTTag(HashMap<String, Tag> tags) {
		super.saveToNBTTag(tags);
	}

	@Override
	public void loadFromNBTTag(CompoundTag tag) {
		super.loadFromNBTTag(tag);
	}

	@Override
	public void destroy(boolean dropItem) {
		if (dropItem) {
			Bukkit.getScheduler().runTask(TransportPipes.instance, new Runnable() {

				@Override
				public void run() {
					blockLoc.getWorld().dropItem(blockLoc.clone().add(0.5d, 0.5d, 0.5d), TransportPipes.instance.getPipeItem(pipeColor));
				}
			});
		}
	}
}
