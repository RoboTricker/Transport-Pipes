package de.robotricker.transportpipes.pipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jnbt.CompoundTag;
import org.jnbt.Tag;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipeutils.NBTUtils;
import de.robotricker.transportpipes.pipeutils.PipeColor;
import de.robotricker.transportpipes.pipeutils.PipeDirection;

public class ColoredPipe extends Pipe {

	private PipeColor pipeColor;
	private int lastOutputIndex = 0;

	public ColoredPipe(Location blockLoc, PipeColor pipeColor) {
		super(blockLoc);
		this.pipeColor = pipeColor;
	}

	@Override
	public PipeDirection calculateNextItemDirection(PipeItem item, PipeDirection before, List<PipeDirection> possibleDirs) {
		if (possibleDirs.contains(before.getOpposite())) {
			possibleDirs.remove(before.getOpposite());
		}
		lastOutputIndex++;
		if (lastOutputIndex >= possibleDirs.size()) {
			lastOutputIndex = 0;
		}
		if (possibleDirs.size() > 0) {
			return possibleDirs.get(lastOutputIndex);
		}
		return before;
	}

	public PipeColor getPipeColor() {
		return pipeColor;
	}

	@Override
	public PipeType getPipeType() {
		return PipeType.COLORED;
	}

	@Override
	public List<ItemStack> getDroppedItems(Player p) {
		List<ItemStack> is = new ArrayList<ItemStack>();
		is.add(TransportPipes.instance.getPipeItemForPlayer(p, PipeType.COLORED, getPipeColor()));
		return is;
	}

	@Override
	public void saveToNBTTag(HashMap<String, Tag> tags) {
		super.saveToNBTTag(tags);
		NBTUtils.saveStringValue(tags, "PipeColor", pipeColor.name());
	}

	@Override
	public void loadFromNBTTag(CompoundTag tag) {
		super.loadFromNBTTag(tag);
		pipeColor = PipeColor.valueOf(NBTUtils.readStringTag(tag.getValue().get("PipeColor"), PipeColor.WHITE.name()));
	}

}
