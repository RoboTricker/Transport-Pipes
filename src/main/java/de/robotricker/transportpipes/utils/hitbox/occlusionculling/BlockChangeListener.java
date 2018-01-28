package de.robotricker.transportpipes.utils.hitbox.occlusionculling;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import de.robotricker.transportpipes.TransportPipes;
import io.sentry.Sentry;

public class BlockChangeListener implements Listener {

	public Map<ChunkCoords, ChunkSnapshot> cachedChunkSnapshots = Collections.synchronizedMap(new HashMap<ChunkCoords, ChunkSnapshot>());

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent e) {
		Chunk chunk = e.getBlock().getChunk();
		updateCachedChunkSync(new ChunkCoords(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()), chunk);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e) {
		Chunk chunk = e.getBlock().getChunk();
		updateCachedChunkSync(new ChunkCoords(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()), chunk);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChunkLoad(ChunkLoadEvent e) {
		handleChunkLoadSync(e.getChunk());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChunkUnload(ChunkUnloadEvent e) {
		Chunk chunk = e.getChunk();
		updateCachedChunkSync(new ChunkCoords(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()), null);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockExplode(BlockExplodeEvent e) {
		handleExplosionSync(e.blockList());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent e) {
		handleExplosionSync(e.blockList());
	}

	public void handleChunkLoadSync(Chunk loadedChunk) {
		updateCachedChunkSync(new ChunkCoords(loadedChunk.getWorld().getName(), loadedChunk.getX(), loadedChunk.getZ()), loadedChunk);
		TransportPipes.instance.containerBlockUtils.handleChunkLoadSync(loadedChunk);
	}

	public void handleExplosionSync(List<Block> blockList) {
		Set<ChunkCoords> chunks = new HashSet<>();
		for (Block block : blockList) {
			int chunkX = (int) Math.floor(block.getX() / 16d);
			int chunkZ = (int) Math.floor(block.getZ() / 16d);
			chunks.add(new ChunkCoords(block.getWorld().getName(), chunkX, chunkZ));
		}
		for (ChunkCoords cc : chunks) {
			updateCachedChunkSync(cc, cc.getRealChunkSync());
		}
	}

	public void updateCachedChunkSync(final ChunkCoords cc, final Chunk chunk) {
		if (chunk == null) {
			cachedChunkSnapshots.remove(cc);
			return;
		}
		TransportPipes.runTask(new Runnable() {

			@Override
			public void run() {
				cachedChunkSnapshots.put(cc, chunk.getChunkSnapshot());
			}
		});
	}

	public boolean isInLoadedChunk(Location loc) {
		try {
			int chunkX = (int) Math.floor(loc.getBlockX() / 16d);
			int chunkZ = (int) Math.floor(loc.getBlockZ() / 16d);
			ChunkCoords cc = new ChunkCoords(loc.getWorld().getName(), chunkX, chunkZ);
			return cachedChunkSnapshots.containsKey(cc);
		} catch (Exception exception) {
			exception.printStackTrace();
			Sentry.capture(exception);
		}
		return false;
	}

	public static class ChunkCoords {
		public String worldName;
		public int chunkX;
		public int chunkZ;

		public ChunkCoords(String worldName, int chunkX, int chunkZ) {
			this.worldName = worldName;
			this.chunkX = chunkX;
			this.chunkZ = chunkZ;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + chunkX;
			result = prime * result + chunkZ;
			result = prime * result + ((worldName == null) ? 0 : worldName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ChunkCoords other = (ChunkCoords) obj;
			if (chunkX != other.chunkX)
				return false;
			if (chunkZ != other.chunkZ)
				return false;
			if (worldName == null) {
				if (other.worldName != null)
					return false;
			} else if (!worldName.equals(other.worldName))
				return false;
			return true;
		}

		public Chunk getRealChunkSync() {
			World world = Bukkit.getWorld(worldName);
			return world.getChunkAt(chunkX, chunkZ);
		}

	}

}
