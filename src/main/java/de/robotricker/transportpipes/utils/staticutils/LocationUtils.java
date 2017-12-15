package de.robotricker.transportpipes.utils.staticutils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class LocationUtils {

	public static List<Block> getNearbyBlocks(Location location, int radius) {
		List<Block> blocks = new ArrayList<>();
		Block middle = location.getBlock();
		for (int x = radius; x >= -radius; x--) {
			for (int y = radius; y >= -radius; y--) {
				for (int z = radius; z >= -radius; z--) {
					blocks.add(middle.getRelative(x, y, z));
				}
			}
		}
		return blocks;
	}

	public static List<Player> getPlayerList(World world) {
		// Bukkit.getOnlinePlayers is the only thread safe playerlist getter
		List<Player> playerList = new ArrayList<Player>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.getLocation().getWorld().equals(world)) {
				playerList.add(p);
			}
		}
		return playerList;
	}

	public static String LocToString(Location loc) {
		return loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ();
	}

	public static Location StringToLoc(String loc) {
		try {
			return new Location(Bukkit.getWorld(loc.split(":")[0]), Double.parseDouble(loc.split(":")[1]), Double.parseDouble(loc.split(":")[2]), Double.parseDouble(loc.split(":")[3]));
		} catch (Exception e) {
			return null;
		}
	}

}
