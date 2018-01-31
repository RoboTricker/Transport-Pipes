package de.robotricker.transportpipes.duct.pipe;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.wrappers.WrappedIntHashMap;
import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.IntTag;
import com.flowpowered.nbt.Tag;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctType;
import de.robotricker.transportpipes.duct.pipe.utils.ItemDistribution;
import de.robotricker.transportpipes.duct.pipe.utils.PipeType;
import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipeitems.RelLoc;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.hitbox.TimingCloseable;
import de.robotricker.transportpipes.utils.staticutils.DuctUtils;
import de.robotricker.transportpipes.utils.staticutils.InventoryUtils;
import de.robotricker.transportpipes.utils.staticutils.NBTUtils;
import de.robotricker.transportpipes.utils.staticutils.UpdateUtils;
import de.robotricker.transportpipes.utils.tick.PipeTickData;
import de.robotricker.transportpipes.utils.tick.TickData;
import io.sentry.Sentry;

public abstract class Pipe extends Duct {

	public static final float ITEM_SPEED = 0.125f;// 0.0625f;
	public static final float ICE_ITEM_SPEED = 0.5f;
	// calculate the amount of digits in 10^digits to shift all floats
	public static final long FLOAT_PRECISION = (long) (Math.pow(10, Math.max(Float.toString(ITEM_SPEED).split("\\.")[1].length(), Float.toString(ICE_ITEM_SPEED).split("\\.")[1].length())));

	// contains all items managed by this pipe
	public final Map<PipeItem, WrappedDirection> pipeItems = Collections.synchronizedMap(new HashMap<PipeItem, WrappedDirection>());

	// here are pipes saved that should be put in "pipeItems" in the next tick and
	// should NOT be spawned to the players (they are already spawned)
	// remember to synchronize while iterating
	public final Map<PipeItem, WrappedDirection> tempPipeItems = Collections.synchronizedMap(new HashMap<PipeItem, WrappedDirection>());

	// here are pipes saved that should be put in "pipeItems" in the next tick and
	// should be spawned to the players
	// remember to synchronize while iterating
	public final Map<PipeItem, WrappedDirection> tempPipeItemsWithSpawn = Collections.synchronizedMap(new HashMap<PipeItem, WrappedDirection>());

	protected ItemDistribution itemDistribution;

	public Pipe(Location blockLoc) {
		super(blockLoc);
		this.itemDistribution = new ItemDistribution(this);
	}

	public ItemDistribution getItemDistribution() {
		return itemDistribution;
	}

	/**
	 * puts an item into the pipe, so it will be handled in each tick
	 */
	public void putPipeItem(PipeItem item, WrappedDirection dir) {
		item.setBlockLoc(blockLoc);
		pipeItems.put(item, dir);
	}

	/**
	 * gets the PipeItem direction in this pipe
	 */
	public WrappedDirection getPipeItemDirection(PipeItem item) {
		return pipeItems.get(item);
	}

	/**
	 * removes the PipeItem from this pipe. This should be called whenever the item
	 * leaves the pipe (for all ItemHandling cases)
	 */
	public void removePipeItem(PipeItem item) {
		pipeItems.remove(item);
	}

	@Override
	public void tick(TickData tickData) {

		try {

			PipeTickData pipeTickData = (PipeTickData) tickData;
			// insert items from "tempPipeItemsWithSpawn"
			synchronized (tempPipeItemsWithSpawn) {
				Iterator<PipeItem> itemIterator = tempPipeItemsWithSpawn.keySet().iterator();
				while (itemIterator.hasNext()) {
					PipeItem pipeItem = itemIterator.next();

					WrappedDirection dir = tempPipeItemsWithSpawn.get(pipeItem);
					putPipeItem(pipeItem, dir);

					TransportPipes.instance.ductManager.createPipeItem(pipeItem);

					itemIterator.remove();
				}
			}
			// put the "tempPipeItems" which had been put there by the tick method in the
			// pipe before, into the "pipeItems" where they got affected by the tick method
			synchronized (tempPipeItems) {
				Iterator<PipeItem> itemIterator = tempPipeItems.keySet().iterator();
				while (itemIterator.hasNext()) {
					PipeItem pipeItem = itemIterator.next();

					// only put them there if they got into "tempPipeItems" last tick
					if (!pipeTickData.itemsTicked.contains(pipeItem)) {
						WrappedDirection dir = tempPipeItems.get(pipeItem);
						putPipeItem(pipeItem, dir);
						itemIterator.remove();
					}
				}
			}
			List<WrappedDirection> pipeConnections = getOnlyConnectableDuctConnections();
			List<WrappedDirection> blockConnections = getOnlyBlockConnections();
			if (pipeTickData.extractItems && this instanceof ExtractionPipe) {
				// extract items from inventories
				((ExtractionPipe) this).extractItems(blockConnections);
			}
			// handle item transport through pipe
			transportItems(pipeConnections, blockConnections, pipeTickData.itemsTicked);

			if (pipeItems.size() > TransportPipes.instance.generalConf.getMaxItemsPerPipe()) {
				explode(true, true);
			}

		} catch (Exception exception) {
			exception.printStackTrace();
			Sentry.capture(exception);
		}

	}

