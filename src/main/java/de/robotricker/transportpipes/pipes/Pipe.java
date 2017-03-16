package de.robotricker.transportpipes.pipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipeutils.InventoryUtils;
import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.pipeutils.PipeUtils;
import de.robotricker.transportpipes.pipeutils.RelLoc;
import de.robotricker.transportpipes.pipeutils.hitbox.AxisAlignedBB;
import de.robotricker.transportpipes.protocol.ArmorStandData;

public abstract class Pipe {

	private static final float ITEM_SPEED = 0.25f;//0.0625f;
	//hier wird berechnet um wie viel die relLoc verschoben werden muss, damit damit gerechnet werden kann, ohne dass man mit floats rechnet
	//(da bei float-rechnungen ungenaue ergebnisse rauskommen)
	public static final long FLOAT_PRECISION = (long) (Math.pow(10, Float.toString(ITEM_SPEED).split("\\.")[1].length()));

	protected static final ItemStack ITEM_BLAZE = new ItemStack(Material.BLAZE_ROD);
	protected static final ItemStack ITEM_GLASS = new ItemStack(Material.GLASS);
	protected static final ItemStack ITEM_GOLD_BLOCK = new ItemStack(Material.GOLD_BLOCK);
	protected static final ItemStack ITEM_IRON_BLOCK = new ItemStack(Material.IRON_BLOCK);
	//protected static final ItemStack ITEM_ARROW = new ItemStack(Material.ARROW);
	protected static final ItemStack ITEM_CARPET_WHITE = new ItemStack(Material.CARPET, 1, (short) 0);
	protected static final ItemStack ITEM_CARPET_YELLOW = new ItemStack(Material.CARPET, 1, (short) 4);
	protected static final ItemStack ITEM_CARPET_GREEN = new ItemStack(Material.CARPET, 1, (short) 5);
	protected static final ItemStack ITEM_CARPET_BLUE = new ItemStack(Material.CARPET, 1, (short) 11);
	protected static final ItemStack ITEM_CARPET_RED = new ItemStack(Material.CARPET, 1, (short) 14);
	protected static final ItemStack ITEM_CARPET_BLACK = new ItemStack(Material.CARPET, 1, (short) 15);

	private List<ArmorStandData> armorStandList;

	public HashMap<PipeItem, PipeDirection> pipeItems = new HashMap<>();

	//here are pipes saved that should be put in "pipeItems" in the next tick and should NOT be spawned to the players (they are already spawned)
	//jedes iteraten durch diese Map MUSS mit synchronized(tempPipeItems){} sein!
	public final Map<PipeItem, PipeDirection> tempPipeItems = Collections.synchronizedMap(new HashMap<PipeItem, PipeDirection>());

	//here are pipes saved that should be put in "pipeItems" in the next tick and should be spawned to the players
	//jedes iteraten durch diese Map MUSS mit synchronized(tempPipeItemsWithSpawn){} sein!
	public final Map<PipeItem, PipeDirection> tempPipeItemsWithSpawn = Collections.synchronizedMap(new HashMap<PipeItem, PipeDirection>());

	public Location blockLoc;

	//that ONLY contains the neighbor BLOCKS, not the neighbor pipes!
	//jedes iteraten durch diese List MUSS mit synchronized(pipeNeighborBlocks){} sein!
	public List<PipeDirection> pipeNeighborBlocks = Collections.synchronizedList(new ArrayList<PipeDirection>());

	//the Hitbox for this pipe
	private AxisAlignedBB aabb;

	public Pipe(Location blockLoc, AxisAlignedBB aabb) {
		armorStandList = new ArrayList<>();
		this.blockLoc = blockLoc;
		this.aabb = aabb;
	}

	public Pipe(Location blockLoc, AxisAlignedBB aabb, List<PipeDirection> pipeNeighborBlocks, ArmorStandData... list) {
		this(blockLoc, aabb);
		Collections.addAll(armorStandList, list);
		synchronized (pipeNeighborBlocks) {
			for (PipeDirection pd : pipeNeighborBlocks) {
				this.pipeNeighborBlocks.add(pd);
			}
		}
	}

