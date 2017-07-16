package de.robotricker.transportpipes.container;

import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.inventory.BrewerInventory;

import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.pipes.PipeDirection;

public class BrewingStandContainer extends BlockContainer {

	private BrewingStand brewingStand;

	public BrewingStandContainer(Block block) {
		this.brewingStand = (BrewingStand) block.getState();
	}

	@Override
	public ItemData extractItem(PipeDirection extractDirection) {
		BrewerInventory inv = brewingStand.getInventory();
		return null;
	}

	@Override
	public boolean insertItem(PipeDirection insertDirection, ItemData insertion) {
		BrewerInventory inv = brewingStand.getInventory();
		return true;
	}

}
