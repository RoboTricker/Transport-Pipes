package de.robotricker.transportpipes.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.manager.GlobalDuctManager;
import de.robotricker.transportpipes.location.TPDirection;
import de.robotricker.transportpipes.rendersystems.RenderSystem;

public class HitboxUtils {

    private final static int HITBOX_RANGE = 5;
    private final static Set<Material> LINE_OF_SIGHT_SET;

    static {
        LINE_OF_SIGHT_SET = new HashSet<>();
        LINE_OF_SIGHT_SET.add(Material.WATER);
        LINE_OF_SIGHT_SET.add(Material.LAVA);
        LINE_OF_SIGHT_SET.add(Material.AIR);
        // add transprant blocks, so that when you look only a little above the hitbox
        // of e.g. a grass,
        // you will neverless click on the pipe unless you really click on the hitbox of
        // the grass.
        // without this code, the line of sight will stop at this transparent block and
        // won't recognize the pipe behind it.
        for (Material m : Material.values()) {
            if (m.isTransparent()) {
                LINE_OF_SIGHT_SET.add(m);
            }
        }
    }

    public static List<Block> getLineOfSight(Player p) {
        try {
            return p.getLineOfSight(LINE_OF_SIGHT_SET, HITBOX_RANGE);
        } catch (IllegalStateException ignored) {

        }
        return new ArrayList<>();
    }

    public static Duct getDuctLookingTo(GlobalDuctManager globalDuctManager, Player p, Block clickedBlock) {
        List<Block> line = getLineOfSight(p);

        Duct currentDuct = null;
        int indexOfDuctBlock = -1;

        for (int i = 0; currentDuct == null && line.size() > i; i++) {
            // check whether on this block is a duct or not
            Duct tempDuct = globalDuctManager.getDuctAtLoc(line.get(i).getLocation());
            if (tempDuct != null) {
                // check if the player looks on the hitbox of the duct (the player could
                // possibly look on a block with a duct but not on the hitbox itself)
                RenderSystem playerRenderSystem = globalDuctManager.getPlayerRenderSystem(p, tempDuct.getDuctType().getBaseDuctType());
                if (playerRenderSystem.getClickedDuctFace(p, tempDuct) != null) {
                    currentDuct = tempDuct;
                    indexOfDuctBlock = i;
                }
            }
        }

        // calculate the index of the block clicked on
        int indexOfClickedBlock = -1;
        if (clickedBlock != null) {
            if (line.contains(clickedBlock)) {
                indexOfClickedBlock = line.indexOf(clickedBlock);
            }
        }

        // check if the clicked block is before the duct block, so that you can't
        // interact with a duct behind the clicked block
        if (indexOfDuctBlock != -1 && indexOfClickedBlock != -1) {
            if (indexOfClickedBlock <= indexOfDuctBlock) {
                return null;
            }
        }
        return currentDuct;
    }

    /**
     * gets the neighbor block of the duct (where a block would be placed if right clicked) (calculated by the player direction ray
     * and the duct hitbox)
     */
    public static Block getRelativeBlockOfDuct(GlobalDuctManager globalDuctManager, Player p, Block ductLoc) {
        Duct duct = globalDuctManager.getDuctAtLoc(ductLoc.getLocation());
        if (duct == null) {
            return null;
        }
        TPDirection dir = globalDuctManager.getPlayerRenderSystem(p, duct.getDuctType().getBaseDuctType()).getClickedDuctFace(p, duct);
        return dir != null ? ductLoc.getRelative(dir.getBlockFace()) : null;
    }

}
