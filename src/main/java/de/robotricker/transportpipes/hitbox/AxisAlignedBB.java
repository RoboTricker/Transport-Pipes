package de.robotricker.transportpipes.hitbox;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.RelativeLocation;
import de.robotricker.transportpipes.location.TPDirection;

/**
 * represents an axis aligned bounding box relative to a blocks origin. That means the coordinates are mostly inside a [0;1] range.
 * There are various methods to convert theses coordinates to world coordinates with a given block location.
 */
public class AxisAlignedBB {

    private RelativeLocation min;
    private RelativeLocation max;

    public AxisAlignedBB(double minx, double miny, double minz, double maxx, double maxy, double maxz) {
        min = new RelativeLocation(minx, miny, minz);
        max = new RelativeLocation(maxx, maxy, maxz);
    }

    public Vector getAABBMiddle(BlockLocation blockLoc) {
        return new Vector(min.getDoubleX() + (max.getDoubleX() - min.getDoubleX()) / 2d, min.getDoubleY() + (max.getDoubleY() - min.getDoubleY()) / 2d, min.getDoubleZ() + (max.getDoubleZ() - min.getDoubleZ()) / 2d).add(new Vector(blockLoc.getX(), blockLoc.getY(), blockLoc.getZ()));
    }

    public RelativeLocation getMin() {
        return min;
    }

    public RelativeLocation getMax() {
        return max;
    }

    public Location getMinLocation(World world) {
        return new Location(world, min.getDoubleX(), min.getDoubleY(), min.getDoubleZ());
    }

    public Location getMaxLocation(World world) {
        return new Location(world, max.getDoubleX(), max.getDoubleY(), max.getDoubleZ());
    }

    public double getWidth() {
        return max.getDoubleX() - min.getDoubleX();
    }

    public double getHeight() {
        return max.getDoubleY() - min.getDoubleY();
    }

    public double getDepth() {
        return max.getDoubleZ() - min.getDoubleZ();
    }

    public TPDirection performRayIntersection(Vector ray, Vector rayOrigin, BlockLocation aabbBlockLoc) {

        //optimization to decrease division operations
        Vector dirFrac = new Vector(1d / ray.getX(), 1d / ray.getY(), 1d / ray.getZ());

        double t1 = (min.getDoubleX() + aabbBlockLoc.getX() - rayOrigin.getX()) * dirFrac.getX();
        double t2 = (max.getDoubleX() + aabbBlockLoc.getX() - rayOrigin.getX()) * dirFrac.getX();
        double t3 = (min.getDoubleY() + aabbBlockLoc.getY() - rayOrigin.getY()) * dirFrac.getY();
        double t4 = (max.getDoubleY() + aabbBlockLoc.getY() - rayOrigin.getY()) * dirFrac.getY();
        double t5 = (min.getDoubleZ() + aabbBlockLoc.getZ() - rayOrigin.getZ()) * dirFrac.getZ();
        double t6 = (max.getDoubleZ() + aabbBlockLoc.getZ() - rayOrigin.getZ()) * dirFrac.getZ();

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

        Vector aabbMiddle = getAABBMiddle(aabbBlockLoc);
        Vector faceMiddle = new Vector();

        for (TPDirection tpDir : TPDirection.values()) {
            faceMiddle.setX(aabbMiddle.getX() + tpDir.getX() * (getWidth()) / 2d);
            faceMiddle.setY(aabbMiddle.getY() + tpDir.getY() * (getHeight()) / 2d);
            faceMiddle.setZ(aabbMiddle.getZ() + tpDir.getZ() * (getDepth()) / 2d);
            double v = 1d;
            if (tpDir.getX() != 0) {
                v = Math.abs(intersectionPoint.getX() - faceMiddle.getX());
            }
            if (tpDir.getY() != 0) {
                v = Math.abs(intersectionPoint.getY() - faceMiddle.getY());
            }
            if (tpDir.getZ() != 0) {
                v = Math.abs(intersectionPoint.getZ() - faceMiddle.getZ());
            }
            if (v <= 0.001d) {
                return tpDir;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return String.format("%s-%s", min, max);
    }

}
