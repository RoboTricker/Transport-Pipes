package de.robotricker.transportpipes.pipeutils.hitbox;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class AxisAlignedBB {

	public double minx;
	public double miny;
	public double minz;
	public double maxx;
	public double maxy;
	public double maxz;

	public AxisAlignedBB(double minx, double miny, double minz, double maxx, double maxy, double maxz) {
		this.minx = minx;
		this.miny = miny;
		this.minz = minz;
		this.maxx = maxx;
		this.maxy = maxy;
		this.maxz = maxz;
	}

	/**
	 * 
	 * checks the ray intersection with a pipe and returns the BlockFace from which the ray "is coming"
	 * 
	 * @param ray
	 *            the ray to check
	 * @param loc
	 *            the location the ray starts from
	 * @param xOffset
	 *            the integer x-Coordinate of the block the pipe is on
	 * @param yOffset
	 *            the integer y-Coordinate of the block the pipe is on
	 * @param zOffset
	 *            the integer z-Coordinate of the block the pipe is on
	 */
	public BlockFace intersectRay(Vector ray, Location loc, int xOffset, int yOffset, int zOffset) {
		ray.multiply(0.0001d);
		Location startloc = loc.clone();
		for (int i = 0; i < 100000; i++) {
			startloc.add(ray);

			//******************* x-Achse *******************
			if (startloc.getX() > minx + xOffset && startloc.getX() < minx + xOffset + 0.001) {
				if (startloc.getY() > miny + yOffset && startloc.getY() < maxy + yOffset) {
					if (startloc.getZ() > minz + zOffset && startloc.getZ() < maxz + zOffset) {
						return BlockFace.WEST;
					}
				}
			}
			if (startloc.getX() > maxx + xOffset - 0.001 && startloc.getX() < maxx + xOffset) {
				if (startloc.getY() > miny + yOffset && startloc.getY() < maxy + yOffset) {
					if (startloc.getZ() > minz + zOffset && startloc.getZ() < maxz + zOffset) {
						return BlockFace.EAST;
					}
				}
			}

			//******************* y-Achse *******************
			if (startloc.getY() > miny + yOffset && startloc.getY() < miny + yOffset + 0.001) {
				if (startloc.getX() > minx + xOffset && startloc.getX() < maxx + xOffset) {
					if (startloc.getZ() > minz + zOffset && startloc.getZ() < maxz + zOffset) {
						return BlockFace.DOWN;
					}
				}
			}
			if (startloc.getY() > maxy + yOffset - 0.001 && startloc.getY() < maxy + yOffset) {
				if (startloc.getX() > minx + xOffset && startloc.getX() < maxx + xOffset) {
					if (startloc.getZ() > minz + zOffset && startloc.getZ() < maxz + zOffset) {
						return BlockFace.UP;
					}
				}
			}

			//******************* z-Achse *******************
			if (startloc.getZ() > minz + zOffset && startloc.getZ() < minz + zOffset + 0.001) {
				if (startloc.getY() > miny + yOffset && startloc.getY() < maxy + yOffset) {
					if (startloc.getX() > minx + xOffset && startloc.getX() < maxx + xOffset) {
						return BlockFace.NORTH;
					}
				}
			}
			if (startloc.getZ() > maxz + zOffset - 0.001 && startloc.getZ() < maxz + zOffset) {
				if (startloc.getY() > miny + yOffset && startloc.getY() < maxy + yOffset) {
					if (startloc.getX() > minx + xOffset && startloc.getX() < maxx + xOffset) {
						return BlockFace.SOUTH;
					}
				}
			}
		}
		return null;
	}
}