	/**
	 * there are three return states:
	 * <ol>
	 * <li>the map is <b>null</b>: the item will silently disappear</li>
	 * <li>the map is <b>empty</b>: the item will drop</li>
	 * <li>the map has <b>at least one entry</b>: the item will split to the given
	 * directions</li>
	 * </ol>
	 */
	public abstract Map<WrappedDirection, Integer> handleArrivalAtMiddle(PipeItem item, WrappedDirection before, Collection<WrappedDirection> possibleDirs);

	private void transportItems(List<WrappedDirection> pipeConnections, List<WrappedDirection> blockConnections, List<PipeItem> itemsTicked) {
		HashMap<PipeItem, WrappedDirection> itemsMap;
		synchronized (pipeItems) {
			itemsMap = new HashMap<>(pipeItems);
		}
		for (final PipeItem item : itemsMap.keySet()) {
			WrappedDirection itemDir = itemsMap.get(item);

			// if the item arrives at the middle of the pipe
			if (item.relLoc().getFloatX() == 0.5f && item.relLoc().getFloatY() == 0.5f && item.relLoc().getFloatZ() == 0.5f) {
				Set<WrappedDirection> clonedAllConnections = new HashSet<>();
				clonedAllConnections.addAll(pipeConnections);
				clonedAllConnections.addAll(blockConnections);

				Map<WrappedDirection, Integer> splittedItemsMap = handleArrivalAtMiddle(item, itemDir, clonedAllConnections);

				if (splittedItemsMap == null || splittedItemsMap.isEmpty()) {
					fullyRemovePipeItemFromSystem(item, splittedItemsMap != null);
					continue;
				}

				final ItemStack itemStack = item.getItem().clone();
				final RelLoc relLoc = RelLoc.fromString(item.relLoc().toString());

				PipeItem lastPipeItem = null;
				for (WrappedDirection pd : splittedItemsMap.keySet()) {
					int newAmount = splittedItemsMap.get(pd);

					if (lastPipeItem == null) {
						lastPipeItem = item;
					} else {
						lastPipeItem = new PipeItem(itemStack.clone(), blockLoc, pd);
					}
					lastPipeItem.getItem().setAmount(newAmount);
					lastPipeItem.relLoc().set(relLoc.getLongX(), relLoc.getLongY(), relLoc.getLongZ());

					pipeItems.put(lastPipeItem, pd);
					if (!lastPipeItem.equals(item)) {
						TransportPipes.instance.ductManager.createPipeItem(lastPipeItem);
					}
					moveItem(lastPipeItem, pipeConnections, blockConnections);
					// specifies that this item isn't handled inside another pipe in this tick if it
					// moves in the tempPipeItems in this tick
					itemsTicked.add(lastPipeItem);
				}

				continue;
			}

			moveItem(item, pipeConnections, blockConnections);

			// specifies that this item isn't handled inside another pipe in this tick if it
			// moves in the tempPipeItems in this tick
			itemsTicked.add(item);

		}
	}

