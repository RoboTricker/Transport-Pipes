package de.robotricker.transportpipes.pipeutils;

public enum GoldenPipeColor {

	WHITE(14, PipeDirection.NORTH),
	BLUE(15, PipeDirection.EAST),
	RED(16, PipeDirection.SOUTH),
	YELLOW(17, PipeDirection.WEST),
	GREEN(18, PipeDirection.UP),
	BLACK(19, PipeDirection.DOWN);

	private short damage;
	private PipeDirection direction;

	private GoldenPipeColor(int damage, PipeDirection direction) {
		this.damage = (short) damage;
		this.direction = direction;
	}

	public short getDamage() {
		return damage;
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
