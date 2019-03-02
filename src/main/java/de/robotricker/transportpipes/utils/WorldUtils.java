package de.robotricker.transportpipes.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WorldUtils {

    /**
     * THREAD-SAFE
     */
    public static List<Player> getPlayerList(World world) {
        // Bukkit.getOnlinePlayers is the only thread safe playerlist getter
        List<Player> playerList = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getLocation().getWorld().equals(world)) {
                playerList.add(p);
            }
        }
        return playerList;
    }

    public static boolean isContainerBlock(Material material) {
        switch (material) {
            case DISPENSER:
            case CHEST:
            case FURNACE:
            case TRAPPED_CHEST:
            case DROPPER:
            case HOPPER:
            case BREWING_STAND:
            case SHULKER_BOX:
            case WHITE_SHULKER_BOX:
            case BLACK_SHULKER_BOX:
            case BLUE_SHULKER_BOX:
            case GRAY_SHULKER_BOX:
            case BROWN_SHULKER_BOX:
            case CYAN_SHULKER_BOX:
            case GREEN_SHULKER_BOX:
            case LIGHT_BLUE_SHULKER_BOX:
            case LIGHT_GRAY_SHULKER_BOX:
            case LIME_SHULKER_BOX:
            case MAGENTA_SHULKER_BOX:
            case ORANGE_SHULKER_BOX:
            case PINK_SHULKER_BOX:
            case PURPLE_SHULKER_BOX:
            case RED_SHULKER_BOX:
            case YELLOW_SHULKER_BOX:
                return true;
            default:
                return false;
        }
    }

}
