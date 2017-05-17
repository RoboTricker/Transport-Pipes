package de.robotricker.transportpipes.pipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jnbt.CompoundTag;
import org.jnbt.ListTag;
import org.jnbt.Tag;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.manager.settings.GoldenPipeInv;
import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipes.interfaces.Clickable;
import de.robotricker.transportpipes.pipeutils.PipeColor;
import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.pipeutils.PipeUtils;
import de.robotricker.transportpipes.pipeutils.RelLoc;
import de.robotricker.transportpipes.pipeutils.hitbox.AxisAlignedBB;
import de.robotricker.transportpipes.protocol.ArmorStandData;

public class GoldenPipe extends Pipe implements Clickable {

	//1st dimension: output dirs in order of PipeDirection.values() | 2nd dimension: output items in this direction
	private ItemData[][] outputItems = new ItemData[6][8];
	private boolean ignoreNBT = false;

	public GoldenPipe(Location blockLoc, List<PipeDirection> pipeNeighborBlocks, PipeColor pipeColor) {
		//PipeLoc | Body Direction | isSmall | HeadItem | HandItem | headRotation | handRotation
		//@formatter:off
		super(pipeColor, blockLoc, new AxisAlignedBB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75), pipeNeighborBlocks,
				new ArmorStandData(new RelLoc(0.5f, -0.43f, 0.5f), new Vector(1, 0, 0), true, ITEM_GOLD_BLOCK, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)),
				new ArmorStandData(new RelLoc(0.5f + 0.26f, -0.255f, 0.5f), new Vector(1, 0, 0), true, ITEM_CARPET_WHITE, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f)),
				new ArmorStandData(new RelLoc(0.5f - 0.26f, -0.255f, 0.5f), new Vector(-1, 0, 0), true, ITEM_CARPET_YELLOW, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f)),
				new ArmorStandData(new RelLoc(0.5f, -0.255f, 0.5f + 0.26f), new Vector(0, 0, 1), true, ITEM_CARPET_GREEN, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f)),
				new ArmorStandData(new RelLoc(0.5f, -0.255f, 0.5f - 0.26f), new Vector(0, 0, -1), true, ITEM_CARPET_BLUE, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f)),
				new ArmorStandData(new RelLoc(0.5f, -0.255f + 0.26f, 0.5f), new Vector(1, 0, 0), true, ITEM_CARPET_RED, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)),
				new ArmorStandData(new RelLoc(0.5f, -0.255f - 0.26f, 0.5f), new Vector(1, 0, 0), true, ITEM_CARPET_BLACK, null, new Vector(180f, 0f, 0f), new Vector(0f, 0f, 0f)));
		//@formatter:on
	}

	@Override
	public PipeDirection itemArrivedAtMiddle(PipeItem item, PipeDirection before, List<PipeDirection> dirs) {

		ItemData itemMAD = new ItemData(item.getItem());

		List<PipeDirection> possibleDirections = getPossibleDirectionsForItem(itemMAD, before);

		return possibleDirections.get(new Random().nextInt(possibleDirections.size()));

	}

	public List<PipeDirection> getPossibleDirectionsForItem(ItemData itemData, PipeDirection before) {
		//all directions in which is an other pipe or inventory-block
		List<PipeDirection> connectionDirections = PipeUtils.getPipeConnections(blockLoc, pipeColor, false);

		synchronized (pipeNeighborBlocks) {
			for (PipeDirection dir : pipeNeighborBlocks) {
				if (!connectionDirections.contains(dir)) {
					connectionDirections.add(dir);
				}
			}
		}

		//the possible directions in which the item could go
		List<PipeDirection> possibleDirections = new ArrayList<>();

		for (int line = 0; line < 6; line++) {
			PipeDirection dir = PipeDirection.fromID(line);
			//ignore the direction in which is no pipe or inv-block
			if (!connectionDirections.contains(dir)) {
				continue;
			}
			for (int i = 0; i < 8; i++) {
				if (ignoreNBT) {
					ItemStack item = itemData.toItemStack();
					if (outputItems[line][i] != null) {
						ItemStack sample = outputItems[line][i].toItemStack();
						if (sample.getType().equals(item.getType()) && sample.getData().getData() == item.getData().getData()) {
							possibleDirections.add(dir);
						}
					}
				} else if (itemData.equals(outputItems[line][i])) {
					possibleDirections.add(dir);
				}
			}
		}

		//if this item isn't in the list, it will take a random direction from the empty dirs
		if (possibleDirections.isEmpty()) {

			List<PipeDirection> emptyList = new ArrayList<>();

			for (int line = 0; line < 6; line++) {
				PipeDirection dir = PipeDirection.fromID(line);
				//ignore the direction in which is no pipe or inv-block
				if (!connectionDirections.contains(dir)) {
					continue;
				}
				boolean empty = true;
				for (int i = 0; i < 8; i++) {
					if (outputItems[line][i] != null) {
						empty = false;
					}
				}
				if (empty) {
					emptyList.add(dir);
				}
			}

			for (PipeDirection dir : emptyList) {
				//add all possible empty directions without the direction back. Only if this is the only possible way, it will go back.
				if (dir != before.getOpposite() || emptyList.size() == 1) {
					possibleDirections.add(dir);
				}
			}

		}

		//if all lines are full with items, it will simply go back.
		if (possibleDirections.isEmpty()) {
			possibleDirections.add(before.getOpposite());
		}
		return possibleDirections;
	}

	@Override
	public void saveToNBTTag(HashMap<String, Tag> tags) {
		super.saveToNBTTag(tags);

		for (int line = 0; line < 6; line++) {
			List<Tag> lineList = new ArrayList<Tag>();
			ListTag lineTag = new ListTag("Line" + line, CompoundTag.class, lineList);

			for (int i = 0; i < 8; i++) {
				ItemData mad = outputItems[line][i];
				if (mad != null) {
					lineList.add(mad.toNBTTag());
				}
			}

			tags.put("Line" + line, lineTag);
		}

	}

	@Override
	public void loadFromNBTTag(CompoundTag tag) {
		super.loadFromNBTTag(tag);

		Map<String, Tag> map = tag.getValue();

		for (int line = 0; line < 6; line++) {

			ListTag lineTag = (ListTag) map.get("Line" + line);
			List<Tag> lineList = lineTag.getValue();

			for (int i = 0; i < 8; i++) {
				if (lineList.size() > i) {
					ItemData mad = ItemData.fromNBTTag((CompoundTag) lineList.get(i));
					outputItems[line][i] = mad;
				}
			}
		}

	}

	//Override this method because this pipe musn't be updated
	@Override
	public void updatePipeShape() {

	}

	@Override
	public void click(Player p, BlockFace side) {
		GoldenPipeInv.openGoldenPipeInv(p, this);
	}

	public ItemData[] getOutputItems(PipeDirection pd) {
		return outputItems[pd.getId()];
	}

	public boolean isIgnoreNBT() {
		return ignoreNBT;
	}

	public void setIgnoreNBT(boolean ignoreNBT) {
		this.ignoreNBT = ignoreNBT;
	}

	public void changeOutputItems(PipeDirection pd, List<ItemData> items) {
		for (int i = 0; i < outputItems[pd.getId()].length; i++) {
			if (items.size() > i) {
				outputItems[pd.getId()][i] = items.get(i);
			} else {
				//set item null if the items size is too small -> when you take an item and decrease the items size it will don't override the item with null
				outputItems[pd.getId()][i] = null;
			}
		}
	}

	@Override
	public void destroy(boolean dropItem) {
		if (dropItem) {
			Bukkit.getScheduler().runTask(TransportPipes.instance, new Runnable() {

				@Override
				public void run() {
					blockLoc.getWorld().dropItem(blockLoc.clone().add(0.5d, 0.5d, 0.5d), TransportPipes.instance.getGoldenPipeItem());
				}
			});
		}
		for (int line = 0; line < 6; line++) {
			for (int i = 0; i < 8; i++) {
				if (outputItems[line][i] != null) {
					final ItemStack item = outputItems[line][i].toItemStack();
					//otherwise: asynchronous entity add
					Bukkit.getScheduler().runTask(TransportPipes.instance, new Runnable() {

						@Override
						public void run() {
							blockLoc.getWorld().dropItem(blockLoc.clone().add(0.5d, 0.5d, 0.5d), item);
						}
					});
				}
			}
		}
	}

}
