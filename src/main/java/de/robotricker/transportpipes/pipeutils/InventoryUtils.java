package de.robotricker.transportpipes.pipeutils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Dropper;
import org.bukkit.block.Furnace;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.pipes.GoldenPipe;
import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.protocol.ReflectionManager;

public class InventoryUtils {

	//puts the item in this inventory from the specific side and returns the overflow items
	public static List<ItemStack> putItemInInventoryHolder(InventoryHolder ih, ItemStack item, PipeDirection side) {
		List<ItemStack> overflow = new ArrayList<>();

		if (ih instanceof Chest) {
			Chest i = (Chest) ih;
			overflow.addAll(i.getInventory().addItem(item).values());
		} else if (ih instanceof DoubleChest) {
			DoubleChest i = (DoubleChest) ih;
			overflow.addAll(i.getInventory().addItem(item).values());
		} else if (ih instanceof Dispenser) {
			Dispenser i = (Dispenser) ih;
			overflow.addAll(i.getInventory().addItem(item).values());
		} else if (ih instanceof Dropper) {
			Dropper i = (Dropper) ih;
			overflow.addAll(i.getInventory().addItem(item).values());
		} else if (ih instanceof Hopper) {
			Hopper i = (Hopper) ih;
			overflow.addAll(i.getInventory().addItem(item).values());
		} else if (ih instanceof BrewingStand) {
			BrewingStand i = (BrewingStand) ih;
			BrewerInventory bi = i.getInventory();

			if (item.getType() == Material.POTION || item.getType() == Material.SPLASH_POTION || item.getType() == Material.LINGERING_POTION) {
				if (bi.getItem(0) == null) {
					bi.setItem(0, item);
				} else if (bi.getItem(1) == null) {
					bi.setItem(1, item);
				} else if (bi.getItem(2) == null) {
					bi.setItem(2, item);
				} else {
					overflow.add(item);
				}
			} else if (side.isSide() && item.getType() == Material.BLAZE_POWDER) {

				bi.setFuel(putItemInSlot(item, bi.getFuel(), overflow));

			} else if (isBrewingIngredient(item)) {

				bi.setIngredient(putItemInSlot(item, bi.getIngredient(), overflow));

			} else {
				overflow.add(item);
			}
		} else if (ih instanceof Furnace) {
			Furnace i = (Furnace) ih;
			FurnaceInventory fi = i.getInventory();

			if (ReflectionManager.isFurnaceBurnableItem(item)) {
				if (side.isSide() || side == PipeDirection.UP) {
					fi.setSmelting(putItemInSlot(item, fi.getSmelting(), overflow));
				} else if (ReflectionManager.isFurnaceFuelItem(item)) {
					fi.setFuel(putItemInSlot(item, fi.getFuel(), overflow));
				}
			} else if (ReflectionManager.isFurnaceFuelItem(item)) {
				fi.setFuel(putItemInSlot(item, fi.getFuel(), overflow));
			} else {
				overflow.add(item);
			}
		} else if (ih instanceof org.bukkit.block.ShulkerBox) {
			org.bukkit.block.ShulkerBox sb = (org.bukkit.block.ShulkerBox) ih;
			overflow.addAll(sb.getInventory().addItem(item).values());
		}

		return overflow;

	}

