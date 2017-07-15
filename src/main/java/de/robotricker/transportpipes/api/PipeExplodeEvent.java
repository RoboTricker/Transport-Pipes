package de.robotricker.transportpipes.api;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.robotricker.transportpipes.pipes.types.Pipe;

/**
 * asynchronous event!
 */
public class PipeExplodeEvent extends Event implements Cancellable {

	private boolean cancelled;
	private Pipe pipe;
	private Location loc;
	private static final HandlerList handlers = new HandlerList();

	public PipeExplodeEvent(Pipe pipe) {
		super(true);
		this.cancelled = false;
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

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public Pipe getPipe() {
		return pipe;
	}

	public Location getLocation() {
		return loc;
	}

	public int getItemAmountInPipe() {
		return pipe.pipeItems.size();
	}

}
