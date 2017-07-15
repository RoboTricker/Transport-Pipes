package de.robotricker.transportpipes.pipes.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jnbt.CompoundTag;
import org.jnbt.NBTTagType;
import org.jnbt.Tag;

import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipes.ClickablePipe;
import de.robotricker.transportpipes.pipes.PipeDirection;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipes.goldenpipe.GoldenPipeInv;
import de.robotricker.transportpipes.pipeutils.NBTUtils;
import de.robotricker.transportpipes.pipeutils.PipeItemUtils;

public class GoldenPipe extends Pipe implements ClickablePipe {

	//1st dimension: output dirs in order of PipeDirection.values() | 2nd dimension: output items in this direction
	private ItemData[][] outputItems = new ItemData[6][8];
	private boolean ignoreNBT = false;

	public GoldenPipe(Location blockLoc) {
		super(blockLoc);
	}

	@Override
	public PipeDirection calculateNextItemDirection(PipeItem item, PipeDirection before, List<PipeDirection> possibleDirs) {
		ItemData itemData = new ItemData(item.getItem());
		List<PipeDirection> possibleDirections = getPossibleDirectionsForItem(itemData, before);
		return possibleDirections.get(new Random().nextInt(possibleDirections.size()));
	}

	public List<PipeDirection> getPossibleDirectionsForItem(ItemData itemData, PipeDirection before) {
		//all directions in which is an other pipe or inventory-block
		List<PipeDirection> connectionDirections = getAllConnections();

		//the possible directions in which the item could go
		List<PipeDirection> possibleDirections = new ArrayList<PipeDirection>();

		List<PipeDirection> emptyPossibleDirections = new ArrayList<PipeDirection>();

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
			if (empty) {
				emptyPossibleDirections.add(dir);
			}
		}

		//if this item isn't in the list, it will take a random direction from the empty dirs
		if (possibleDirections.isEmpty()) {

			for (PipeDirection dir : emptyPossibleDirections) {
				//add all possible empty directions without the direction back. Only if this is the only possible way, it will go back.
				if (dir != before.getOpposite() || emptyPossibleDirections.size() == 1) {
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
			for (int i = 0; i < 8; i++) {
				ItemData itemData = outputItems[line][i];
				if (itemData != null) {
					lineList.add(itemData.toNBTTag());
				}
			}
			NBTUtils.saveListValue(tags, "Line" + line, NBTTagType.TAG_COMPOUND, lineList);
		}

		NBTUtils.saveByteValue(tags, "IgnoreNBT", ignoreNBT ? (byte) 1 : (byte) 0);

	}

	@Override
	public void loadFromNBTTag(CompoundTag tag) {
		super.loadFromNBTTag(tag);

		Map<String, Tag> map = tag.getValue();
		for (int line = 0; line < 6; line++) {

			List<Tag> lineList = NBTUtils.readListTag(map.get("Line" + line));
			for (int i = 0; i < 8; i++) {
				if (lineList.size() > i) {
					ItemData itemData = ItemData.fromNBTTag((CompoundTag) lineList.get(i));
					outputItems[line][i] = itemData;
				}
			}
		}

		boolean ignoreNBT = NBTUtils.readByteTag(map.get("IgnoreNBT"), (byte) 0) == (byte) 1;
		setIgnoreNBT(ignoreNBT);

	}

	@Override
	public void click(Player p, PipeDirection side) {
		GoldenPipeInv.updateGoldenPipeInventory(p, this);
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
			if (i < items.size()) {
				outputItems[pd.getId()][i] = items.get(i);
			} else {
				outputItems[pd.getId()][i] = null;
			}
		}
	}

	@Override
	public PipeType getPipeType() {
		return PipeType.GOLDEN;
	}

	@Override
	public List<ItemStack> getDroppedItems() {
		List<ItemStack> is = new ArrayList<ItemStack>();
		is.add(PipeItemUtils.getPipeItem(getPipeType(), null));
		for (int line = 0; line < 6; line++) {
			for (int i = 0; i < 8; i++) {
				if (outputItems[line][i] != null) {
					is.add(outputItems[line][i].toItemStack());
				}
			}
		}
		return is;
	}

}
