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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jnbt.ByteTag;
import org.jnbt.CompoundTag;
import org.jnbt.IntTag;
import org.jnbt.ListTag;
import org.jnbt.StringTag;
import org.jnbt.Tag;

import de.robotricker.transportpipes.PipeThread;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.TransportPipes.BlockLoc;
import de.robotricker.transportpipes.api.PipeExplodeEvent;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipeutils.InventoryUtils;
import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.pipeutils.PipeUtils;
import de.robotricker.transportpipes.pipeutils.RelLoc;

public abstract class Pipe {

	private static int maxItemsPerPipe = 10;
	public static final float ITEM_SPEED = 0.125f;//0.0625f;
	public static final float ICE_ITEM_SPEED = 0.5f;
	//hier wird berechnet um wie viel die relLoc verschoben werden muss, damit damit gerechnet werden kann, ohne dass man mit floats rechnet
	//(da bei float-rechnungen ungenaue ergebnisse rauskommen)
	public static final long FLOAT_PRECISION = (long) (Math.pow(10, Float.toString(ITEM_SPEED).split("\\.")[1].length()));

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
		return this.pipeNeighborBlocks.contains(dir);
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

	public void tick(boolean inputItems, List<PipeItem> itemsAlreadyTicked) {

		//input items from inventories and calculate PipeDirections
		List<PipeDirection> dirs = inputItemsAndCalculatePipeDirections(inputItems);

		//handle item transport through pipe
		transportItems(dirs, itemsAlreadyTicked);

		//pipe explosion if too many items
		if (pipeItems.size() >= maxItemsPerPipe) {
			PipeExplodeEvent pee = new PipeExplodeEvent(this);
			Bukkit.getPluginManager().callEvent(pee);
			if (!pee.isCancelled()) {
				PipeThread.runTask(new Runnable() {

					@Override
					public void run() {
						PipeUtils.destroyPipe(null, Pipe.this, true);
						blockLoc.getWorld().playSound(blockLoc, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
						blockLoc.getWorld().playEffect(blockLoc.clone().add(0.5d, 0.5d, 0.5d), Effect.SMOKE, 31);
					}
				}, 0);
			}
		}

	}

	/**
	 * Will be overridden by all pipe subclasses. This method is be called for all items that arrive in the middle of the pipe to calculate the direction they should go next
	 */
	public abstract PipeDirection itemArrivedAtMiddle(PipeItem item, PipeDirection before, List<PipeDirection> dirs);

	private void transportItems(List<PipeDirection> dirs, List<PipeItem> itemsAlreadyTicked) {

		List<PipeDirection> pipeConnections = PipeUtils.getOnlyPipeConnections(this);

		HashMap<PipeItem, PipeDirection> mapCopy = (HashMap<PipeItem, PipeDirection>) pipeItems.clone();
		for (final PipeItem item : mapCopy.keySet()) {
			PipeDirection dir = mapCopy.get(item);

			//if the item is in the middle of the pipe
			if (item.changeRelLoc().getFloatX() == 0.5f && item.changeRelLoc().getFloatY() == 0.5f && item.changeRelLoc().getFloatZ() == 0.5f) {
				dir = itemArrivedAtMiddle(item, dir, dirs);
				pipeItems.put(item, dir);
			}

			RelLoc relLoc = item.changeRelLoc();
			float xSpeed = dir.getX() * getPipeItemSpeed();
			float ySpeed = dir.getY() * getPipeItemSpeed();
			float zSpeed = dir.getZ() * getPipeItemSpeed();
			//update relLoc (the real movement)
			relLoc.set((long) (relLoc.getLongX() + xSpeed * FLOAT_PRECISION), (long) (relLoc.getLongY() + ySpeed * FLOAT_PRECISION), (long) (relLoc.getLongZ() + zSpeed * FLOAT_PRECISION));

			//specifies that this item isn't handled inside another pipe in this tick if it moves in the tempPipeItems in this tick
			itemsAlreadyTicked.add(item);

			//update relLocDiff (for pipe updating packets)
			item.changeRelLocDiff().set(xSpeed, ySpeed, zSpeed);

			//update
			TransportPipes.pipePacketManager.updatePipeItem(item);

			//if the item is at the end of the transportation
			if (relLoc.getFloatX() == 1.0f || relLoc.getFloatY() == 1.0f || relLoc.getFloatZ() == 1.0f || relLoc.getFloatX() == 0.0f || relLoc.getFloatY() == 0.0f || relLoc.getFloatZ() == 0.0f) {
				removePipeItem(item);

				//change relLoc:
				// 1 -> 0
				// 0 -> 1
				item.changeRelLoc().set(1f - item.changeRelLoc().getFloatX(), 1f - item.changeRelLoc().getFloatY(), 1f - item.changeRelLoc().getFloatZ());

				final Location newBlockLoc = blockLoc.clone().add(dir.getX(), dir.getY(), dir.getZ());
				ItemHandling itemHandling = ItemHandling.NOTHING;

				if (isPipeNeighborBlock(dir)) {
					itemHandling = ItemHandling.OUTPUT_TO_INVENTORY;
				}

				if (itemHandling == ItemHandling.NOTHING) {
					if (pipeConnections.contains(dir)) {
						Map<BlockLoc, Pipe> pipeMap = TransportPipes.getPipeMap(newBlockLoc.getWorld());
						if (pipeMap != null) {
							BlockLoc newBlockLocLong = TransportPipes.convertBlockLoc(newBlockLoc);
							if (pipeMap.containsKey(newBlockLocLong)) {
								Pipe pipe = pipeMap.get(newBlockLocLong);
								//put item in next pipe
								pipe.tempPipeItems.put(item, dir);
								itemHandling = ItemHandling.OUTPUT_TO_NEXT_PIPE;
							}
						}
					}
				}

				if (itemHandling == ItemHandling.NOTHING) {
					itemHandling = ItemHandling.DROP;
				}

				final ItemStack itemStack = item.getItem();

				if (itemHandling == ItemHandling.DROP) {

					TransportPipes.pipePacketManager.destroyPipeItem(item);

					Bukkit.getScheduler().runTask(TransportPipes.instance, new Runnable() {

						@Override
						public void run() {
							newBlockLoc.getWorld().dropItem(newBlockLoc.clone().add(0.5d, 0.5d, 0.5d), itemStack);
						}
					});
				} else if (itemHandling == ItemHandling.OUTPUT_TO_INVENTORY) {

					final PipeDirection finalDir = dir;

					Bukkit.getScheduler().runTask(TransportPipes.instance, new Runnable() {

						@Override
						public void run() {
							try {

								TransportPipes.pipePacketManager.destroyPipeItem(item);

								if (newBlockLoc.getBlock().getState() instanceof InventoryHolder) {
									InventoryHolder invH = (InventoryHolder) newBlockLoc.getBlock().getState();
									//put items in inv and put overflow items back in pipe
									for (ItemStack resultItem : InventoryUtils.putItemInInventoryHolder(invH, itemStack, finalDir.getOpposite())) {
										PipeDirection itemDir = finalDir.getOpposite();
										PipeItem pi = new PipeItem(resultItem, Pipe.this.blockLoc, itemDir);
										//the item should only be put there if the pipe is also there
										//if the pipe is no longer there but the Pipe Object still exists
										//the item should drop
										tempPipeItemsWithSpawn.put(pi, itemDir);
									}
								} else {
									//drop the item in case the inventory block is registered in the "pipeNeighborBlocks" list but is no longer in the world,
									//e.g. removed with WorldEdit
									newBlockLoc.getWorld().dropItem(newBlockLoc.clone().add(0.5d, 0.5d, 0.5d), itemStack);
								}
							} catch (Exception e) {
								e.printStackTrace(); //this should be not there when exporting
							}
						}

					});

				}

			}

		}
	}

	protected float getPipeItemSpeed() {
		return ITEM_SPEED;
	}

	private List<PipeDirection> inputItemsAndCalculatePipeDirections(boolean inputItems) {

		List<PipeDirection> dirs = new ArrayList<PipeDirection>();

		List<PipeDirection> pipeConnections = PipeUtils.getOnlyPipeConnections(this);
		for (final PipeDirection dir : PipeDirection.values()) {

			final Location blockLoc = this.blockLoc.clone().add(dir.getX(), dir.getY(), dir.getZ());
			boolean dirAvailable = false;

			if (pipeConnections.contains(dir)) {
				dirAvailable = true;
			}

			if (!dirAvailable) {
				if (isPipeNeighborBlock(dir)) {
					//make sure that items won't be extracted if the extracting-pipe is an iron pipe pointing to the inventory block
					if (getPipeType() == PipeType.IRON) {
						IronPipe ip = (IronPipe) this;
						if (!ip.getCurrentOutputDir().equals(dir)) {
							dirAvailable = true;
						}
					} else {
						dirAvailable = true;
					}

					if (dirAvailable) {
						//input items
						if (inputItems) {
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
			}

			if (dirAvailable) {
				dirs.add(dir);
			}

		}

		return dirs;
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
		tags.put("PipeClassName", new StringTag("PipeClassName", getClass().getName()));
		tags.put("PipeLocation", new StringTag("PipeLocation", PipeUtils.LocToString(blockLoc)));
		tags.put("IcePipe", new ByteTag("IcePipe", getPipeType() == PipeType.ICE ? (byte) 1 : (byte) 0));
		List<Tag> itemList = new ArrayList<>();

		for (PipeItem pipeItem : pipeItems.keySet()) {
			HashMap<String, Tag> itemMap = new HashMap<>();

			//put item data
			{
				ItemStack item = pipeItem.getItem();
				itemMap.put("RelLoc", new StringTag("RelLoc", pipeItem.changeRelLoc().toString()));
				itemMap.put("Direction", new IntTag("Direction", pipeItems.get(pipeItem).getId()));
				itemMap.put("Type", new IntTag("Type", item.getTypeId()));
				itemMap.put("Damage", new ByteTag("Damage", item.getData().getData()));
				itemMap.put("Amount", new IntTag("Amount", item.getAmount()));
				if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
					itemMap.put("DisplayName", new StringTag("DisplayName", item.getItemMeta().getDisplayName()));
				}

				List<Tag> enchantments = new ArrayList<>();
				for (Enchantment ench : item.getEnchantments().keySet()) {
					HashMap<String, Tag> enchMap = new HashMap<>();
					enchMap.put("Id", new IntTag("Id", ench.getId()));
					enchMap.put("Level", new IntTag("Level", item.getEnchantments().get(ench)));
					enchantments.add(new CompoundTag("Enchantment", enchMap));
				}

				ListTag enchantsTag = new ListTag("Enchantments", CompoundTag.class, enchantments);
				itemMap.put("Enchantments", enchantsTag);
			}

			itemList.add(new CompoundTag("PipeItem", itemMap));
		}

		ListTag itemsTag = new ListTag("PipeItems", CompoundTag.class, itemList);
		tags.put("PipeItems", itemsTag);
	}

	public void loadFromNBTTag(CompoundTag tag) {
		Map<String, Tag> compoundValues = tag.getValue();

		ListTag pipeItems = (ListTag) compoundValues.get("PipeItems");
		for (Tag itemTag : pipeItems.getValue()) {
			CompoundTag itemCompoundTag = (CompoundTag) itemTag;
			Map<String, Tag> itemMap = itemCompoundTag.getValue();

			RelLoc relLoc = RelLoc.fromString(((StringTag) itemMap.get("RelLoc")).getValue());
			PipeDirection dir = PipeDirection.fromID(((IntTag) itemMap.get("Direction")).getValue());
			int id = ((IntTag) itemMap.get("Type")).getValue();
			byte damage = ((ByteTag) itemMap.get("Damage")).getValue();
			int amount = ((IntTag) itemMap.get("Amount")).getValue();
			String displayName = itemMap.containsKey("DisplayName") ? ((StringTag) itemMap.get("DisplayName")).getValue() : null;

			ItemStack itemStack = new ItemStack(id, amount, (short) damage);
			ItemMeta itemMeta = itemStack.getItemMeta();
			if (displayName != null) {
				itemMeta.setDisplayName(displayName);
			}
			itemStack.setItemMeta(itemMeta);

			//Enchantments
			{
				List<Tag> enchantmentsTag = ((ListTag) itemMap.get("Enchantments")).getValue();
				for (Tag enchantmentTag : enchantmentsTag) {
					Map<String, Tag> enchantmentMap = ((CompoundTag) enchantmentTag).getValue();

					int enchId = ((IntTag) enchantmentMap.get("Id")).getValue();
					int enchLvl = ((IntTag) enchantmentMap.get("Level")).getValue();

					itemStack.addUnsafeEnchantment(Enchantment.getById(enchId), enchLvl);
				}
			}

			PipeItem newPipeItem = new PipeItem(itemStack, this.blockLoc, dir);
			newPipeItem.changeRelLoc().set(relLoc.getFloatX(), relLoc.getFloatY(), relLoc.getFloatZ());
			tempPipeItemsWithSpawn.put(newPipeItem, dir);

		}

	}

	/**
	 * get the items that will be dropped on pipe destroy
	 */
	public abstract List<ItemStack> getDroppedItems();

	public abstract PipeType getPipeType();

}
