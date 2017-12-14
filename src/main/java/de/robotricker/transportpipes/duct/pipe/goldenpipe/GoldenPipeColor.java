package de.robotricker.transportpipes.duct.pipe.goldenpipe;

import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.config.LocConf;

public enum GoldenPipeColor {

	BLUE(15, 11, LocConf.GOLDENPIPE_COLORS_BLUE, WrappedDirection.EAST),
	YELLOW(17, 4, LocConf.GOLDENPIPE_COLORS_YELLOW, WrappedDirection.WEST),
	RED(16, 14, LocConf.GOLDENPIPE_COLORS_RED, WrappedDirection.SOUTH),
	WHITE(14, 0, LocConf.GOLDENPIPE_COLORS_WHITE, WrappedDirection.NORTH),
	GREEN(18, 5, LocConf.GOLDENPIPE_COLORS_GREEN, WrappedDirection.UP),
	BLACK(19, 15, LocConf.GOLDENPIPE_COLORS_BLACK, WrappedDirection.DOWN);

	private short resourcePackDamage;
	private short itemDamage;
	private String locConfKey;
	private WrappedDirection direction;

	GoldenPipeColor(int resourcePackDamage, int itemDamage, String locConfKey, WrappedDirection direction) {
		this.resourcePackDamage = (short) resourcePackDamage;
		this.itemDamage = (short) itemDamage;
		this.locConfKey = locConfKey;
		this.direction = direction;
	}

	public short getResourcePackDamage() {
		return resourcePackDamage;
	}

	public short getItemDamage() {
		return itemDamage;
	}

	public String getLocConfKey() {
		return locConfKey;
	}

	public WrappedDirection getDirection() {
		return direction;
	}

	public static GoldenPipeColor getColorWithDirection(WrappedDirection direction) {
		for (GoldenPipeColor gpc : GoldenPipeColor.values()) {
			if (gpc.getDirection().equals(direction)) {
				return gpc;
			}
		}
		return null;
	}

}
