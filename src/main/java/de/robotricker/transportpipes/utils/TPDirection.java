package de.robotricker.transportpipes.utils;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public enum TPDirection {

    EAST(1, 0, 0, BlockFace.EAST),
    WEST(-1, 0, 0, BlockFace.WEST),
    SOUTH(0, 0, 1, BlockFace.SOUTH),
    NORTH(0, 0, -1, BlockFace.NORTH),
    UP(0, 1, 0, BlockFace.UP),
    DOWN(0, -1, 0, BlockFace.DOWN);

    private Vector vec;
    private BlockFace blockFace;

    TPDirection(int x, int y, int z, BlockFace blockFace) {
        this.vec = new Vector(x, y, z);
        this.blockFace = blockFace;
    }

    public Vector getVector() {
        return vec.clone();
    }

    public int getX() {
        return vec.getBlockX();
    }

    public int getY() {
        return vec.getBlockY();
    }

    public int getZ() {
        return vec.getBlockZ();
    }

    public BlockFace getBlockFace() {
        return blockFace;
    }

    public TPDirection getOpposite() {
        return getFromBlockFace(getBlockFace().getOppositeFace());
    }

    public boolean isSide() {
        return vec.getBlockY() == 0;
    }

    public TPDirection cicle() {
        int ordinal = ordinal();
        ordinal++;
        ordinal %= values().length;
        return values()[ordinal];
    }

    public static TPDirection getFromBlockFace(BlockFace blockFace) {
        for (TPDirection tpDir : TPDirection.values()) {
            if (tpDir.getBlockFace().equals(blockFace)) {
                return tpDir;
            }
        }
        return null;
    }

}
