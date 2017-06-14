package de.robotricker.transportpipes.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.robotricker.transportpipes.pipes.Pipe;

public class PlayerDestroyPipeEvent extends Event implements Cancellable {

	private boolean cancelled;
	private Player player;
	private Pipe pipe;
	private Location loc;
	private static final HandlerList handlers = new HandlerList();

	public PlayerDestroyPipeEvent(Player player, Pipe pipe) {
		super();
		this.cancelled = false;
		this.player = player;
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

	public Player getPlayer(){
		return player;
	}
	
	public Pipe getPipe() {
		return pipe;
	}

	public Location getLocation() {
		return loc;
	}

}
