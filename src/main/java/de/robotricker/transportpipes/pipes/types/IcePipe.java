package de.robotricker.transportpipes.pipes.types;

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
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipes.BlockLoc;
import de.robotricker.transportpipes.pipes.PipeDirection;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipeutils.PipeItemUtils;

public class IcePipe extends Pipe {

	private int lastOutputIndex = 0;

	public IcePipe(Location blockLoc) {
		super(blockLoc);
	}

	@Override
	public Map<PipeDirection, Integer> handleArrivalAtMiddle(PipeItem item, PipeDirection before, Collection<PipeDirection> possibleDirs) {
		Map<BlockLoc, TransportPipesContainer> containerMap = TransportPipes.instance.getContainerMap(blockLoc.getWorld());

		Map<PipeDirection, Integer> maxSpaceMap = new HashMap<PipeDirection, Integer>();
		Map<PipeDirection, Integer> map = new HashMap<PipeDirection, Integer>();

		//update maxSpaceMap
		for (PipeDirection pd : PipeDirection.values()) {
			maxSpaceMap.put(pd, Integer.MAX_VALUE);
			if (containerMap != null) {
				BlockLoc bl = BlockLoc.convertBlockLoc(blockLoc.clone().add(pd.getX(), pd.getY(), pd.getZ()));
				if (containerMap.containsKey(bl)) {
					TransportPipesContainer tpc = containerMap.get(bl);
					int freeSpace = tpc.howMuchSpaceForItemAsync(pd.getOpposite(), item.getItem());
					maxSpaceMap.put(pd, freeSpace);
				}
			}
		}

		for (int i = 0; i < item.getItem().getAmount(); i++) {
			PipeDirection nextDir = getNextItemDirection(item, before, new ArrayList<>(possibleDirs), map, maxSpaceMap);
			if (nextDir != null) {
				if (map.containsKey(nextDir)) {
					map.put(nextDir, map.get(nextDir) + 1);
				} else {
					map.put(nextDir, 1);
				}
			}
		}

		return map;
	}

	private PipeDirection getNextItemDirection(PipeItem item, PipeDirection before, Collection<PipeDirection> possibleDirs, Map<PipeDirection, Integer> outputMap, Map<PipeDirection, Integer> maxSpaceMap) {

		Iterator<PipeDirection> it = possibleDirs.iterator();
		while (it.hasNext()) {
			PipeDirection pd = it.next();
			if (pd.equals(before.getOpposite())) {
				it.remove();
			} else {
				int currentAmount = outputMap.containsKey(pd) ? outputMap.get(pd) : 0;
				currentAmount += getSimilarItemAmountOnDirectionWay(item, pd);
				int maxFreeSpace = maxSpaceMap.get(pd);
				if (currentAmount >= maxFreeSpace) {
					it.remove();
				}
			}
		}
		PipeDirection[] array = possibleDirs.toArray(new PipeDirection[0]);
		lastOutputIndex++;
		if (lastOutputIndex >= possibleDirs.size()) {
			lastOutputIndex = 0;
		}
		if (possibleDirs.size() > 0) {
			return array[lastOutputIndex];
		}
		return null;
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
		is.add(PipeItemUtils.getPipeItem(getPipeType(), null));
		return is;
	}

	@Override
	protected float getPipeItemSpeed() {
		return ICE_ITEM_SPEED;
	}

}
