package main.de.robotricker.transportpipes.pipeutils;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public enum PipeDirection {

	EAST(1, 0, 0),
	WEST(-1, 0, 0),
	SOUTH(0, 0, 1),
	NORTH(0, 0, -1),
	UP(0, 1, 0),
	DOWN(0, -1, 0);

	private Vector v;

	PipeDirection(int x, int y, int z) {
		this.v = new Vector(x, y, z);
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

	public PipeDirection getOpposite() {
		Vector v2 = v.clone().multiply(-1f);
		for (PipeDirection dir : PipeDirection.values()) {
			if (v2.equals(dir.v)) {
				return dir;
			}
		}
		return null;
	}

	public int getId() {
		return this.ordinal();
	}

	//checks if this direction is north/east/west/south
	public boolean isSide() {
		return this == NORTH || this == EAST || this == SOUTH || this == WEST;
	}

	public static PipeDirection fromID(int id) {
		for (PipeDirection pd : PipeDirection.values()) {
			if (pd.getId() == id) {
				return pd;
			}
		}
		return null;
	}

	public static PipeDirection fromBlockFace(BlockFace bf) {
		switch (bf) {
		case EAST:
			return PipeDirection.EAST;
		case WEST:
			return PipeDirection.WEST;
		case SOUTH:
			return PipeDirection.SOUTH;
		case NORTH:
			return PipeDirection.NORTH;
		case UP:
			return PipeDirection.UP;
		case DOWN:
			return PipeDirection.DOWN;
		default:
			return null;
		}
	}

}
