package de.robotricker.transportpipes.pipeutils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;

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
}
