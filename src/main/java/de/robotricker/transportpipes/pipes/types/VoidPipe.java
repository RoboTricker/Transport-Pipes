package de.robotricker.transportpipes.pipes.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipes.PipeDirection;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipeutils.PipeItemUtils;

public class VoidPipe extends Pipe {

	public VoidPipe(Location blockLoc) {
		super(blockLoc);
	}

	@Override
	public Map<PipeDirection, Integer> handleArrivalAtMiddle(PipeItem item, PipeDirection before, Collection<PipeDirection> possibleDirs) {
		return null;
	}

	@Override
	public PipeType getPipeType() {
		return PipeType.VOID;
	}
	
	@Override
	public int[] getBreakParticleData() {
		return new int[] { 49, 0 };
	}

	@Override
	public List<ItemStack> getDroppedItems() {
		List<ItemStack> is = new ArrayList<>();
		is.add(PipeItemUtils.getPipeItem(getPipeType(), null));
		return is;
	}

}
