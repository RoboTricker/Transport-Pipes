package de.robotricker.transportpipes.duct.pipe.utils;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.pipe.CraftingPipe;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.WrappedDirection;

public class ItemDistribution {

	private Pipe pipe;
	private int distributionCounter;

	public ItemDistribution(Pipe pipe) {
		this.pipe = pipe;
		this.distributionCounter = 0;
	}

	/**
	 * reduce all abs weights by calculating the greatest common dividor and
	 * dividing each weight through it
	 */
	private void reduceAbsWeights(Map<WrappedDirection, Integer> absWeights) {
		if (absWeights == null) {
			return;
		}
		BigInteger gcd = null;
		for (WrappedDirection wd : absWeights.keySet()) {
			if (gcd == null) {
				gcd = BigInteger.valueOf(absWeights.get(wd));
			} else {
				gcd = BigInteger.valueOf(absWeights.get(wd)).gcd(gcd);
			}
		}
		for (WrappedDirection wd : absWeights.keySet()) {
			absWeights.put(wd, absWeights.get(wd) / gcd.intValue());
		}
	}

	/**
	 * this method maps the possible directions with their free space
	 */
	private Map<WrappedDirection, Integer> calculateFreeSpaceForAllDirections(ItemStack item, Collection<WrappedDirection> possibleDirections) {
		Map<WrappedDirection, Integer> freeSpaceMap = new HashMap<>();

		Map<BlockLoc, TransportPipesContainer> containerMap = TransportPipes.instance.getContainerMap(pipe.getBlockLoc().getWorld());
		Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(pipe.getBlockLoc().getWorld());

		for (WrappedDirection wd : possibleDirections) {
			freeSpaceMap.put(wd, Integer.MAX_VALUE);
			BlockLoc bl = BlockLoc.convertBlockLoc(pipe.getBlockLoc(), wd);
			if (containerMap != null && containerMap.containsKey(bl)) {
				// container at location
				TransportPipesContainer container = containerMap.get(bl);
				int freeSpace = container.howMuchSpaceForItemAsync(wd.getOpposite(), item);
				freeSpaceMap.put(wd, freeSpace);
			} else if (ductMap != null && ductMap.containsKey(bl) && ductMap.get(bl) instanceof CraftingPipe) {
				// crafting pipe at location
				CraftingPipe cp = (CraftingPipe) ductMap.get(bl);
				int freeSpace = cp.freeSpaceForItem(new ItemData(item));
				freeSpaceMap.put(wd, freeSpace);
			}
		}

		return freeSpaceMap;
	}

	/**
	 * 
	 * splits the given "item" based on the given "absWeights" into the
	 * "possibleDirections".
	 * 
	 * @param item
	 * @param possibleDirections
	 * @param absWeights
	 * @return map which maps each possible direction to the item amount so that the
	 *         total amount is equal to item.getAmount()
	 */
	public Map<WrappedDirection, Integer> splitPipeItem(ItemStack item, Collection<WrappedDirection> possibleDirections, Map<WrappedDirection, Integer> absWeights) {
		Map<WrappedDirection, Integer> splitMap = new HashMap<>();
		Map<WrappedDirection, Integer> freeSpaceMap = calculateFreeSpaceForAllDirections(item, possibleDirections);
		reduceAbsWeights(absWeights);

		// if no weights exist, set all weights to 1
		if (absWeights == null) {
			absWeights = new HashMap<>();
			for (WrappedDirection wd : possibleDirections) {
				absWeights.put(wd, 1);
			}
		}

		// convert the weight map to a direction list where a weight is equal to the
		// amount of directions
		List<WrappedDirection> weightsList = new LinkedList<>();
		for (WrappedDirection wd : absWeights.keySet()) {
			for (int i = 1; i <= absWeights.get(wd); i++) {
				// make sure that the max weight is equal to the free space of this direction
				if (freeSpaceMap.get(wd) >= i) {
					weightsList.add(wd);
				}
			}
		}

		if (weightsList.isEmpty()) {
			// return empty map so the item will be dropped
			// if null would be returned, no item will be dropped
			return splitMap;
		}

		for (int i = 0; i < item.getAmount(); i++) {
			distributionCounter %= weightsList.size();

			WrappedDirection outputDirection = weightsList.get(distributionCounter);
			if (splitMap.containsKey(outputDirection)) {
				splitMap.put(outputDirection, splitMap.get(outputDirection) + 1);
			} else {
				splitMap.put(outputDirection, 1);
			}

			distributionCounter++;
		}

		return splitMap;
	}

}
