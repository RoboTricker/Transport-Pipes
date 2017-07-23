package de.robotricker.transportpipes.pipes;

import java.util.Objects;

import org.bukkit.Location;

public class BlockLoc implements Comparable<BlockLoc> {

	private int x;
	private int y;
	private int z;

	public BlockLoc(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double distanceSquared(BlockLoc bl) {
		return Math.pow(x - bl.x, 2) + Math.pow(y - bl.y, 2) + Math.pow(z - bl.z, 2);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof BlockLoc)) {
			return false;
		}
		BlockLoc bl = (BlockLoc) obj;
		return bl.x == x && bl.y == y && bl.z == z;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y, z);
	}

	@Override
	public int compareTo(BlockLoc o) {
		if (z < o.z) {
			return -1;
		} else if (z > o.z) {
			return 1;
		} else {
			if (y < o.y) {
				return -1;
			} else if (y > o.y) {
				return 1;
			} else {
				if (x < o.x) {
					return -1;
				} else if (x > o.x) {
					return 1;
				} else {
					return 0;
				}
			}
		}
	}

	public static BlockLoc convertBlockLoc(Location blockLoc) {
		return new BlockLoc(blockLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ());
	}

}