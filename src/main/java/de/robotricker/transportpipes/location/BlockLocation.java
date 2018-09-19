package de.robotricker.transportpipes.location;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BlockLocation implements Comparable<BlockLocation> {

    private int x;
    private int y;
    private int z;

    public BlockLocation(Location location) {
        this(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public BlockLocation(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public BlockLocation getNeighbor(Direction direction) {
        return new BlockLocation(x + direction.getVector().getBlockX(), y + direction.getVector().getBlockY(), z + direction.getVector().getBlockZ());
    }

    @Override
    public int compareTo(@NotNull BlockLocation other) {
        if (z < other.z) {
            return -1;
        } else if (z > other.z) {
            return 1;
        } else {
            if (y < other.y) {
                return -1;
            } else if (y > other.y) {
                return 1;
            } else {
                return Integer.compare(x, other.x);
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        BlockLocation blockLoc = (BlockLocation) other;
        return x == blockLoc.x &&
                y == blockLoc.y &&
                z == blockLoc.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