	private void moveItem(final PipeItem item, List<WrappedDirection> pipeConnections, List<WrappedDirection> blockConnections) {
		if (!pipeItems.containsKey(item)) {
			return;
		}
		RelLoc relLoc = item.relLoc();
		WrappedDirection itemDir = pipeItems.get(item);
		float xSpeed = itemDir.getX() * getPipeItemSpeed();
		float ySpeed = itemDir.getY() * getPipeItemSpeed();
		float zSpeed = itemDir.getZ() * getPipeItemSpeed();
		// update pipeItemSpeed (for pipe updating packets)
		item.relLocDerivation().set(xSpeed, ySpeed, zSpeed);
		// update the real item location
		relLoc.addValues(xSpeed, ySpeed, zSpeed);

		// update
		TransportPipes.instance.ductManager.updatePipeItem(item);

		// if the item is at the end of the transportation
		if (relLoc.getFloatX() == 1.0f || relLoc.getFloatY() == 1.0f || relLoc.getFloatZ() == 1.0f || relLoc.getFloatX() == 0.0f || relLoc.getFloatY() == 0.0f || relLoc.getFloatZ() == 0.0f) {

			final Location newBlockLoc = blockLoc.clone().add(itemDir.getX(), itemDir.getY(), itemDir.getZ());
			ItemHandling itemHandling = ItemHandling.NOTHING;

			// ITEM HANDLING CALC
			{
				if (itemHandling == ItemHandling.NOTHING) {
					if (blockConnections.contains(itemDir)) {

						final ItemStack itemStack = item.getItem();
						final WrappedDirection finalDir = itemDir;
						TransportPipes.runTask(new Runnable() {

							@Override
							public void run() {
								try (TimingCloseable tc = new TimingCloseable("output item scheduler")) {

									fullyRemovePipeItemFromSystem(item, false);

									BlockLoc bl = BlockLoc.convertBlockLoc(newBlockLoc);

									Map<BlockLoc, TransportPipesContainer> containerMap = TransportPipes.instance.getContainerMap(newBlockLoc.getWorld());
									if (containerMap == null || !containerMap.containsKey(bl)) {
										// drop the item in case the inventory block is registered but is no longer in
										// the world
										newBlockLoc.getWorld().dropItem(newBlockLoc.clone().add(0.5d, 0.5d, 0.5d), itemStack);
									} else {
										ItemStack overflow = containerMap.get(bl).insertItem(finalDir.getOpposite(), itemStack);
										if (overflow != null) {
											// move overflow items in opposite direction
											WrappedDirection newItemDir = finalDir.getOpposite();
											PipeItem pi = new PipeItem(overflow, Pipe.this.blockLoc, newItemDir);
											tempPipeItemsWithSpawn.put(pi, newItemDir);
										}
									}
								} catch (Exception ignored) {
									ignored.printStackTrace();
								}
							}

						});
						itemHandling = ItemHandling.OUTPUT_TO_INVENTORY;
					}
				}

				if (itemHandling == ItemHandling.NOTHING) {
					if (pipeConnections.contains(itemDir)) {
						Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(newBlockLoc.getWorld());
						if (ductMap != null) {
							BlockLoc newBlockLocLong = BlockLoc.convertBlockLoc(newBlockLoc);
							if (ductMap.containsKey(newBlockLocLong)) {
								Duct newDuct = ductMap.get(newBlockLocLong);
								if (newDuct.getDuctType() == DuctType.PIPE) {

									removePipeItem(item);
									// switch relLoc values from 0 to 1 and vice-versa
									item.relLoc().switchValues();

									// put item in next pipe
									((Pipe) newDuct).tempPipeItems.put(item, itemDir);
									itemHandling = ItemHandling.OUTPUT_TO_NEXT_PIPE;
								}
							}
						}
					}
				}

				if (itemHandling == ItemHandling.NOTHING) {
					fullyRemovePipeItemFromSystem(item, true);
					itemHandling = ItemHandling.DROP;
				}
			}
			// END ITEM HANDLING CALC

		}
	}

	protected float getPipeItemSpeed() {
		return ITEM_SPEED;
	}

	/**
	 * Determines wether the item should drop, be put in another pipe or be put in
	 * an inventory when it reaches the end of a pipe
	 */
	private enum ItemHandling {
		NOTHING(),
		DROP(),
		OUTPUT_TO_INVENTORY(),
		OUTPUT_TO_NEXT_PIPE()
	}

	@Override
	public void saveToNBTTag(CompoundMap tags) {
		super.saveToNBTTag(tags);

		List<Tag<?>> itemList = new ArrayList<>();
		Map<PipeItem, WrappedDirection> pipeItemMap = new HashMap<>();
		pipeItemMap.putAll(pipeItems);
		pipeItemMap.putAll(tempPipeItems);
		pipeItemMap.putAll(tempPipeItemsWithSpawn);
		for (PipeItem pipeItem : pipeItemMap.keySet()) {
			CompoundMap itemMap = new CompoundMap();

			NBTUtils.saveStringValue(itemMap, "RelLoc", pipeItem.relLoc().toString());
			NBTUtils.saveIntValue(itemMap, "Direction", pipeItemMap.get(pipeItem).getId());
			NBTUtils.saveStringValue(itemMap, "Item", InventoryUtils.ItemStackToString(pipeItem.getItem()));

			itemList.add(new CompoundTag("PipeItem", itemMap));
		}
		NBTUtils.saveListValue(tags, "PipeItems", CompoundTag.class, itemList);

	}

