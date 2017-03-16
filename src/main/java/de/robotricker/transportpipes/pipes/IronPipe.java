package de.robotricker.transportpipes.pipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jnbt.CompoundTag;
import org.jnbt.IntTag;
import org.jnbt.Tag;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.pipes.interfaces.Clickable;
import de.robotricker.transportpipes.pipes.interfaces.Editable;
import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.pipeutils.RelLoc;
import de.robotricker.transportpipes.pipeutils.hitbox.AxisAlignedBB;
import de.robotricker.transportpipes.protocol.ArmorStandData;

public class IronPipe extends Pipe implements Editable, Clickable {

	private HashMap<PipeDirection, ArmorStandData> outputASDs = new HashMap<PipeDirection, ArmorStandData>();
	private PipeDirection currentOutputDir;

	public IronPipe(Location blockLoc, List<PipeDirection> pipeNeighborBlocks) {
		//PipeLoc | Body Direction | isSmall | HeadItem | HandItem | headRotation | handRotation
		//@formatter:off
		super(blockLoc, new AxisAlignedBB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75), pipeNeighborBlocks,
				new ArmorStandData(new RelLoc(0.5f, -0.43f, 0.5f), new Vector(1, 0, 0), true, ITEM_IRON_BLOCK, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)),
				new ArmorStandData(new RelLoc(0.5f + 0.26f, -0.255f, 0.5f), new Vector(1, 0, 0), true, ITEM_CARPET_WHITE, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f)),
				new ArmorStandData(new RelLoc(0.5f - 0.26f, -0.255f, 0.5f), new Vector(-1, 0, 0), true, ITEM_CARPET_WHITE, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f)),
				new ArmorStandData(new RelLoc(0.5f, -0.255f, 0.5f + 0.26f), new Vector(0, 0, 1), true, ITEM_CARPET_WHITE, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f)),
				new ArmorStandData(new RelLoc(0.5f, -0.255f, 0.5f - 0.26f), new Vector(0, 0, -1), true, ITEM_CARPET_WHITE, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f)),
				new ArmorStandData(new RelLoc(0.5f, -0.255f + 0.26f, 0.5f), new Vector(1, 0, 0), true, ITEM_CARPET_YELLOW, null, new Vector(0f, 0f, 0f), new Vector(0f, 0f, 0f)),
				new ArmorStandData(new RelLoc(0.5f, -0.255f - 0.26f, 0.5f), new Vector(1, 0, 0), true, ITEM_CARPET_WHITE, null, new Vector(180f, 0f, 0f), new Vector(0f, 0f, 0f)));
		//@formatter:on
		for (int i = 0; i < PipeDirection.values().length; i++) {
			ArmorStandData tempASD = getArmorStandList().get(i + 1);
			if (tempASD.getHeadItem() != null && tempASD.getHeadItem().equals(ITEM_CARPET_YELLOW)) {
				currentOutputDir = PipeDirection.values()[i];
			}
			outputASDs.put(PipeDirection.values()[i], tempASD);
		}
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

			List<ArmorStandData> newList = new ArrayList<ArmorStandData>();
			newList.add(newASD1);
			newList.add(newASD2);
			List<ArmorStandData> oldList = new ArrayList<ArmorStandData>();
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
					blockLoc.getWorld().dropItem(blockLoc.clone().add(0.5d, 0.5d, 0.5d), TransportPipes.IRON_PIPE_ITEM);
				}
			});
		}
	}

}
