package de.robotricker.transportpipes.duct.pipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.duct.pipe.utils.PipeType;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.ductdetails.DuctDetails;
import de.robotricker.transportpipes.utils.ductdetails.PipeDetails;
import de.robotricker.transportpipes.utils.staticutils.DuctItemUtils;

public class IcePipe extends Pipe {

	public IcePipe(Location blockLoc) {
		super(blockLoc);
	}

	@Override
	public Map<WrappedDirection, Integer> handleArrivalAtMiddle(PipeItem item, WrappedDirection before, Collection<WrappedDirection> possibleDirs) {
		Iterator<WrappedDirection> it = possibleDirs.iterator();
		while (it.hasNext()) {
			WrappedDirection pd = it.next();
			if (pd.equals(before.getOpposite())) {
				it.remove();
			}
		}
		return getItemDistribution().splitPipeItem(item.getItem(), possibleDirs, null);
	}

	@Override
	public PipeType getPipeType() {
		return PipeType.ICE;
	}

	@Override
	public int[] getBreakParticleData() {
		return new int[] { 79, 0 };
	}

	@Override
	public List<ItemStack> getDroppedItems() {
		List<ItemStack> is = new ArrayList<>();
		is.add(DuctItemUtils.getClonedDuctItem(new PipeDetails(getPipeType())));
		return is;
	}

	@Override
	protected float getPipeItemSpeed() {
		return ICE_ITEM_SPEED;
	}

	@Override
	public DuctDetails getDuctDetails() {
		return new PipeDetails(getPipeType());
	}
	
}