	public AxisAlignedBB getAABB() {
		return aabb;
	}

	/**
	 * checks if the neighbor block of this pipe in the direction "dir" is a block with an inventory (not a pipe)
	 */
	public boolean isPipeNeighborBlock(PipeDirection dir) {
		return this.pipeNeighborBlocks.contains(dir);
	}

	public List<ArmorStandData> getArmorStandList() {
		return armorStandList;
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
		if (pipeItems.containsKey(item)) {
			return pipeItems.get(item);
		}
		return null;
	}

	/**
	 * removes the PipeItem from this pipe. This should be called whenever the item leaves the pipe (for all ItemHandling cases)
	 */
	public void removePipeItem(PipeItem item) {
		if (pipeItems.containsKey(item)) {
			pipeItems.remove(item);
		}
	}

	public void tick(boolean inputItems, List<PipeItem> roundItems) {
		//input items from inventorys and ground and calculate PipeDirections
		List<PipeDirection> dirs = inputItemsAndCalculatePipeDirections(inputItems);

		//handle item transport through pipe
		transportItems(dirs, roundItems);
	}

	/**
	 * Will be overridden by all Pipe Subclasses. This method is be called for all items that arrive in the middle of the pipe to calculate the direction they should go to
	 */
	public abstract PipeDirection itemArrivedAtMiddle(PipeItem item, PipeDirection before, List<PipeDirection> dirs);