	@Override
	public void loadFromNBTTag(CompoundTag tag, long datVersion) {
		super.loadFromNBTTag(tag, datVersion);

		CompoundMap compoundValues = tag.getValue();

		List<Tag<?>> pipeItems = NBTUtils.readListTag(compoundValues.get("PipeItems"));
		for (Tag<?> itemTag : pipeItems) {
			CompoundTag itemCompoundTag = (CompoundTag) itemTag;
			CompoundMap itemMap = itemCompoundTag.getValue();

			RelLoc relLoc = RelLoc.fromString(NBTUtils.readStringTag(itemMap.get("RelLoc"), null));
			WrappedDirection dir = WrappedDirection.fromID(NBTUtils.readIntTag(itemMap.get("Direction"), 0));
			ItemStack itemStack = InventoryUtils.StringToItemStack(NBTUtils.readStringTag(itemMap.get("Item"), null));

			if (itemStack != null) {
				PipeItem newPipeItem = new PipeItem(itemStack, this.blockLoc, dir);
				newPipeItem.relLoc().set(relLoc.getFloatX(), relLoc.getFloatY(), relLoc.getFloatZ());
				tempPipeItemsWithSpawn.put(newPipeItem, dir);
			}
		}

	}

	public abstract PipeType getPipeType();

	protected List<PipeItem> splitPipeItem(PipeItem toSplit, int... newAmounts) {
		List<PipeItem> pipeItems = new ArrayList<PipeItem>();
		return pipeItems;
	}

	protected void fullyRemovePipeItemFromSystem(final PipeItem item, boolean dropItem) {
		removePipeItem(item);
		TransportPipes.instance.ductManager.destroyPipeItem(item);

		if (dropItem) {
			final ItemStack itemStack = item.getItem();
			TransportPipes.runTask(new Runnable() {

				@Override
				public void run() {
					item.getBlockLoc().getWorld().dropItem(item.getBlockLoc().clone().add(0.5d, 0.5d, 0.5d), itemStack);
				}
			});
		}
	}

	public int getSimilarItemAmountOnDirectionWay(PipeItem toCompare, WrappedDirection direction) {
		int amount = 0;
		synchronized (pipeItems) {
			for (PipeItem pi : pipeItems.keySet()) {
				WrappedDirection pd = pipeItems.get(pi);
				if (pi.getItem().isSimilar(toCompare.getItem()) && !pi.equals(toCompare) && pd.equals(direction)) {
					if (pd == WrappedDirection.NORTH && pi.relLoc().getFloatZ() < 0.5f) {

					} else if (pd == WrappedDirection.SOUTH && pi.relLoc().getFloatZ() > 0.5f) {

					} else if (pd == WrappedDirection.EAST && pi.relLoc().getFloatX() > 0.5f) {

					} else if (pd == WrappedDirection.WEST && pi.relLoc().getFloatX() < 0.5f) {

					} else if (pd == WrappedDirection.UP && pi.relLoc().getFloatY() > 0.5f) {

					} else if (pd == WrappedDirection.DOWN && pi.relLoc().getFloatY() < 0.5f) {

					} else {
						continue;
					}
					amount += pi.getItem().getAmount();
				}
			}
		}
		return amount;
	}

	@Override
	public boolean canConnectToDuct(Duct duct) {
		if (duct instanceof Pipe) {
			Pipe neighborPipe = (Pipe) duct;
			if (neighborPipe.getPipeType() == PipeType.EXTRACTION && getPipeType() == PipeType.EXTRACTION) {
				return false;
			}
			if (neighborPipe.getPipeType() == PipeType.VOID && getPipeType() == PipeType.VOID) {
				return false;
			}
			if (neighborPipe.getPipeType() == PipeType.COLORED && getPipeType() == PipeType.COLORED) {
				if (!((ColoredPipe) neighborPipe).getPipeColor().equals(((ColoredPipe) this).getPipeColor())) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public List<WrappedDirection> getOnlyBlockConnections() {
		List<WrappedDirection> dirs = new ArrayList<>();

		Map<BlockLoc, TransportPipesContainer> containerMap = TransportPipes.instance.getContainerMap(getBlockLoc().getWorld());

		if (containerMap != null) {
			for (WrappedDirection dir : WrappedDirection.values()) {
				Location blockLoc = getBlockLoc().clone().add(dir.getX(), dir.getY(), dir.getZ());
				BlockLoc bl = BlockLoc.convertBlockLoc(blockLoc);
				if (containerMap.containsKey(bl)) {
					dirs.add(dir);
				}
			}
		}

		return dirs;
	}

	@Override
	public DuctType getDuctType() {
		return DuctType.PIPE;
	}

}
