package main.de.robotricker.transportpipes.pipes.interfaces;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public interface Clickable {

	void click(Player p, BlockFace side);
	
}