	private void transportItems(List<PipeDirection> dirs, List<PipeItem> roundItems) {
		HashMap<PipeItem, PipeDirection> mapCopy = (HashMap<PipeItem, PipeDirection>) pipeItems.clone();
		for (final PipeItem item : mapCopy.keySet()) {
			PipeDirection dir = mapCopy.get(item);

			//if the item is in the middle of the pipe
			if (item.changeRelLoc().getFloatX() == 0.5f && item.changeRelLoc().getFloatY() == 0.5f && item.changeRelLoc().getFloatZ() == 0.5f) {
				dir = itemArrivedAtMiddle(item, dir, dirs);
				pipeItems.put(item, dir);
			}

			RelLoc relLoc = item.changeRelLoc();
			float xSpeed = dir.getX() * ITEM_SPEED;
			float ySpeed = dir.getY() * ITEM_SPEED;
			float zSpeed = dir.getZ() * ITEM_SPEED;
			//update relLoc (the real movement)
			relLoc.set((long) (relLoc.getLongX() + xSpeed * FLOAT_PRECISION), (long) (relLoc.getLongY() + ySpeed * FLOAT_PRECISION), (long) (relLoc.getLongZ() + zSpeed * FLOAT_PRECISION));

			//specifies that this item isn't handled inside another pipe in this tick if it moves in the tempPipeItems in this tick
			roundItems.add(item);

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
					itemHandling = ItemHandling.INPUT_IN_INVENTORY;
				}

				if (itemHandling == ItemHandling.NOTHING) {

					Map<Long, Pipe> pipeMap = TransportPipes.getPipeMap(newBlockLoc.getWorld());
					if (pipeMap != null) {
						long newBlockLocLong = TransportPipes.blockLocToLong(newBlockLoc);
						if (pipeMap.containsKey(newBlockLocLong)) {
							Pipe pipe = pipeMap.get(newBlockLocLong);
							//put item in next pipe
							pipe.tempPipeItems.put(item, dir);
							itemHandling = ItemHandling.INPUT_IN_NEXT_PIPE;
						}
					}
				}

				if (itemHandling == ItemHandling.NOTHING) {
					itemHandling = ItemHandling.DROP;
				}

				final ItemStack itemStack = item.getItem();

				if (itemHandling == ItemHandling.DROP) {

					TransportPipes.pipePacketManager.destroyPipeItemSync(item);

					Bukkit.getScheduler().runTask(TransportPipes.instance, new Runnable() {

						@Override
						public void run() {
							newBlockLoc.getWorld().dropItem(newBlockLoc.clone().add(0.5d, 0.5d, 0.5d), itemStack);
						}
					});
				} else if (itemHandling == ItemHandling.INPUT_IN_INVENTORY) {

					final PipeDirection finalDir = dir;

					Bukkit.getScheduler().runTask(TransportPipes.instance, new Runnable() {

						@Override
						public void run() {
							try {

								TransportPipes.pipePacketManager.destroyPipeItemSync(item);

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

	private List<PipeDirection> inputItemsAndCalculatePipeDirections(boolean inputItems) {

		List<PipeDirection> dirs = new ArrayList<>();

		Map<Long, Pipe> pipeMap = TransportPipes.getPipeMap(blockLoc.getWorld());
		if (pipeMap != null) {

			for (final PipeDirection dir : PipeDirection.values()) {

				final Location blockLoc = this.blockLoc.clone().add(dir.getX(), dir.getY(), dir.getZ());
				boolean dirAvailable = false;

				if (pipeMap.containsKey(TransportPipes.blockLocToLong(blockLoc))) {
					dirAvailable = true;
				}

				if (!dirAvailable) {
					if (isPipeNeighborBlock(dir)) {
						dirAvailable = true;

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

				if (dirAvailable) {
					dirs.add(dir);
				}

			}
		}

		return dirs;
	}

	/**
	 * removes this pipe instance and creates a new one with the correct shape depending on the neighbor blocks/pipe (but only if necessary)
	 */
	public void updatePipeShape() {

		//sum all neighbor blocks and pipes
		final List<PipeDirection> pipeConnections = PipeUtils.getPipeConnections(blockLoc);
		List<PipeDirection> combi = new ArrayList<>();
		combi.addAll(pipeConnections);
		synchronized (this.pipeNeighborBlocks) {
			for (PipeDirection dir : this.pipeNeighborBlocks) {
				if (!combi.contains(dir)) {
					combi.add(dir);
				}
			}
		}

		//build new pipe if necessary and remove 'this' pipe
		Class<? extends Pipe> newPipeClass = PipeUtils.calculatePipeShapeWithDirList(combi);

		if (newPipeClass != null && !newPipeClass.equals(this.getClass())) {
			Pipe pipe = null;
			try {
				pipe = newPipeClass.getConstructor(Location.class, List.class).newInstance(blockLoc, this.pipeNeighborBlocks);

				Map<Long, Pipe> pipeMap = TransportPipes.getPipeMap(blockLoc.getWorld());
				pipeMap.put(TransportPipes.blockLocToLong(pipe.blockLoc), pipe);

				TransportPipes.pipePacketManager.destroyPipeSync(this);
				TransportPipes.pipePacketManager.spawnPipeSync(pipe);

				//put all items from old pipe in new pipe
				for (PipeItem item : pipeItems.keySet()) {
					pipe.tempPipeItems.put(item, pipeItems.get(item));
				}
				//and clear old pipe items map
				pipeItems.clear();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * You don't have to really destroy/remove the pipe or drop the items inside of the pipe.<br>
	 * But you have to drop the pipe itself that it can be used again
	 */
	public abstract void destroy(boolean dropItem);

	/**
	 * Determines wether the item should drop, be put in another pipe or be put in an Inventory when it reaches the end of a pipe
	 */
	private enum ItemHandling {
		NOTHING(),
		DROP(),
		INPUT_IN_INVENTORY(),
		INPUT_IN_NEXT_PIPE()
	}

	public void saveToNBTTag(HashMap<String, Tag> tags) {
		tags.put("PipeClassName", new StringTag("PipeClassName", getClass().getName()));
		tags.put("PipeLocation", new StringTag("PipeLocation", PipeUtils.LocToString(blockLoc)));
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

}
