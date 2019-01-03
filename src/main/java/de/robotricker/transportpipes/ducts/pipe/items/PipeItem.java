package de.robotricker.transportpipes.ducts.pipe.items;

import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.RelativeLocation;
import de.robotricker.transportpipes.location.TPDirection;
import de.robotricker.transportpipes.protocol.ArmorStandData;

public class PipeItem {

    private ArmorStandData asd;
    private ItemStack item;
    private World world;
    private BlockLocation blockLoc;
    private RelativeLocation oldRelativeLocation;
    private RelativeLocation relativeLocation;
    private TPDirection movingDir;
    private boolean spawned;

    public PipeItem(ItemStack item, World world, BlockLocation blockLoc, TPDirection movingDir) {
        this.item = item;
        this.world = world;
        this.blockLoc = blockLoc;
        this.movingDir = movingDir;
        this.asd = new ArmorStandData(new RelativeLocation(0.25f, 0f, 0.33f), true, new Vector(1, 0, 0), new Vector(0f, 0f, 0f), new Vector(-30f, 0f, 0f), null, item);
        this.relativeLocation = new RelativeLocation(movingDir.getX() > 0 ? 0 : (movingDir.getX() < 0 ? 1 : 0.5f), movingDir.getY() > 0 ? 0 : (movingDir.getY() < 0 ? 1 : 0.5f), movingDir.getZ() > 0 ? 0 : (movingDir.getZ() < 0 ? 1 : 0.5f));
        resetOldRelativeLocation();
    }

    public ArmorStandData getAsd() {
        return asd;
    }

    public ItemStack getItem() {
        return item;
    }

    public World getWorld() {
        return world;
    }

    public BlockLocation getBlockLoc() {
        return blockLoc;
    }

    public RelativeLocation getOldRelativeLocation() {
        return oldRelativeLocation;
    }

    public RelativeLocation getRelativeLocation() {
        return relativeLocation;
    }

    public RelativeLocation getRelativeLocationDifference() {
        return relativeLocation.clone().add(-oldRelativeLocation.getLongX(), -oldRelativeLocation.getLongY(), -oldRelativeLocation.getLongZ());
    }

    public void resetOldRelativeLocation() {
        oldRelativeLocation = relativeLocation.clone();
    }

    public TPDirection getMovingDir() {
        return movingDir;
    }

    public boolean isSpawned() {
        return spawned;
    }

    public void setBlockLoc(BlockLocation blockLoc) {
        this.blockLoc = blockLoc;
    }

    public void setMovingDir(TPDirection movingDir) {
        this.movingDir = movingDir;
    }
}
