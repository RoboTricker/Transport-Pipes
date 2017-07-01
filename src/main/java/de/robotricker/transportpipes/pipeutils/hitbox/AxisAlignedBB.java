package de.robotricker.transportpipes.pipeutils.hitbox;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.pipeutils.PipeDirection;

public class AxisAlignedBB {

	private double minx;
	private double miny;
	private double minz;
	private double maxx;
	private double maxy;
	private double maxz;

	public AxisAlignedBB(double minx, double miny, double minz, double maxx, double maxy, double maxz) {
		this.minx = minx;
		this.miny = miny;
		this.minz = minz;
		this.maxx = maxx;
		this.maxy = maxy;
		this.maxz = maxz;
	}

	public Vector getAABBMiddle(Location blockLoc){
		return new Vector(minx + (maxx - minx) / 2d, miny + (maxy - miny) / 2d, minz + (maxz - minz) / 2d).add(blockLoc.toVector());
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
//	public BlockFace intersectRay(Vector ray, Location loc, int xOffset, int yOffset, int zOffset) {
//		ray.multiply(0.0001d);
//		Location startloc = loc.clone();
//		for (int i = 0; i < 100000; i++) {
//			startloc.add(ray);
//
//			//******************* x-Achse *******************
//			if (startloc.getX() > minx + xOffset && startloc.getX() < minx + xOffset + 0.001) {
//				if (startloc.getY() > miny + yOffset && startloc.getY() < maxy + yOffset) {
//					if (startloc.getZ() > minz + zOffset && startloc.getZ() < maxz + zOffset) {
//						return BlockFace.WEST;
//					}
//				}
//			}
//			if (startloc.getX() > maxx + xOffset - 0.001 && startloc.getX() < maxx + xOffset) {
//				if (startloc.getY() > miny + yOffset && startloc.getY() < maxy + yOffset) {
//					if (startloc.getZ() > minz + zOffset && startloc.getZ() < maxz + zOffset) {
//						return BlockFace.EAST;
//					}
//				}
//			}
//
//			//******************* y-Achse *******************
//			if (startloc.getY() > miny + yOffset && startloc.getY() < miny + yOffset + 0.001) {
//				if (startloc.getX() > minx + xOffset && startloc.getX() < maxx + xOffset) {
//					if (startloc.getZ() > minz + zOffset && startloc.getZ() < maxz + zOffset) {
//						return BlockFace.DOWN;
//					}
//				}
//			}
//			if (startloc.getY() > maxy + yOffset - 0.001 && startloc.getY() < maxy + yOffset) {
//				if (startloc.getX() > minx + xOffset && startloc.getX() < maxx + xOffset) {
//					if (startloc.getZ() > minz + zOffset && startloc.getZ() < maxz + zOffset) {
//						return BlockFace.UP;
//					}
//				}
//			}
//
//			//******************* z-Achse *******************
//			if (startloc.getZ() > minz + zOffset && startloc.getZ() < minz + zOffset + 0.001) {
//				if (startloc.getY() > miny + yOffset && startloc.getY() < maxy + yOffset) {
//					if (startloc.getX() > minx + xOffset && startloc.getX() < maxx + xOffset) {
//						return BlockFace.NORTH;
//					}
//				}
//			}
//			if (startloc.getZ() > maxz + zOffset - 0.001 && startloc.getZ() < maxz + zOffset) {
//				if (startloc.getY() > miny + yOffset && startloc.getY() < maxy + yOffset) {
//					if (startloc.getX() > minx + xOffset && startloc.getX() < maxx + xOffset) {
//						return BlockFace.SOUTH;
//					}
//				}
//			}
//		}
//		return null;
//	}

	public PipeDirection rayIntersection(Vector ray, Vector rayOrigin, Location pipeBlockLoc) {

		//optimization to decrease division operations
		Vector dirFrac = new Vector(1d / ray.getX(), 1d / ray.getY(), 1d / ray.getZ());

		double t1 = (minx + pipeBlockLoc.getX() - rayOrigin.getX()) * dirFrac.getX();
		double t2 = (maxx + pipeBlockLoc.getX() - rayOrigin.getX()) * dirFrac.getX();
		double t3 = (miny + pipeBlockLoc.getY() - rayOrigin.getY()) * dirFrac.getY();
		double t4 = (maxy + pipeBlockLoc.getY() - rayOrigin.getY()) * dirFrac.getY();
		double t5 = (minz + pipeBlockLoc.getZ() - rayOrigin.getZ()) * dirFrac.getZ();
		double t6 = (maxz + pipeBlockLoc.getZ() - rayOrigin.getZ()) * dirFrac.getZ();

		double tMin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
		double tMax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

		//AABB is behind player
		if (tMax < 0) {
			return null;
		}

		//don't intersect
		if (tMin > tMax) {
			return null;
		}

		double t = tMin;
		Vector intersectionPoint = rayOrigin.clone().add(ray.clone().multiply(t));

		Vector aabbMiddle = getAABBMiddle(pipeBlockLoc);
		Vector faceMiddle = new Vector();

		for (PipeDirection pd : PipeDirection.values()) {
			faceMiddle.setX(aabbMiddle.getX() + pd.getX() * (maxx - minx) / 2d);
			faceMiddle.setY(aabbMiddle.getY() + pd.getY() * (maxy - miny) / 2d);
			faceMiddle.setZ(aabbMiddle.getZ() + pd.getZ() * (maxz - minz) / 2d);
			double v = 1d;
			if (pd.getX() != 0) {
				v = Math.abs(intersectionPoint.getX() - faceMiddle.getX());
			}
			if (pd.getY() != 0) {
				v = Math.abs(intersectionPoint.getY() - faceMiddle.getY());
			}
			if (pd.getZ() != 0) {
				v = Math.abs(intersectionPoint.getZ() - faceMiddle.getZ());
			}
			System.out.println(v);
			if (v <= 0.01d) {
				return pd;
			}
		}

		return null;
	}

}
