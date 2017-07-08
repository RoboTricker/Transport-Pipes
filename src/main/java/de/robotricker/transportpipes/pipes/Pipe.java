package de.robotricker.transportpipes.pipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jnbt.CompoundTag;
import org.jnbt.IntTag;
import org.jnbt.NBTTagType;
import org.jnbt.Tag;

import de.robotricker.transportpipes.PipeThread;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.TransportPipes.BlockLoc;
import de.robotricker.transportpipes.api.PipeExplodeEvent;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipeutils.InventoryUtils;
import de.robotricker.transportpipes.pipeutils.NBTUtils;
import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.pipeutils.PipeUtils;
import de.robotricker.transportpipes.pipeutils.RelLoc;

public abstract class Pipe {

	private static int maxItemsPerPipe = 10;
	public static final float ITEM_SPEED = 0.125f;//0.0625f;
	public static final float ICE_ITEM_SPEED = 0.5f;
	//calculate the amount of digits in 10^digits to shift all floats 
	public static final long FLOAT_PRECISION = (long) (Math.pow(10, Math.max(Float.toString(ITEM_SPEED).split("\\.")[1].length(), Float.toString(ICE_ITEM_SPEED).split("\\.")[1].length())));

	protected static final ItemStack ITEM_BLAZE = new ItemStack(Material.BLAZE_ROD);
	protected static final ItemStack ITEM_GOLD_BLOCK = new ItemStack(Material.GOLD_BLOCK);
	protected static final ItemStack ITEM_IRON_BLOCK = new ItemStack(Material.IRON_BLOCK);
	protected static final ItemStack ITEM_CARPET_WHITE = new ItemStack(Material.CARPET, 1, (short) 0);
	protected static final ItemStack ITEM_CARPET_YELLOW = new ItemStack(Material.CARPET, 1, (short) 4);
	protected static final ItemStack ITEM_CARPET_GREEN = new ItemStack(Material.CARPET, 1, (short) 5);
	protected static final ItemStack ITEM_CARPET_BLUE = new ItemStack(Material.CARPET, 1, (short) 11);
	protected static final ItemStack ITEM_CARPET_RED = new ItemStack(Material.CARPET, 1, (short) 14);
	protected static final ItemStack ITEM_CARPET_BLACK = new ItemStack(Material.CARPET, 1, (short) 15);

	//contains all items managed by this pipe
	public HashMap<PipeItem, PipeDirection> pipeItems = new HashMap<PipeItem, PipeDirection>();

	//here are pipes saved that should be put in "pipeItems" in the next tick and should NOT be spawned to the players (they are already spawned)
	//remember to synchronize while iterating
	public final Map<PipeItem, PipeDirection> tempPipeItems = Collections.synchronizedMap(new HashMap<PipeItem, PipeDirection>());

	//here are pipes saved that should be put in "pipeItems" in the next tick and should be spawned to the players
	//remember to synchronize while iterating
	public final Map<PipeItem, PipeDirection> tempPipeItemsWithSpawn = Collections.synchronizedMap(new HashMap<PipeItem, PipeDirection>());

	//the blockLoc of this pipe
	public Location blockLoc;

	//contains all PipeDirections which refer to an inventory block
	//remember to synchronize while iterating
	public List<PipeDirection> pipeNeighborBlocks = Collections.synchronizedList(new ArrayList<PipeDirection>());

	static {
		try {
			maxItemsPerPipe = TransportPipes.instance.getConfig().getInt("max_items_per_pipe");
		} catch (Exception e) {

		}
	}

	public Pipe(Location blockLoc) {
		this.blockLoc = blockLoc;
	}

	/**
	 * checks if the neighbor block of this pipe in the direction "dir" is a block with an inventory (not a pipe)
	 */
	public boolean isPipeNeighborBlock(PipeDirection dir) {
		return pipeNeighborBlocks.contains(dir);
	}

	public Location getBlockLoc() {
		return blockLoc;
	}

