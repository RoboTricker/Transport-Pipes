package de.robotricker.transportpipes.pipes;

import java.util.ArrayList;
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

public class PipeMID extends Pipe {

	public PipeMID(Location blockLoc, List<PipeDirection> pipeNeighborBlocks, PipeColor pipeColor) {
		//PipeLoc | Body Direction | isSmall | HeadItem | HandItem | headRotation | handRotation
		//@formatter:off
		super(pipeColor, blockLoc, new AxisAlignedBB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75), pipeNeighborBlocks,
			  new ArmorStandData(new RelLoc(0.5f, -0.43f, 0.5f), new Vector(1, 0, 0), true, pipeColor.getGlassItem(), null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)));
		//@formatter:on
		lastDir = -1;
	}

	private int lastDir;

	@Override
	public PipeDirection itemArrivedAtMiddle(PipeItem item, PipeDirection before, List<PipeDirection> dirs) {
		List<PipeDirection> clonedList = new ArrayList<>();
		clonedList.addAll(dirs);

		PipeDirection opposite = before.getOpposite();
		if (opposite != null && clonedList.contains(opposite)) {
			clonedList.remove(opposite);
		}
		if (!clonedList.isEmpty()) {
			lastDir++;
			if (lastDir >= clonedList.size()) {
				lastDir = 0;
			}
			return clonedList.get(lastDir);
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
					blockLoc.getWorld().dropItem(blockLoc.clone().add(0.5d, 0.5d, 0.5d), TransportPipes.instance.getPipeItem(pipeColor));
				}
			});
		}
	}
}
