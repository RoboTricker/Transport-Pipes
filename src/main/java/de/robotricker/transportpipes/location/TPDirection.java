package de.robotricker.transportpipes.location;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.config.LangConf;

public enum TPDirection {

    EAST(1, 0, 0, BlockFace.EAST, LangConf.Key.DIRECTIONS_EAST.get()),
    WEST(-1, 0, 0, BlockFace.WEST, LangConf.Key.DIRECTIONS_WEST.get()),
    SOUTH(0, 0, 1, BlockFace.SOUTH, LangConf.Key.DIRECTIONS_SOUTH.get()),
    NORTH(0, 0, -1, BlockFace.NORTH, LangConf.Key.DIRECTIONS_NORTH.get()),
    UP(0, 1, 0, BlockFace.UP, LangConf.Key.DIRECTIONS_UP.get()),
    DOWN(0, -1, 0, BlockFace.DOWN, LangConf.Key.DIRECTIONS_DOWN.get());

    private Vector vec;
    private BlockFace blockFace;
    private String displayName;

    TPDirection(int x, int y, int z, BlockFace blockFace, String displayName) {
        this.vec = new Vector(x, y, z);
        this.blockFace = blockFace;
        this.displayName = displayName;
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
        return fromBlockFace(getBlockFace().getOppositeFace());
    }

    public boolean isSide() {
        return vec.getBlockY() == 0;
    }

    public String getDisplayName() {
        return displayName;
    }

    public TPDirection next() {
        int ordinal = ordinal();
        ordinal++;
        ordinal %= values().length;
        return values()[ordinal];
    }

    public static TPDirection fromBlockFace(BlockFace blockFace) {
        for (TPDirection tpDir : TPDirection.values()) {
            if (tpDir.getBlockFace().equals(blockFace)) {
                return tpDir;
            }
        }
        return null;
    }

}
