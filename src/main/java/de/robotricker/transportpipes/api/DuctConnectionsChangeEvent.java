package de.robotricker.transportpipes.api;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.robotricker.transportpipes.pipes.Duct;
import de.robotricker.transportpipes.pipes.WrappedDirection;
import de.robotricker.transportpipes.pipes.types.Pipe;

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
