package de.robotricker.transportpipes.pipes;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.TransportPipes;
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
	public PipeType getPipeType() {
		return PipeType.ICE;
	}

	@Override
	public List<ItemStack> getDroppedItems() {
		List<ItemStack> is = new ArrayList<ItemStack>();
		is.add(TransportPipes.instance.getIcePipeItem());
		return is;
	}

	@Override
	protected float getPipeItemSpeed() {
		return ICE_ITEM_SPEED;
	}

}
