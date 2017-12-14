package de.robotricker.transportpipes.pipes.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipes.WrappedDirection;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipeutils.DuctDetails;
import de.robotricker.transportpipes.pipeutils.DuctItemUtils;
import de.robotricker.transportpipes.pipeutils.PipeDetails;

public class VoidPipe extends Pipe {

	public VoidPipe(Location blockLoc) {
		super(blockLoc);
	}

	@Override
	public Map<WrappedDirection, Integer> handleArrivalAtMiddle(PipeItem item, WrappedDirection before, Collection<WrappedDirection> possibleDirs) {
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
		is.add(DuctItemUtils.getClonedDuctItem(new PipeDetails(getPipeType())));
		return is;
	}

	@Override
	public DuctDetails getDuctDetails() {
		return new PipeDetails(getPipeType());
	}

}