	/**
	 * puts an item into the pipe, so it will be handled in each tick
	 */
	public void putPipeItem(PipeItem item, PipeDirection dir) {
		item.setBlockLoc(blockLoc);
		pipeItems.put(item, dir);
	}

	/**
	 * gets the PipeItem direction in this pipe
	 */
	public PipeDirection getPipeItemDirection(PipeItem item) {
		return pipeItems.getOrDefault(item, null);
	}

	/**
	 * removes the PipeItem from this pipe. This should be called whenever the item leaves the pipe (for all ItemHandling cases)
	 */
	public void removePipeItem(PipeItem item) {
		pipeItems.remove(item);
	}

	public void tick(boolean extractItems, List<PipeItem> itemsTicked) {

		if (extractItems) {
			//extract items from inventories
			extractItems();
		}

		//handle item transport through pipe
		transportItems(itemsTicked);

		//pipe explosion if too many items
		if (pipeItems.size() >= maxItemsPerPipe) {
			synchronized (this) {
				explode(true);
			}
		}

	}

	public void explode(boolean withSound) {
		PipeExplodeEvent pee = new PipeExplodeEvent(this);
		Bukkit.getPluginManager().callEvent(pee);
		if (!pee.isCancelled()) {
			PipeThread.runTask(new Runnable() {

				@Override
				public void run() {
					PipeUtils.destroyPipe(null, Pipe.this);
					if(withSound) {
						blockLoc.getWorld().playSound(blockLoc, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
					}
					blockLoc.getWorld().playEffect(blockLoc.clone().add(0.5d, 0.5d, 0.5d), Effect.SMOKE, 31);
				}
			}, 0);
		}
	}

	/**
	 * This method is be called for all items that arrive in the middle of the pipe to calculate the direction they should go next
	 */
	public abstract PipeDirection calculateNextItemDirection(PipeItem item, PipeDirection before, List<PipeDirection> possibleDirs);

	private void transportItems(List<PipeItem> itemsTicked) {

		List<PipeDirection> allConnections = getAllConnections();

		HashMap<PipeItem, PipeDirection> itemsMap = (HashMap<PipeItem, PipeDirection>) pipeItems.clone();
		for (final PipeItem item : itemsMap.keySet()) {
			PipeDirection itemDir = itemsMap.get(item);

			//if the item arrives at the middle of the pipe
			if (item.relLoc().getFloatX() == 0.5f && item.relLoc().getFloatY() == 0.5f && item.relLoc().getFloatZ() == 0.5f) {
				List<PipeDirection> clonedAllConnections = new ArrayList<PipeDirection>();
				clonedAllConnections.addAll(allConnections);

				itemDir = calculateNextItemDirection(item, itemDir, clonedAllConnections);
				pipeItems.put(item, itemDir);
			}

			RelLoc relLoc = item.relLoc();
			float xSpeed = itemDir.getX() * getPipeItemSpeed();
			float ySpeed = itemDir.getY() * getPipeItemSpeed();
			float zSpeed = itemDir.getZ() * getPipeItemSpeed();
			//update pipeItemSpeed (for pipe updating packets)
			item.relLocDerivation().set(xSpeed, ySpeed, zSpeed);
			//update the real item location
			relLoc.addValues(xSpeed, ySpeed, zSpeed);

			//update
			TransportPipes.pipePacketManager.updatePipeItem(item);

			//if the item is at the end of the transportation
			if (relLoc.getFloatX() == 1.0f || relLoc.getFloatY() == 1.0f || relLoc.getFloatZ() == 1.0f || relLoc.getFloatX() == 0.0f || relLoc.getFloatY() == 0.0f || relLoc.getFloatZ() == 0.0f) {
				removePipeItem(item);

				//switch relLoc values from 0 to 1 and vice-versa
				item.relLoc().switchValues();

				final Location newBlockLoc = blockLoc.clone().add(itemDir.getX(), itemDir.getY(), itemDir.getZ());
				ItemHandling itemHandling = ItemHandling.NOTHING;

				//ITEM HANDLING CALC
				{
					if (isPipeNeighborBlock(itemDir)) {
						TransportPipes.pipePacketManager.destroyPipeItem(item);

						final ItemStack itemStack = item.getItem();
						final PipeDirection finalDir = itemDir;
						Bukkit.getScheduler().runTask(TransportPipes.instance, new Runnable() {

							@Override
							public void run() {
								try {

									if (newBlockLoc.getBlock().getState() instanceof InventoryHolder) {
										InventoryHolder invH = (InventoryHolder) newBlockLoc.getBlock().getState();
										//put items in inv and put overflow items back in pipe
										for (ItemStack resultItem : InventoryUtils.putItemInInventoryHolder(invH, itemStack, finalDir.getOpposite())) {
											PipeDirection newItemDir = finalDir.getOpposite();
											PipeItem pi = new PipeItem(resultItem, Pipe.this.blockLoc, newItemDir);
											tempPipeItemsWithSpawn.put(pi, newItemDir);
										}
									} else {
										//drop the item in case the inventory block is registered in the "pipeNeighborBlocks" list but is no longer in the world,
										//e.g. removed with WorldEdit
										newBlockLoc.getWorld().dropItem(newBlockLoc.clone().add(0.5d, 0.5d, 0.5d), itemStack);
									}
								} catch (Exception e) {

								}
							}

						});
						itemHandling = ItemHandling.OUTPUT_TO_INVENTORY;
					}

					if (itemHandling == ItemHandling.NOTHING) {
						if (allConnections.contains(itemDir)) {
							Map<BlockLoc, Pipe> pipeMap = TransportPipes.getPipeMap(newBlockLoc.getWorld());
							if (pipeMap != null) {
								BlockLoc newBlockLocLong = TransportPipes.convertBlockLoc(newBlockLoc);
								if (pipeMap.containsKey(newBlockLocLong)) {
									Pipe pipe = pipeMap.get(newBlockLocLong);
									//put item in next pipe
									pipe.tempPipeItems.put(item, itemDir);
									itemHandling = ItemHandling.OUTPUT_TO_NEXT_PIPE;
								}
							}
						}
					}

					if (itemHandling == ItemHandling.NOTHING) {

						TransportPipes.pipePacketManager.destroyPipeItem(item);

						final ItemStack itemStack = item.getItem();
						Bukkit.getScheduler().runTask(TransportPipes.instance, new Runnable() {

							@Override
							public void run() {
								newBlockLoc.getWorld().dropItem(newBlockLoc.clone().add(0.5d, 0.5d, 0.5d), itemStack);
							}
						});
						itemHandling = ItemHandling.DROP;
					}
				}
				//END ITEM HANDLING CALC

			}

			//specifies that this item isn't handled inside another pipe in this tick if it moves in the tempPipeItems in this tick
			itemsTicked.add(item);

		}
	}

	protected float getPipeItemSpeed() {
		return ITEM_SPEED;
	}

	private void extractItems() {

		for (final PipeDirection dir : PipeDirection.values()) {

			final Location blockLoc = this.blockLoc.clone().add(dir.getX(), dir.getY(), dir.getZ());

			if (isPipeNeighborBlock(dir)) {
				//make sure that items won't be extracted if the extracting-pipe is an iron pipe pointing to the inventory block
				if (getPipeType() == PipeType.IRON) {
					IronPipe ip = (IronPipe) this;
					if (ip.getCurrentOutputDir().equals(dir)) {
						return;
					}
				}

				//input items
				Bukkit.getScheduler().runTask(TransportPipes.instance, new Runnable() {

					@Override
					public void run() {
						try {
							if (Pipe.this.blockLoc.getBlock().isBlockIndirectlyPowered()) {
								if (blockLoc.getBlock().getState() instanceof InventoryHolder) {
									InventoryHolder invH = (InventoryHolder) blockLoc.getBlock().getState();

									PipeDirection itemDir = dir.getOpposite();

									ItemStack taken = InventoryUtils.takeItemFromInventoryHolder(invH, Pipe.this, itemDir);
									if (taken != null) {
										PipeItem pi = new PipeItem(taken, Pipe.this.blockLoc, itemDir);
										tempPipeItemsWithSpawn.put(pi, itemDir);
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

			}

		}
	}

	public List<PipeDirection> getAllConnections() {
		List<PipeDirection> connections = PipeUtils.getOnlyPipeConnections(this);
		synchronized (this.pipeNeighborBlocks) {
			for (PipeDirection dir : this.pipeNeighborBlocks) {
				if (!connections.contains(dir)) {
					connections.add(dir);
				}
			}
		}
		return connections;
	}

	/**
	 * Determines wether the item should drop, be put in another pipe or be put in an inventory when it reaches the end of a pipe
	 */
	private enum ItemHandling {
		NOTHING(),
		DROP(),
		OUTPUT_TO_INVENTORY(),
		OUTPUT_TO_NEXT_PIPE()
	}

	public void saveToNBTTag(HashMap<String, Tag> tags) {
		NBTUtils.saveIntValue(tags, "PipeType", getPipeType().getId());
		NBTUtils.saveStringValue(tags, "PipeLocation", PipeUtils.LocToString(blockLoc));

		List<Tag> itemList = new ArrayList<Tag>();
		for (PipeItem pipeItem : pipeItems.keySet()) {
			HashMap<String, Tag> itemMap = new HashMap<>();

			NBTUtils.saveStringValue(itemMap, "RelLoc", pipeItem.relLoc().toString());
			NBTUtils.saveIntValue(itemMap, "Direction", pipeItems.get(pipeItem).getId());
			NBTUtils.saveStringValue(itemMap, "Item", InventoryUtils.ItemStackToString(pipeItem.getItem()));

			itemList.add(new CompoundTag("PipeItem", itemMap));
		}
		NBTUtils.saveListValue(tags, "PipeItems", NBTTagType.TAG_COMPOUND, itemList);

		List<Tag> neighborPipesList = new ArrayList<Tag>();
		List<PipeDirection> neighborPipes = PipeUtils.getOnlyPipeConnections(this);
		for (PipeDirection pd : neighborPipes) {
			neighborPipesList.add(new IntTag("Direction", pd.getId()));
		}
		NBTUtils.saveListValue(tags, "NeighborPipes", NBTTagType.TAG_INT, neighborPipesList);

	}

	public void loadFromNBTTag(CompoundTag tag) {
		Map<String, Tag> compoundValues = tag.getValue();

		List<Tag> pipeItems = NBTUtils.readListTag(compoundValues.get("PipeItems"));
		for (Tag itemTag : pipeItems) {
			CompoundTag itemCompoundTag = (CompoundTag) itemTag;
			Map<String, Tag> itemMap = itemCompoundTag.getValue();

			RelLoc relLoc = RelLoc.fromString(NBTUtils.readStringTag(itemMap.get("RelLoc"), null));
			PipeDirection dir = PipeDirection.fromID(NBTUtils.readIntTag(itemMap.get("Direction"), 0));
			ItemStack itemStack = InventoryUtils.StringToItemStack(NBTUtils.readStringTag(itemMap.get("Item"), null));

			if (itemStack != null) {
				PipeItem newPipeItem = new PipeItem(itemStack, this.blockLoc, dir);
				newPipeItem.relLoc().set(relLoc.getFloatX(), relLoc.getFloatY(), relLoc.getFloatZ());
				tempPipeItemsWithSpawn.put(newPipeItem, dir);
			}
		}

	}

	/**
	 * get the items that will be dropped on pipe destroy
	 */
	public abstract List<ItemStack> getDroppedItems(Player p);

	public abstract PipeType getPipeType();

}
