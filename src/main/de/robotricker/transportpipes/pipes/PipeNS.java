package main.de.robotricker.transportpipes.pipes;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jnbt.CompoundTag;
import org.jnbt.Tag;

import main.de.robotricker.transportpipes.TransportPipes;
import main.de.robotricker.transportpipes.pipeitems.PipeItem;
import main.de.robotricker.transportpipes.pipeutils.PipeDirection;
import main.de.robotricker.transportpipes.pipeutils.RelLoc;
import main.de.robotricker.transportpipes.pipeutils.hitbox.AxisAlignedBB;
import main.de.robotricker.transportpipes.protocol.ArmorStandData;

public class PipeNS extends Pipe {

	public PipeNS(Location blockLoc, List<PipeDirection> pipeNeighborBlocks) {
		//PipeLoc | Body Direction | isSmall | HeadItem | HandItem | headRotation | handRotation
		//@formatter:off
		super(blockLoc, new AxisAlignedBB(0.22, 0.22, 0, 0.78, 0.78, 1), pipeNeighborBlocks,
			  new ArmorStandData(new RelLoc(0.5f - 0.44f, -0.35f, 1f), new Vector(0, 0, -1), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 0f, 45f)),
			  new ArmorStandData(new RelLoc(0.5f - 0.86f, -1.0307f, 1f), new Vector(0, 0, -1), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 0f, 135f)),
			  new ArmorStandData(new RelLoc(0.5f - 0.37f, -1.0307f - 0.45f, 1f), new Vector(0, 0, -1), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 0f, 135f)),
			  new ArmorStandData(new RelLoc(0.5f - 0.93f, -0.35f - 0.45f, 1f), new Vector(0, 0, -1), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 0f, 45f)),
			  new ArmorStandData(new RelLoc(0.5f, -0.43f, 0.5f + 0.3f), new Vector(0, 0, -1), true, ITEM_GLASS, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)),
			  new ArmorStandData(new RelLoc(0.5f, -0.43f, 0.5f - 0.2f), new Vector(0, 0, -1), true, ITEM_GLASS, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));
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
					blockLoc.getWorld().dropItem(blockLoc.clone().add(0.5d, 0.5d, 0.5d), TransportPipes.PIPE_ITEM);
				}
			});
		}
	}
}
