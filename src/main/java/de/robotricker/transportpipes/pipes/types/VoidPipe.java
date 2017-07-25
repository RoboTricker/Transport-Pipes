package de.robotricker.transportpipes.pipes.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipes.PipeDirection;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipeutils.PipeItemUtils;

public class VoidPipe extends Pipe {

	public VoidPipe(Location blockLoc) {
		super(blockLoc);
	}

	@Override
	public PipeDirection calculateNextItemDirection(PipeItem item, PipeDirection before, Collection<PipeDirection> possibleDirs) {
		removePipeItem(item);
		TransportPipes.pipePacketManager.destroyPipeItem(item);
		return null;
	}

	@Override
	public PipeType getPipeType() {
		return PipeType.VOID;
	}

	@Override
	public List<ItemStack> getDroppedItems() {
		List<ItemStack> is = new ArrayList<>();
		is.add(PipeItemUtils.getPipeItem(getPipeType(), null));
		return is;
	}

}
