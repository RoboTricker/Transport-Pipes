package de.robotricker.transportpipes.pipes;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipeutils.PipeDirection;

public class IcePipe extends Pipe {

	private int lastOutputIndex = 0;

	public IcePipe(Location blockLoc) {
		super(blockLoc);
	}

	@Override
	public PipeDirection calculateNextItemDirection(PipeItem item, PipeDirection before, List<PipeDirection> possibleDirs) {
		if (possibleDirs.contains(before.getOpposite())) {
			possibleDirs.remove(before.getOpposite());
		}
		lastOutputIndex++;
		if (lastOutputIndex >= possibleDirs.size()) {
			lastOutputIndex = 0;
		}
		if (possibleDirs.size() > 0) {
			return possibleDirs.get(lastOutputIndex);
		}
		return before;
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
