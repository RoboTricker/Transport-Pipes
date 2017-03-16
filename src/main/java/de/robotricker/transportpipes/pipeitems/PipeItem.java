package de.robotricker.transportpipes.pipeitems;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.pipeutils.RelLoc;
import de.robotricker.transportpipes.protocol.ArmorStandData;

public class PipeItem {

	private ArmorStandData itemArmorStand;
	private ItemStack item;
	private Location blockLoc;
	private RelLoc relLoc;
	private RelLoc relLocDiff;

	public PipeItem(ItemStack item, Location blockLoc, PipeDirection itemDir) {
		this.item = item;
		this.blockLoc = blockLoc;
		itemArmorStand = new ArmorStandData(new RelLoc(0.5f - 0.17f, 0, 0.5f - 0.17f), new Vector(1, 0, 0), true, null, item, new Vector(0f, 0f, 0f), new Vector(-30f, 0f, 0f));
		relLoc = new RelLoc(itemDir.getX() > 0 ? 0 : (itemDir.getX() < 0 ? 1 : 0.5f), itemDir.getY() > 0 ? 0 : (itemDir.getY() < 0 ? 1 : 0.5f), itemDir.getZ() > 0 ? 0 : (itemDir.getZ() < 0 ? 1 : 0.5f));
		relLocDiff = new RelLoc(0, 0, 0);
	}
	
	public ItemStack getItem(){
		return item;
	}
	
	public Location getBlockLoc(){
		return blockLoc;
	}
	
	public void setBlockLoc(Location blockLoc){
		this.blockLoc = blockLoc;
	}
	
	public RelLoc changeRelLoc(){
		return relLoc;
	}
	
	public RelLoc changeRelLocDiff(){
		return relLocDiff;
	}
	
	public ArmorStandData getArmorStand(){
		return itemArmorStand;
	}

}
