package de.robotricker.transportpipes.pipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jnbt.CompoundTag;
import org.jnbt.Tag;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipes.interfaces.ClickablePipe;
import de.robotricker.transportpipes.pipeutils.NBTUtils;
import de.robotricker.transportpipes.pipeutils.PipeDirection;

public class IronPipe extends Pipe implements ClickablePipe {

	private PipeDirection currentOutputDir;

	public IronPipe(Location blockLoc) {
		super(blockLoc);
		currentOutputDir = PipeDirection.UP;
	}

	@Override
	public PipeDirection calculateNextItemDirection(PipeItem item, PipeDirection before, List<PipeDirection> possibleDirs) {
		return currentOutputDir;
	}

	@Override
	public void saveToNBTTag(HashMap<String, Tag> tags) {
		super.saveToNBTTag(tags);
		NBTUtils.saveIntValue(tags, "OutputDirection", currentOutputDir.getId());
	}

	@Override
	public void loadFromNBTTag(CompoundTag tag) {
		super.loadFromNBTTag(tag);
		currentOutputDir = PipeDirection.fromID(NBTUtils.readIntTag(tag.getValue().get("OutputDirection"), 0));
	}

	public void cyleOutputDirection() {
		List<PipeDirection> connections = getAllConnections();
		if (connections.isEmpty()) {
			return;
		}

		PipeDirection oldOutputDir = currentOutputDir;

		do {
			int dirId = currentOutputDir.getId();
			dirId++;
			if (PipeDirection.fromID(dirId) == null) {
				dirId = 0;
			}
			currentOutputDir = PipeDirection.fromID(dirId);
		} while (!connections.contains(currentOutputDir));

		if (oldOutputDir != currentOutputDir) {
			TransportPipes.pipePacketManager.updatePipe(this);
		}
	}

	@Override
	public void click(Player p, PipeDirection side) {
		cyleOutputDirection();
		p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
	}

	public PipeDirection getCurrentOutputDir() {
		return currentOutputDir;
	}

	@Override
	public PipeType getPipeType() {
		return PipeType.IRON;
	}

	@Override
	public List<ItemStack> getDroppedItems() {
		List<ItemStack> is = new ArrayList<ItemStack>();
		is.add(TransportPipes.instance.getVanillaPipeItem(getPipeType(), null));
		return is;
	}

}
