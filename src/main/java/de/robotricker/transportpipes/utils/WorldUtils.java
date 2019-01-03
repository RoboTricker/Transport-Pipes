package de.robotricker.transportpipes.utils;

import org.bukkit.Bukkit;
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

}
