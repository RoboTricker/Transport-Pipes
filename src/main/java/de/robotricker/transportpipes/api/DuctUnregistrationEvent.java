package de.robotricker.transportpipes.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.pipe.Pipe;

public class DuctUnregistrationEvent extends Event {

	private Duct duct;
	private Location loc;
	private static final HandlerList handlers = new HandlerList();

	public DuctUnregistrationEvent(Duct duct) {
		super();
		this.duct = duct;
		this.loc = duct.getBlockLoc();
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public Duct getDuct() {
		return duct;
	}

	public Location getLocation() {
		return loc;
	}

}
