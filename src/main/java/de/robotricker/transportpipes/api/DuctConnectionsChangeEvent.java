package de.robotricker.transportpipes.api;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.utils.WrappedDirection;

public class DuctConnectionsChangeEvent extends Event {

	private Duct duct;
	private Location loc;
	private static final HandlerList handlers = new HandlerList();

	public DuctConnectionsChangeEvent(Duct duct) {
		super(true);
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

	public Collection<WrappedDirection> getDuctConnections() {
		return duct.getAllConnections();
	}

}
