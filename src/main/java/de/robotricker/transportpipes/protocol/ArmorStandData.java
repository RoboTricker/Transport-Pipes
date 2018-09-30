package de.robotricker.transportpipes.protocol;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Objects;

import de.robotricker.transportpipes.location.RelativeLocation;

public class ArmorStandData implements Cloneable {

    private RelativeLocation relLoc;
    private boolean small;
    private Vector direction;
    private Vector headRotation;
    private Vector armRotation;
    private ItemStack headItem;
    private ItemStack handItem;
    private int entityID = -1;

    public ArmorStandData(RelativeLocation relLoc, boolean small, Vector direction, Vector headRotation, Vector armRotation, ItemStack headItem, ItemStack handItem) {
        this.relLoc = relLoc;
        this.small = small;
        this.direction = direction;
        this.headRotation = headRotation;
        this.armRotation = armRotation;
        this.headItem = headItem;
        this.handItem = handItem;
    }

    public RelativeLocation getRelLoc() {
        return relLoc;
    }

    public boolean isSmall() {
        return small;
    }

    public Vector getDirection() {
        return direction;
    }

    public Vector getHeadRotation() {
        return headRotation;
    }

    public Vector getArmRotation() {
        return armRotation;
    }

    public ItemStack getHeadItem() {
        return headItem;
    }

    public ItemStack getHandItem() {
        return handItem;
    }

    public int getEntityID() {
        return entityID;
    }

    public void setEntityID(int entityID) {
        this.entityID = entityID;
    }

    @Override
    public ArmorStandData clone() {
        return new ArmorStandData(relLoc, small, direction, headRotation, armRotation, headItem, handItem);
    }

    public boolean isSimilar(ArmorStandData armorStandData) {
        if (armorStandData == null) {
            return false;
        }
        return Objects.equals(relLoc, armorStandData.relLoc) &&
                small == armorStandData.small &&
                Objects.equals(direction, armorStandData.direction) &&
                Objects.equals(headRotation, armorStandData.headRotation) &&
                Objects.equals(armRotation, armorStandData.armRotation) &&
                Objects.equals(headItem, armorStandData.headItem) &&
                Objects.equals(handItem, armorStandData.handItem);
    }

}
