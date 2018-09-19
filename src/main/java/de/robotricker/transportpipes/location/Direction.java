package de.robotricker.transportpipes.location;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public enum Direction {

    EAST(1, 0, 0, BlockFace.EAST),
    WEST(-1, 0, 0, BlockFace.WEST),
    SOUTH(0, 0, 1, BlockFace.SOUTH),
    NORTH(0, 0, -1, BlockFace.NORTH),
    UP(0, 1, 0, BlockFace.UP),
    DOWN(0, -1, 0, BlockFace.DOWN);

    private Vector vector;
    private BlockFace blockFace;

    Direction(int x, int y, int z, BlockFace blockFace) {
        this.vector = new Vector(x, y, z);
        this.blockFace = blockFace;
    }

    public Vector getVector() {
        return vector.clone();
    }

    public int getX() {
        return vector.getBlockX();
    }

    public int getY() {
        return vector.getBlockY();
    }

    public int getZ() {
        return vector.getBlockZ();
    }

    public BlockFace getBlockFace() {
        return blockFace;
    }

    public Direction getOpposite() {
        return getFromBlockFace(getBlockFace().getOppositeFace());
    }

    public boolean isSide() {
        return vector.getBlockY() == 0;
    }

    public Direction cycle() {
        int ordinal = ordinal();
        ordinal++;
        ordinal %= values().length;
        return values()[ordinal];
    }

    public static Direction getFromBlockFace(BlockFace blockFace) {
        for (Direction direction : Direction.values()) {
            if (direction.getBlockFace().equals(blockFace)) {
                return direction;
            }
        }
        return null;
    }
}
