package de.robotricker.transportpipes.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.pipe.Pipe;

public class PlayerDestroyDuctEvent extends Event implements Cancellable {

	private boolean cancelled;
	private Player player;
	private Duct duct;
	private Location loc;
	private static final HandlerList handlers = new HandlerList();

	public PlayerDestroyDuctEvent(Player player, Duct duct) {
		super();
		this.cancelled = false;
		this.player = player;
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
	
	public Duct getDuct() {
		return duct;
	}

	public Location getLocation() {
		return loc;
	}

}
