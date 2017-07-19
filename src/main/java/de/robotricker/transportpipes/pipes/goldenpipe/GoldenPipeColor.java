package de.robotricker.transportpipes.pipes.goldenpipe;

import de.robotricker.transportpipes.pipes.PipeDirection;
import de.robotricker.transportpipes.pipeutils.config.LocConf;

public enum GoldenPipeColor {

	BLUE(15, 11, LocConf.GOLDENPIPE_COLORS_BLUE, PipeDirection.EAST),
	YELLOW(17, 4, LocConf.GOLDENPIPE_COLORS_YELLOW, PipeDirection.WEST),
	RED(16, 14, LocConf.GOLDENPIPE_COLORS_RED, PipeDirection.SOUTH),
	WHITE(14, 0, LocConf.GOLDENPIPE_COLORS_WHITE, PipeDirection.NORTH),
	GREEN(18, 5, LocConf.GOLDENPIPE_COLORS_GREEN, PipeDirection.UP),
	BLACK(19, 15, LocConf.GOLDENPIPE_COLORS_BLACK, PipeDirection.DOWN);

	private short resourcePackDamage;
	private short itemDamage;
	private String locConfKey;
	private PipeDirection direction;

	GoldenPipeColor(int resourcePackDamage, int itemDamage, String locConfKey, PipeDirection direction) {
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

	public PipeDirection getDirection() {
		return direction;
	}

	public static GoldenPipeColor getColorWithDirection(PipeDirection direction) {
		for (GoldenPipeColor gpc : GoldenPipeColor.values()) {
			if (gpc.getDirection().equals(direction)) {
				return gpc;
			}
		}
		return null;
	}

}
