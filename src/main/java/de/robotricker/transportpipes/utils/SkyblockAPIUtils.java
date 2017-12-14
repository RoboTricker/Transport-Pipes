package de.robotricker.transportpipes.utils;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import de.robotricker.transportpipes.api.PipeAPI;

public class SkyblockAPIUtils implements Listener {

	@EventHandler
	public void onReset(com.wasteofplastic.acidisland.events.IslandResetEvent e) {
		destroyAllDuctsOnIsland(e.getLocation());
	}

	@EventHandler
	public void onReset(com.wasteofplastic.acidisland.events.IslandDeleteEvent e) {
		destroyAllDuctsOnIsland(e.getLocation());
	}

	private void destroyAllDuctsOnIsland(Location islandLoc) {
		int dist = 200;
		final Location min = islandLoc.clone().add(-dist, 0, -dist);
		min.setY(0);
		final Location max = islandLoc.clone().add(dist, 0, dist);
		max.setY(max.getWorld().getMaxHeight());

		com.wasteofplastic.acidisland.ASkyBlockAPI skyblockApi = com.wasteofplastic.acidisland.ASkyBlockAPI.getInstance();

		Location tempLoc = new Location(min.getWorld(), 0, 0, 0);
		for (int x = min.getBlockX(); x < max.getBlockX(); x++) {
			for (int y = min.getBlockY(); y < max.getBlockY(); y++) {
				for (int z = min.getBlockZ(); z < max.getBlockZ(); z++) {
					tempLoc.setX(x);
					tempLoc.setY(y);
					tempLoc.setZ(z);
					if (PipeAPI.isDuct(tempLoc, null) && !skyblockApi.islandAtLocation(tempLoc)) {
						PipeAPI.destroyDuct(tempLoc);
					}
				}
			}
		}
	}

}
