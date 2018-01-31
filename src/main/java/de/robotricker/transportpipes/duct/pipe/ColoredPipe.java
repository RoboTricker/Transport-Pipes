package de.robotricker.transportpipes.duct.pipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.duct.pipe.utils.PipeColor;
import de.robotricker.transportpipes.duct.pipe.utils.PipeType;
import de.robotricker.transportpipes.pipeitems.PipeItem;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.ductdetails.DuctDetails;
import de.robotricker.transportpipes.utils.ductdetails.PipeDetails;
import de.robotricker.transportpipes.utils.staticutils.DuctItemUtils;
import de.robotricker.transportpipes.utils.staticutils.NBTUtils;
import de.robotricker.transportpipes.utils.staticutils.UpdateUtils;

public class ColoredPipe extends Pipe {

	private PipeColor pipeColor;

	public ColoredPipe(Location blockLoc, PipeColor pipeColor) {
		super(blockLoc);
		this.pipeColor = pipeColor;
	}

	@Override
	public Map<WrappedDirection, Integer> handleArrivalAtMiddle(PipeItem item, WrappedDirection before, Collection<WrappedDirection> possibleDirs) {
		Iterator<WrappedDirection> it = possibleDirs.iterator();
		while (it.hasNext()) {
			WrappedDirection pd = it.next();
			if (pd.equals(before.getOpposite())) {
				it.remove();
			}
		}
		return getItemDistribution().splitPipeItem(item.getItem(), possibleDirs, null);
	}

	public PipeColor getPipeColor() {
		return pipeColor;
	}

	@Override
	public int[] getBreakParticleData() {
		return new int[] { getPipeColor().getDyeItem().getTypeId(), getPipeColor().getDyeItem().getDurability() };
	}

	@Override
	public PipeType getPipeType() {
		return PipeType.COLORED;
	}

	@Override
	public List<ItemStack> getDroppedItems() {
		List<ItemStack> is = new ArrayList<>();
		is.add(DuctItemUtils.getClonedDuctItem(new PipeDetails(getPipeColor())));
		return is;
	}

	@Override
	public DuctDetails getDuctDetails() {
		return new PipeDetails(getPipeColor());
	}

	@Override
	public void loadFromNBTTag(CompoundTag tag, long datFileVersion) {
		super.loadFromNBTTag(tag, datFileVersion);
		if (datFileVersion < UpdateUtils.convertVersionToLong("4.3.0")) {
			pipeColor = PipeColor.valueOf(NBTUtils.readStringTag(tag.getValue().get("PipeColor"), PipeColor.WHITE.name()));
		}
	}

}