	public static ItemStack takeItemFromInventoryHolder(InventoryHolder ih, Pipe pipe, PipeDirection direction) {

		ItemStack taken = null;
		GoldenPipe gp = null;
		if (pipe instanceof GoldenPipe) {
			gp = (GoldenPipe) pipe;
		}

		if (ih instanceof Chest) {
			Chest i = (Chest) ih;
			for (int x = i.getInventory().getSize() - 1; x >= 0; x--) {
				if (i.getInventory().getItem(x) != null) {
					//only take item if this item wouldn't go back immediatly because of a golden-pipe (in this case this item is skipped)
					ItemData id = new ItemData(i.getInventory().getItem(x));
					List<PipeDirection> possibleDirections = gp != null ? gp.getPossibleDirectionsForItem(id, direction) : null;
					if (gp == null || !(possibleDirections.size() == 1 && possibleDirections.get(0).equals(direction.getOpposite()))) {
						taken = createOneAmountItemStack(i.getInventory().getItem(x));
						i.getInventory().setItem(x, decreaseAmountWithOne(i.getInventory().getItem(x)));
						break;
					}
				}
			}
		} else if (ih instanceof DoubleChest) {
			DoubleChest i = (DoubleChest) ih;
			for (int x = i.getInventory().getSize() - 1; x >= 0; x--) {
				if (i.getInventory().getItem(x) != null) {
					//only take item if this item wouldn't go back immediatly because of a golden-pipe (in this case this item is skipped)
					ItemData id = new ItemData(i.getInventory().getItem(x));
					List<PipeDirection> possibleDirections = gp != null ? gp.getPossibleDirectionsForItem(id, direction) : null;
					if (gp == null || !(possibleDirections.size() == 1 && possibleDirections.get(0).equals(direction.getOpposite()))) {
						taken = createOneAmountItemStack(i.getInventory().getItem(x));
						i.getInventory().setItem(x, decreaseAmountWithOne(i.getInventory().getItem(x)));
						break;
					}
				}
			}
		} else if (ih instanceof Dispenser) {
			Dispenser i = (Dispenser) ih;
			for (int x = i.getInventory().getSize() - 1; x >= 0; x--) {
				if (i.getInventory().getItem(x) != null) {
					//only take item if this item wouldn't go back immediatly because of a golden-pipe (in this case this item is skipped)
					ItemData id = new ItemData(i.getInventory().getItem(x));
					List<PipeDirection> possibleDirections = gp != null ? gp.getPossibleDirectionsForItem(id, direction) : null;
					if (gp == null || !(possibleDirections.size() == 1 && possibleDirections.get(0).equals(direction.getOpposite()))) {
						taken = createOneAmountItemStack(i.getInventory().getItem(x));
						i.getInventory().setItem(x, decreaseAmountWithOne(i.getInventory().getItem(x)));
						break;
					}
				}
			}
		} else if (ih instanceof Dropper) {
			Dropper i = (Dropper) ih;
			for (int x = i.getInventory().getSize() - 1; x >= 0; x--) {
				if (i.getInventory().getItem(x) != null) {
					//only take item if this item wouldn't go back immediatly because of a golden-pipe (in this case this item is skipped)
					ItemData id = new ItemData(i.getInventory().getItem(x));
					List<PipeDirection> possibleDirections = gp != null ? gp.getPossibleDirectionsForItem(id, direction) : null;
					if (gp == null || !(possibleDirections.size() == 1 && possibleDirections.get(0).equals(direction.getOpposite()))) {
						taken = createOneAmountItemStack(i.getInventory().getItem(x));
						i.getInventory().setItem(x, decreaseAmountWithOne(i.getInventory().getItem(x)));
						break;
					}
				}
			}
		} else if (ih instanceof Hopper) {
			Hopper i = (Hopper) ih;
			for (int x = i.getInventory().getSize() - 1; x >= 0; x--) {
				if (i.getInventory().getItem(x) != null) {
					//only take item if this item wouldn't go back immediatly because of a golden-pipe (in this case this item is skipped)
					ItemData id = new ItemData(i.getInventory().getItem(x));
					List<PipeDirection> possibleDirections = gp != null ? gp.getPossibleDirectionsForItem(id, direction) : null;
					if (gp == null || !(possibleDirections.size() == 1 && possibleDirections.get(0).equals(direction.getOpposite()))) {
						taken = createOneAmountItemStack(i.getInventory().getItem(x));
						i.getInventory().setItem(x, decreaseAmountWithOne(i.getInventory().getItem(x)));
						break;
					}
				}
			}
		} else if (ih instanceof BrewingStand) {
			BrewingStand i = (BrewingStand) ih;
			BrewerInventory bi = i.getInventory();

			if (bi.getItem(0) != null) {
				taken = bi.getItem(0);
				bi.setItem(0, null);
			} else if (bi.getItem(1) != null) {
				taken = bi.getItem(1);
				bi.setItem(1, null);
			} else if (bi.getItem(2) != null) {
				taken = bi.getItem(2);
				bi.setItem(2, null);
			}
		} else if (ih instanceof Furnace) {
			Furnace i = (Furnace) ih;
			FurnaceInventory fi = i.getInventory();

			if (fi.getResult() != null) {
				taken = createOneAmountItemStack(fi.getResult());
				fi.setResult(decreaseAmountWithOne(fi.getResult()));
			}
		} else if (ih instanceof org.bukkit.block.ShulkerBox) {
			org.bukkit.block.ShulkerBox i = (org.bukkit.block.ShulkerBox) ih;
			for (int x = i.getInventory().getSize() - 1; x >= 0; x--) {
				if (i.getInventory().getItem(x) != null) {
					//only take item if this item wouldn't go back immediatly because of a golden-pipe (in this case this item is skipped)
					ItemData id = new ItemData(i.getInventory().getItem(x));
					List<PipeDirection> possibleDirections = gp != null ? gp.getPossibleDirectionsForItem(id, direction) : null;
					if (gp == null || !(possibleDirections.size() == 1 && possibleDirections.get(0).equals(direction.getOpposite()))) {
						taken = createOneAmountItemStack(i.getInventory().getItem(x));
						i.getInventory().setItem(x, decreaseAmountWithOne(i.getInventory().getItem(x)));
						break;
					}
				}
			}
		}
		return taken;
	}

