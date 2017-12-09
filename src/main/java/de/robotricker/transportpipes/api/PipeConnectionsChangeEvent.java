package de.robotricker.transportpipes.api;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.robotricker.transportpipes.pipes.WrappedDirection;
import de.robotricker.transportpipes.pipes.types.Pipe;

public class PipeConnectionsChangeEvent extends Event {

	private Pipe pipe;
	private Location loc;
	private static final HandlerList handlers = new HandlerList();

	public PipeConnectionsChangeEvent(Pipe pipe) {
		super(true);
		this.pipe = pipe;
		this.loc = pipe.getBlockLoc();
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public Pipe getPipe() {
		return pipe;
	}

	public Location getLocation() {
		return loc;
	}

	public Collection<WrappedDirection> getPipeConnections() {
		return pipe.getAllConnections();
	}

}
