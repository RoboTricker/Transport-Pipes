package de.robotricker.transportpipes.protocol;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.pipeitems.RelLoc;

public class ArmorStandData {

	private RelLoc loc;
	private boolean small;
	private Vector direction;
	private ItemStack headItem;
	private ItemStack handItem;
	private Vector headRotation;
	private Vector armRotation;
	private int entityID = -1;

	public ArmorStandData(RelLoc loc, Vector direction, boolean small, ItemStack headItem, ItemStack handItem, Vector headRotation, Vector armRotation) {
		this.loc = loc;
		this.small = small;
		this.headItem = headItem;
		this.handItem = handItem;
		this.headRotation = headRotation;
		this.armRotation = armRotation;
		this.direction = direction;
	}

	public RelLoc getLoc() {
		return loc;
	}

	public boolean isSmall() {
		return small;
	}

	public ItemStack getHeadItem() {
		return headItem;
	}

	public ItemStack getHandItem() {
		return handItem;
	}

	public Vector getHeadRotation() {
		return headRotation;
	}

	public Vector getArmRotation() {
		return armRotation;
	}

	public Vector getDirection() {
		return direction;
	}

	public int getEntityID() {
		return entityID;
	}

	public void setEntityID(int entityID) {
		this.entityID = entityID;
	}

	public ArmorStandData clone(ItemStack headItem) {
		return new ArmorStandData(loc, direction, small, headItem, handItem, headRotation, armRotation);
	}

	public boolean isSimilar(ArmorStandData asd) {
		if(asd == null){
			return false;
		}
		if (RelLoc.compare(this.loc, asd.loc)) {
			if (direction.equals(asd.direction)) {
				if (small == asd.small) {
					if ((headItem != null && headItem.isSimilar(asd.headItem)) || headItem == asd.headItem) {
						if ((handItem != null && handItem.isSimilar(asd.handItem)) || handItem == asd.handItem) {
							if (headRotation.equals(asd.headRotation)) {
								if (armRotation.equals(asd.armRotation)) {
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

}
