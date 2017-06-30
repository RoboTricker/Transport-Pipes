package de.robotricker.transportpipes.pipes;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipeutils.PipeDirection;

public class IcePipe extends Pipe {

	public IcePipe(Location blockLoc) {
		super(blockLoc);
	}

	@Override
	public PipeDirection itemArrivedAtMiddle(PipeItem item, PipeDirection before, List<PipeDirection> dirs) {
		return null;
	}

	@Override
	public void destroy(boolean dropPipeItem) {

	}

	@Override
	public PipeType getPipeType() {
		return PipeType.ICE;
	}

	@Override
	protected List<ItemStack> getDroppedItems() {
		return null;
	}
	
	@Override
	protected float getPipeItemSpeed() {
		return ICE_ITEM_SPEED;
	}
	
}
