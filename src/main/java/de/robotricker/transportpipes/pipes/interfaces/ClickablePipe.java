package de.robotricker.transportpipes.pipes.interfaces;

import org.bukkit.entity.Player;

import de.robotricker.transportpipes.pipeutils.PipeDirection;

public interface ClickablePipe {

	void click(Player p, PipeDirection side);
	
}
