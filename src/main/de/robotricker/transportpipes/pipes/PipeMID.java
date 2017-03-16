package main.de.robotricker.transportpipes.pipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

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

public class PipeMID extends Pipe {

	public PipeMID(Location blockLoc, List<PipeDirection> pipeNeighborBlocks) {
		//PipeLoc | Body Direction | isSmall | HeadItem | HandItem | headRotation | handRotation
		//@formatter:off
		super(blockLoc, new AxisAlignedBB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75), pipeNeighborBlocks,
			  new ArmorStandData(new RelLoc(0.5f, -0.43f, 0.5f), new Vector(1, 0, 0), true, ITEM_GLASS, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));
		//@formatter:on
	}

	@Override
	public PipeDirection itemArrivedAtMiddle(PipeItem item, PipeDirection before, List<PipeDirection> dirs) {
		List<PipeDirection> clonedList = new ArrayList<>();
		clonedList.addAll(dirs);

		PipeDirection opposite = before.getOpposite();
		if (opposite != null && clonedList.contains(opposite)) {
			clonedList.remove(opposite);
		}
		if (!clonedList.isEmpty()) {
			return clonedList.get(new Random().nextInt(clonedList.size()));
		}

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
