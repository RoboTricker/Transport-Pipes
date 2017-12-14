package de.robotricker.transportpipes.duct;

import org.bukkit.entity.Player;

import de.robotricker.transportpipes.utils.WrappedDirection;

public interface ClickableDuct {

	void click(Player p, WrappedDirection side);
	
}
