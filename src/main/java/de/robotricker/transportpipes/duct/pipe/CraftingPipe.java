package de.robotricker.transportpipes.duct.pipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.duct.ClickableDuct;
import de.robotricker.transportpipes.duct.DuctInv;
import de.robotricker.transportpipes.duct.InventoryDuct;
import de.robotricker.transportpipes.duct.pipe.craftingpipe.CraftingPipeProcessInv;
import de.robotricker.transportpipes.duct.pipe.craftingpipe.CraftingPipeRecipeInv;
import de.robotricker.transportpipes.duct.pipe.utils.PipeType;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.ductdetails.DuctDetails;
import de.robotricker.transportpipes.utils.ductdetails.PipeDetails;
import de.robotricker.transportpipes.utils.staticutils.DuctItemUtils;
import de.robotricker.transportpipes.utils.tick.TickData;

public class CraftingPipe extends Pipe implements ClickableDuct, InventoryDuct {

	//ignoring amount
	private ItemStack[] recipeItems;
	private ItemStack[] processItems;

	private CraftingPipeRecipeInv recipeInventory;
	private CraftingPipeProcessInv processInventory;

	public CraftingPipe(Location blockLoc) {
		super(blockLoc);
		this.recipeItems = new ItemStack[9];
		this.processItems = new ItemStack[9];

		this.recipeInventory = new CraftingPipeRecipeInv(this);
		this.processInventory = new CraftingPipeProcessInv(this);
	}

	/**
	 * @return overflow
	 */
	public ItemStack addProcessItem(ItemStack item) {
		for (int i = 0; i < processItems.length; i++) {
			ItemStack processItemBefore = processItems[i];
			if (processItemBefore == null) {
				processItems[i] = item;
				updateProcessInv();
				return null;
			} else {
				if (!processItemBefore.isSimilar(item)) {
					continue;
				}
				int amountBefore = processItemBefore.getAmount();
				int amountItem = item.getAmount();
				int amountMax = item.getMaxStackSize();
				int delta = Math.min(amountMax, amountBefore + amountItem) - amountBefore;
				processItems[i].setAmount(amountBefore + delta);
				updateProcessInv();
				if (delta < amountItem) {
					item.setAmount(amountItem - delta);
				} else {
					return null;
				}
			}
		}
		return item;
	}

	private void updateProcessInv() {
		for (int i = 0; i < processItems.length; i++) {
			if (processItems[i] != null) {
				processInventory.getInventory().setItem(i, processItems[i]);
			} else {
				processInventory.getInventory().setItem(i, null);
			}
		}
	}
	
	@Override
	public void tick(TickData tickData) {
		super.tick(tickData);
//		Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
//		while(recipeIterator.hasNext()) {
//			Recipe recipe = recipeIterator.next();
//		}
	}
	
	public ItemStack[] getRecipeItems() {
		return recipeItems;
	}

	@Override
	public Map<WrappedDirection, Integer> handleArrivalAtMiddle(final PipeItem item, WrappedDirection before, Collection<WrappedDirection> possibleDirs) {
		final ItemStack overflow = addProcessItem(item.getItem());
		if(overflow != null) {
			Bukkit.getScheduler().runTask(TransportPipes.instance, new Runnable() {
				
				@Override
				public void run() {
					item.getBlockLoc().getWorld().dropItem(item.getBlockLoc().clone().add(0.5, 0.5, 0.5), overflow);
				}
			});
		}
		return null;
	}

	@Override
	public int[] getBreakParticleData() {
		return new int[] { 58, 0 };
	}

	@Override
	public void click(Player p, WrappedDirection side) {
		getDuctInventory(p).openOrUpdateInventory(p);
	}

	@Override
	public DuctInv getDuctInventory(Player p) {
		if (p.isSneaking()) {
			return recipeInventory;
		} else {
			return processInventory;
		}
	}

	@Override
	public PipeType getPipeType() {
		return PipeType.CRAFTING;
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
