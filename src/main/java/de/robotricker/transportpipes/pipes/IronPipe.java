package de.robotricker.transportpipes.pipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.jnbt.CompoundTag;
import org.jnbt.IntTag;
import org.jnbt.Tag;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipes.interfaces.Clickable;
import de.robotricker.transportpipes.pipes.interfaces.Editable;
import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.protocol.ArmorStandData;

public class IronPipe extends Pipe implements Editable, Clickable {

	private PipeDirection currentOutputDir;

	public IronPipe(Location blockLoc) {
		super(blockLoc);
		currentOutputDir = PipeDirection.UP;
	}

	@Override
	public PipeDirection itemArrivedAtMiddle(PipeItem item, PipeDirection before, List<PipeDirection> dirs) {
		return currentOutputDir;
	}

	@Override
	public void saveToNBTTag(HashMap<String, Tag> tags) {
		super.saveToNBTTag(tags);
		tags.put("OutputDirection", new IntTag("OutputDirection", currentOutputDir.getId()));
	}

	@Override
	public void loadFromNBTTag(CompoundTag tag) {
		super.loadFromNBTTag(tag);
		changeOutputDirection(PipeDirection.fromID(((IntTag) tag.getValue().get("OutputDirection")).getValue()));
	}

	//Override this method because this pipe musn't be updated
	@Override
	public void updatePipeShape() {

	}

	public void changeOutputDirection(PipeDirection newOutputDir) {
		if (newOutputDir != null && newOutputDir != currentOutputDir) {
			//destroy the old yellow side and the old white site + spawn the new white side and the new yellow site
			ArmorStandData oldASD1 = outputASDs.get(currentOutputDir);
			ArmorStandData newASD1 = oldASD1.clone(ITEM_CARPET_WHITE);

			currentOutputDir = newOutputDir;

			ArmorStandData oldASD2 = outputASDs.get(currentOutputDir);
			ArmorStandData newASD2 = oldASD2.clone(ITEM_CARPET_YELLOW);

			List<ArmorStandData> newList = new ArrayList<>();
			newList.add(newASD1);
			newList.add(newASD2);
			List<ArmorStandData> oldList = new ArrayList<>();
			oldList.add(oldASD1);
			oldList.add(oldASD2);

			editArmorStandDatas(newList, oldList);
		}
	}

	@Override
	public void editArmorStandDatas(List<ArmorStandData> added, List<ArmorStandData> removed) {
		getArmorStandList().addAll(added);
		getArmorStandList().removeAll(removed);
		TransportPipes.pipePacketManager.processPipeEdit(this, added, removed);
	}

	@Override
	public void click(Player p, BlockFace side) {
		changeOutputDirection(PipeDirection.fromBlockFace(side));
		p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
	}

	@Override
	public void destroy(boolean dropItem) {
		if (dropItem) {
			Bukkit.getScheduler().runTask(TransportPipes.instance, new Runnable() {

				@Override
				public void run() {
					blockLoc.getWorld().dropItem(blockLoc.clone().add(0.5d, 0.5d, 0.5d), TransportPipes.instance.getIronPipeItem());
				}
			});
		}
	}

	public PipeDirection getCurrentOutputDir() {
		return currentOutputDir;
	}

}
