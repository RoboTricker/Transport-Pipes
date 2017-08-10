package de.robotricker.transportpipes.pipes.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jnbt.CompoundTag;
import org.jnbt.Tag;

import de.robotricker.transportpipes.PipeThread;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipes.BlockLoc;
import de.robotricker.transportpipes.pipes.ClickablePipe;
import de.robotricker.transportpipes.pipes.PipeDirection;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipes.PipeUtils;
import de.robotricker.transportpipes.pipes.extractionpipe.ExtractionPipeInv;
import de.robotricker.transportpipes.pipeutils.ContainerBlockUtils;
import de.robotricker.transportpipes.pipeutils.NBTUtils;
import de.robotricker.transportpipes.pipeutils.PipeItemUtils;
import de.robotricker.transportpipes.pipeutils.config.LocConf;

public class ExtractionPipe extends Pipe implements ClickablePipe {

	private int lastOutputIndex = 0;

	private PipeDirection extractDirection;
	private ExtractCondition extractCondition;
	private ExtractAmount extractAmount;

	public ExtractionPipe(Location blockLoc) {
		super(blockLoc);
		extractDirection = null;
		extractCondition = ExtractCondition.NEEDS_REDSTONE;
		extractAmount = ExtractAmount.EXTRACT_1;
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
	public void saveToNBTTag(HashMap<String, Tag> tags) {
		super.saveToNBTTag(tags);
		NBTUtils.saveIntValue(tags, "ExtractDirection", extractDirection == null ? -1 : extractDirection.getId());
		NBTUtils.saveIntValue(tags, "ExtractCondition", extractCondition.getId());
		NBTUtils.saveIntValue(tags, "ExtractAmount", extractAmount.getId());
	}

	@Override
	public void loadFromNBTTag(CompoundTag tag) {
		super.loadFromNBTTag(tag);
		int extractDirectionId = NBTUtils.readIntTag(tag.getTag("ExtractDirection"), -1);
		if (extractDirectionId == -1) {
			setExtractDirection(null);
		} else {
			setExtractDirection(PipeDirection.fromID(extractDirectionId));
		}
		setExtractCondition(ExtractCondition.fromId(NBTUtils.readIntTag(tag.getTag("ExtractCondition"), ExtractCondition.NEEDS_REDSTONE.getId())));
		setExtractAmount(ExtractAmount.fromId(NBTUtils.readIntTag(tag.getTag("ExtractAmount"), ExtractAmount.EXTRACT_1.getId())));
	}

	@Override
	public void click(Player p, PipeDirection side) {
		ExtractionPipeInv.updateExtractionPipeInventory(p, this);
	}

	@Override
	public PipeType getPipeType() {
		return PipeType.EXTRACTION;
	}

	@Override
	public List<ItemStack> getDroppedItems() {
		List<ItemStack> is = new ArrayList<>();
		is.add(PipeItemUtils.getPipeItem(getPipeType(), null));
		return is;
	}

	public PipeDirection getExtractDirection() {
		return extractDirection;
	}

	public void setExtractDirection(PipeDirection extractDirection) {
		this.extractDirection = extractDirection;
	}

	public ExtractCondition getExtractCondition() {
		return extractCondition;
	}

	public void setExtractCondition(ExtractCondition extractCondition) {
		this.extractCondition = extractCondition;
	}

	public ExtractAmount getExtractAmount() {
		return extractAmount;
	}

	public void setExtractAmount(ExtractAmount extractAmount) {
		this.extractAmount = extractAmount;
	}

	/**
	 * checks if the current extract direction is valid and updates it to a valid value if necessary
	 * 
	 * @param cycle
	 *            whether the direction should really cycle or just be checked for validity
	 */
	public void checkAndUpdateExtractDirection(boolean cycle) {
		PipeDirection oldExtractDirection = getExtractDirection();

		List<PipeDirection> blockConnections = PipeUtils.getOnlyBlockConnections(this);
		if (blockConnections.isEmpty()) {
			extractDirection = null;
		} else if (cycle || extractDirection == null || !blockConnections.contains(extractDirection)) {
			do {
				if (extractDirection == null) {
					extractDirection = PipeDirection.NORTH;
				} else {
					extractDirection = extractDirection.getNextDirection();
				}
			} while (!blockConnections.contains(extractDirection));
		}

		if (oldExtractDirection != extractDirection) {
			PipeThread.runTask(new Runnable() {

				public void run() {
					TransportPipes.pipePacketManager.updatePipe(ExtractionPipe.this);
				};
			}, 0);
		}
	}

	protected void extractItems(List<PipeDirection> blockConnections) {

		if (extractDirection == null) {
			return;
		}

		if (blockConnections.contains(extractDirection)) {

			final Location containerLoc = blockLoc.clone().add(extractDirection.getX(), extractDirection.getY(), extractDirection.getZ());

			//input items
			Bukkit.getScheduler().runTask(TransportPipes.instance, new Runnable() {

				@Override
				public void run() {
					try {
						if (!isInLoadedChunk()) {
							return;
						}
						boolean powered = getExtractCondition() == ExtractCondition.ALWAYS_EXTRACT;
						if (getExtractCondition() == ExtractCondition.NEEDS_REDSTONE) {
							for (PipeDirection pd : PipeDirection.values()) {
								Location relativeLoc = ExtractionPipe.this.blockLoc.clone().add(pd.getX(), pd.getY(), pd.getZ());

								//don't power this pipe if at least 1 block around this pipe is inside an unloaded chunk
								if (!ContainerBlockUtils.isInLoadedChunk(relativeLoc)) {
									break;
								}

								Block relative = relativeLoc.getBlock();
								if (relative.getType() != Material.TRAPPED_CHEST && relative.getBlockPower(pd.getOpposite().toBlockFace()) > 0) {
									powered = true;
									break;
								}

							}
						}
						if (powered) {
							BlockLoc bl = BlockLoc.convertBlockLoc(containerLoc);
							TransportPipesContainer tpc = TransportPipes.instance.getContainerMap(blockLoc.getWorld()).get(bl);
							PipeDirection itemDir = extractDirection.getOpposite();
							if (tpc != null) {
								ItemStack taken = tpc.extractItem(itemDir, getExtractAmount().getAmount());
								if (taken != null) {
									//extraction successful
									PipeItem pi = new PipeItem(taken, ExtractionPipe.this.blockLoc, itemDir);
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

	@Override
	public int[] getBreakParticleData() {
		return new int[] { 5, 0 };
	}

	@Override
	public void notifyConnectionsChange() {
		super.notifyConnectionsChange();
		checkAndUpdateExtractDirection(false);
	}

	public enum ExtractCondition {
		NEEDS_REDSTONE(LocConf.EXTRACTIONPIPE_CONDITION_NEEDSREDSTONE, Material.REDSTONE, (short) 0),
		ALWAYS_EXTRACT(LocConf.EXTRACTIONPIPE_CONDITION_ALWAYSEXTRACT, Material.INK_SACK, (short) 10),
		NEVER_EXTRACT(LocConf.EXTRACTIONPIPE_CONDITION_NEVEREXTRACT, Material.BARRIER, (short) 0);

		private String locConfKey;
		private ItemStack displayItem;

		private ExtractCondition(String locConfKey, Material type, short damage) {
			this.locConfKey = locConfKey;
			this.displayItem = new ItemStack(type, 1, damage);
		}

		public String getLocConfKey() {
			return locConfKey;
		}

		public int getId() {
			return this.ordinal();
		}

		public static ExtractCondition fromId(int id) {
			return ExtractCondition.values()[id];
		}

		public ExtractCondition getNextCondition() {
			if (getId() == ExtractCondition.values().length - 1) {
				return fromId(0);
			}
			return fromId(getId() + 1);
		}

		public ItemStack getDisplayItem() {
			return displayItem.clone();
		}

	}

	public enum ExtractAmount {
		EXTRACT_1(LocConf.EXTRACTIONPIPE_AMOUNT_EXTRACT1, 1),
		EXTRACT_16(LocConf.EXTRACTIONPIPE_AMOUNT_EXTRACT16, 16);

		private String locConfKey;
		private ItemStack displayItem;

		private ExtractAmount(String locConfKey, int amount) {
			this.locConfKey = locConfKey;
			this.displayItem = new ItemStack(Material.BRICK, amount);
		}

		public int getAmount() {
			return displayItem.getAmount();
		}

		public String getLocConfKey() {
			return locConfKey;
		}

		public int getId() {
			return this.ordinal();
		}

		public static ExtractAmount fromId(int id) {
			return ExtractAmount.values()[id];
		}

		public ExtractAmount getNextAmount() {
			if (getId() == ExtractAmount.values().length - 1) {
				return fromId(0);
			}
			return fromId(getId() + 1);
		}

		public ItemStack getDisplayItem() {
			return displayItem.clone();
		}
	}

}