	public static boolean isBrewingIngredient(ItemStack item) {
		if (item.getType() == Material.NETHER_STALK) {
			return true;
		}
		if (item.getType() == Material.SPECKLED_MELON) {
			return true;
		}
		if (item.getType() == Material.GHAST_TEAR) {
			return true;
		}
		if (item.getType() == Material.RABBIT_FOOT) {
			return true;
		}
		if (item.getType() == Material.BLAZE_POWDER) {
			return true;
		}
		if (item.getType() == Material.SPIDER_EYE) {
			return true;
		}
		if (item.getType() == Material.SUGAR) {
			return true;
		}
		if (item.getType() == Material.MAGMA_CREAM) {
			return true;
		}
		if (item.getType() == Material.GLOWSTONE_DUST) {
			return true;
		}
		if (item.getType() == Material.REDSTONE) {
			return true;
		}
		if (item.getType() == Material.FERMENTED_SPIDER_EYE) {
			return true;
		}
		if (item.getType() == Material.GOLDEN_CARROT) {
			return true;
		}
		if (item.getType() == Material.RAW_FISH && item.getData().getData() == 3) {
			return true;
		}
		if (item.getType() == Material.SULPHUR) {
			return true;
		}
		return false;
	}

	//returns the item to put in slot
	private static ItemStack putItemInSlot(ItemStack toPut, ItemStack inSlot, List<ItemStack> overflowList) {
		//only put item in there if the same item is already inside or nothing is inside
		if (inSlot == null) {
			return toPut;
		}
		if (inSlot.isSimilar(toPut)) {
			ItemStack newFuelItem = toPut.clone();
			ItemStack overflowItem = toPut.clone();

			newFuelItem.setAmount(Math.min(inSlot.getAmount() + toPut.getAmount(), toPut.getMaxStackSize()));

			if (inSlot.getAmount() + toPut.getAmount() - toPut.getMaxStackSize() > 0) {
				overflowItem.setAmount(inSlot.getAmount() + toPut.getAmount() - toPut.getMaxStackSize());
				overflowList.add(overflowItem);
			}
			return newFuelItem;
		} else {
			overflowList.add(toPut);
		}
		return inSlot;
	}

	private static ItemStack decreaseAmountWithOne(ItemStack item) {
		ItemStack copy = item.clone();
		if (item.getAmount() > 1) {
			copy.setAmount(item.getAmount() - 1);
		} else {
			copy = null;
		}
		return copy;
	}

	private static ItemStack createOneAmountItemStack(ItemStack item) {
		ItemStack copy = item.clone();
		copy.setAmount(1);
		return copy;
	}

}
