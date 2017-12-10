package de.robotricker.transportpipes.pipes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.api.PipeConnectionsChangeEvent;
import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.rendersystem.RenderSystem;

public abstract class Duct {

	// the blockLoc of this duct
	protected Location blockLoc;
	protected Chunk cachedChunk;

	public Duct(Location blockLoc) {
		this.blockLoc = blockLoc;
		this.cachedChunk = blockLoc.getBlock().getChunk();
	}

	public Location getBlockLoc() {
		return blockLoc;
	}

	public boolean isInLoadedChunk() {
		return cachedChunk.isLoaded();
	}

	/**
	 * get the items that will be dropped on pipe destroy
	 */
	public abstract List<ItemStack> getDroppedItems();

	public abstract int[] getBreakParticleData();

	public void notifyConnectionsChange() {
		PipeConnectionsChangeEvent event = new PipeConnectionsChangeEvent(this);
		Bukkit.getPluginManager().callEvent(event);
	}

	public Collection<WrappedDirection> getAllConnections() {
		Set<WrappedDirection> connections = new HashSet<>();
		connections.addAll(getOnlyDuctConnections());
		connections.addAll(getOnlyBlockConnections());
		return connections;
	}

	public abstract boolean canConnectToDuct(Duct duct);

	/**
	 * gets all duct connection directions (not block connections)
	 **/
	public List<WrappedDirection> getOnlyDuctConnections() {
		List<WrappedDirection> dirs = new ArrayList<>();

		Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(getBlockLoc().getWorld());

		if (ductMap != null) {
			for (WrappedDirection dir : WrappedDirection.values()) {
				Location blockLoc = getBlockLoc().clone().add(dir.getX(), dir.getY(), dir.getZ());
				BlockLoc bl = BlockLoc.convertBlockLoc(blockLoc);
				if (ductMap.containsKey(bl)) {
					Duct neighborDuct = ductMap.get(bl);
					if (canConnectToDuct(neighborDuct)) {
						dirs.add(dir);
					}
				}
			}
		}

		return dirs;
	}
	
	/**
	 * gets all block connection directions (not duct connections)
	 **/
	public abstract List<WrappedDirection> getOnlyBlockConnections();
	
	public abstract List<RenderSystem> getRenderSystems();

}
