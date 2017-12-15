package de.robotricker.transportpipes.utils.hitbox;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.utils.WrappedDirection;

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

	public Vector getAABBMiddle(Location blockLoc) {
		return new Vector(minx + (maxx - minx) / 2d, miny + (maxy - miny) / 2d, minz + (maxz - minz) / 2d).add(blockLoc.toVector());
	}

	public Location getMinLocation(World world) {
		return new Location(world, minx, miny, minz);
	}

	public Location getMaxLocation(World world) {
		return new Location(world, maxx, maxy, maxz);
	}
	
	public double getWidth() {
		return maxx - minx;
	}
	
	public double getHeight() {
		return maxy - miny;
	}
	
	public double getDepth() {
		return maxz - minz;
	}

	public WrappedDirection rayIntersection(Vector ray, Vector rayOrigin, Location pipeBlockLoc) {

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

		Vector intersectionPoint = rayOrigin.clone().add(ray.clone().multiply(tMin));

		Vector aabbMiddle = getAABBMiddle(pipeBlockLoc);
		Vector faceMiddle = new Vector();

		for (WrappedDirection pd : WrappedDirection.values()) {
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
			if (v <= 0.001d) {
				return pd;
			}
		}

		return null;
	}

	@Override
	public String toString() {
		return minx + ":" + miny + ":" + minz + "_" + maxx + ":" + maxy + ":" + maxz;
	}

}
