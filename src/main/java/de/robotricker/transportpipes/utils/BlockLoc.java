package de.robotricker.transportpipes.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BlockLoc implements Comparable<BlockLoc> {

    private int x;
    private int y;
    private int z;

    public BlockLoc(Location loc) {
        this(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public BlockLoc(int x, int y, int z) {
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

    public BlockLoc getNeighbor(TPDirection direction) {
        return new BlockLoc(x + direction.getVector().getBlockX(), y + direction.getVector().getBlockY(), z + direction.getVector().getBlockZ());
    }

    public Location toLocation(World world){
        return new Location(world, x, y, z);
    }

    public Block toBlock(World world){
        return toLocation(world).getBlock();
    }

    @Override
    public int compareTo(@NotNull BlockLoc o) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockLoc blockLoc = (BlockLoc) o;
        return x == blockLoc.x &&
                y == blockLoc.y &&
                z == blockLoc.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

}