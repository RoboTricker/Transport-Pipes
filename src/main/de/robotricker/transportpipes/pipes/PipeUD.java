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

public class PipeUD extends Pipe {

	public PipeUD(Location blockLoc, List<PipeDirection> pipeNeighborBlocks) {
		//PipeLoc | Body Direction | isSmall | HeadItem | HandItem | headRotation | handRotation
		//@formatter:off
		super(blockLoc, new AxisAlignedBB(0.22, 0, 0.22, 0.78, 1, 0.78), pipeNeighborBlocks,
			  new ArmorStandData(new RelLoc(0.05f + 1.3f, -1.3f, 0.5f - 0.25f), new Vector(1, 0, 1), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 90f, 90f)),
			  new ArmorStandData(new RelLoc(0.05f + 0.8f, -1.3f, 0.5f - 0.75f), new Vector(1, 0, 1), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 90f, 90f)),
			  new ArmorStandData(new RelLoc(0.05f + 1.2f, -1.3f, 0.5f + 0.4f), new Vector(-1, 0, 1), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 90f, 90f)),
			  new ArmorStandData(new RelLoc(0.05f + 0.74f, -1.3f, 0.5f + 0.84f), new Vector(-1, 0, 1), false, null, ITEM_BLAZE, new Vector(0f, 0f, 0f), new Vector(-10f, 90f, 90f)),
			  new ArmorStandData(new RelLoc(0.5f, -0.675f, 0.5f), new Vector(1, 0, 0), true, ITEM_GLASS, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)),
			  new ArmorStandData(new RelLoc(0.5f, -0.175f, 0.5f), new Vector(1, 0, 0), true, ITEM_GLASS, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));
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
