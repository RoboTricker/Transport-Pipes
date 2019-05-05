package de.robotricker.transportpipes.duct.pipe.items;

import net.querz.nbt.CompoundTag;

import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.robotricker.transportpipes.items.ItemService;
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

    public PipeItem() {
    }

    public PipeItem(ItemStack item, World world, BlockLocation blockLoc, TPDirection movingDir) {
        this.item = item;
        this.blockLoc = blockLoc;
        this.movingDir = movingDir;
        init(world, true);
    }

    public PipeItem(ItemStack item, World world, BlockLocation blockLoc, RelativeLocation relLoc, TPDirection movingDir) {
        this.item = item;
        this.blockLoc = blockLoc;
        this.relativeLocation = relLoc;
        this.movingDir = movingDir;
        init(world, false);
    }

    public void init(World world, boolean initRelLoc) {
        this.world = world;
        this.asd = new ArmorStandData(new RelativeLocation(0.25f, 0f, 0.33f), true, new Vector(1, 0, 0), new Vector(0f, 0f, 0f), new Vector(-30f, 0f, 0f), null, item);
        if (initRelLoc)
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

    public void setBlockLoc(BlockLocation blockLoc) {
        this.blockLoc = blockLoc;
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

    public void setMovingDir(TPDirection movingDir) {
        this.movingDir = movingDir;
    }

    public void saveToNBTTag(CompoundTag compoundTag, ItemService itemService) {
        compoundTag.putString("itemStack", itemService.serializeItemStack(item));
        compoundTag.putString("blockLoc", blockLoc.toString());
        compoundTag.putString("relLoc", relativeLocation.toString());
        compoundTag.putInt("movingDir", movingDir.ordinal());
    }

    public void loadFromNBTTag(CompoundTag compoundTag, World world, ItemService itemService) {
        item = itemService.deserializeItemStack(compoundTag.getString("itemStack"));
        blockLoc = BlockLocation.fromString(compoundTag.getString("blockLoc"));
        relativeLocation = RelativeLocation.fromString(compoundTag.getString("relLoc"));
        movingDir = TPDirection.values()[compoundTag.getInt("movingDir")];
        init(world, false);
    }

}
