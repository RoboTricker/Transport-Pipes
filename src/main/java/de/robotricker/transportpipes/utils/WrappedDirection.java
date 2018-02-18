package de.robotricker.transportpipes.utils;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.utils.config.LocConf;

public enum WrappedDirection {

	EAST(1, 0, 0, LocConf.UTILS_DIRECTION_EAST),
	WEST(-1, 0, 0, LocConf.UTILS_DIRECTION_WEST),
	SOUTH(0, 0, 1, LocConf.UTILS_DIRECTION_SOUTH),
	NORTH(0, 0, -1, LocConf.UTILS_DIRECTION_NORTH),
	UP(0, 1, 0, LocConf.UTILS_DIRECTION_UP),
	DOWN(0, -1, 0, LocConf.UTILS_DIRECTION_DOWN);

	private Vector v;
	private String localization_key;

	WrappedDirection(int x, int y, int z, String localization_key) {
		this.v = new Vector(x, y, z);
		this.localization_key = localization_key;
	}

	public String getDisplayName() {
		return LocConf.load(localization_key);
	}

	public Vector getVector() {
		return v.clone();
	}

	public int getX() {
		return v.getBlockX();
	}

	public int getY() {
		return v.getBlockY();
	}

	public int getZ() {
		return v.getBlockZ();
	}

	public WrappedDirection getOpposite() {
		Vector v2 = v.clone().multiply(-1f);
		for (WrappedDirection dir : WrappedDirection.values()) {
			if (v2.equals(dir.v)) {
				return dir;
			}
		}
		return null;
	}

	public int getId() {
		return this.ordinal();
	}

	public boolean isSide() {
		return this == NORTH || this == EAST || this == SOUTH || this == WEST;
	}

	public static WrappedDirection fromID(int id) {
		for (WrappedDirection pd : WrappedDirection.values()) {
			if (pd.getId() == id) {
				return pd;
			}
		}
		return null;
	}

	public static WrappedDirection fromBlockFace(BlockFace bf) {
		switch (bf) {
		case EAST:
			return WrappedDirection.EAST;
		case WEST:
			return WrappedDirection.WEST;
		case SOUTH:
			return WrappedDirection.SOUTH;
		case NORTH:
			return WrappedDirection.NORTH;
		case UP:
			return WrappedDirection.UP;
		case DOWN:
			return WrappedDirection.DOWN;
		default:
			return null;
		}
	}

	public BlockFace toBlockFace() {
		switch (this) {
		case EAST:
			return BlockFace.EAST;
		case WEST:
			return BlockFace.WEST;
		case SOUTH:
			return BlockFace.SOUTH;
		case NORTH:
			return BlockFace.NORTH;
		case UP:
			return BlockFace.UP;
		case DOWN:
			return BlockFace.DOWN;
		default:
			return null;
		}
	}

	public WrappedDirection getNextDirection() {
		if (getId() == WrappedDirection.values().length - 1) {
			return fromID(0);
		}
		return fromID(getId() + 1);
	}

}
