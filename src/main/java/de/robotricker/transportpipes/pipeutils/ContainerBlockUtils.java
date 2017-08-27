package de.robotricker.transportpipes.pipeutils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.InventoryHolder;

import de.robotricker.transportpipes.PipeThread;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.api.PipeAPI;
import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.container.BlockContainer;
import de.robotricker.transportpipes.container.BrewingStandContainer;
import de.robotricker.transportpipes.container.FurnaceContainer;
import de.robotricker.transportpipes.container.SimpleInventoryContainer;
import de.robotricker.transportpipes.pipes.BlockLoc;
import de.robotricker.transportpipes.pipes.types.Pipe;

public class ContainerBlockUtils implements Listener {

	private Map<World, List<Chunk>> loadedChunksGridCoords;

	public ContainerBlockUtils() {
		loadedChunksGridCoords = Collections.synchronizedMap(new HashMap<World, List<Chunk>>());
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent e) {
		if (isIdContainerBlock(e.getBlockPlaced().getTypeId())) {
			updatePipeNeighborBlockSync(e.getBlock(), true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e) {
		if (isIdContainerBlock(e.getBlock().getTypeId())) {
			updatePipeNeighborBlockSync(e.getBlock(), false);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockExplode(BlockExplodeEvent e) {
		for (Block b : e.blockList()) {
			if (isIdContainerBlock(b.getTypeId())) {
				updatePipeNeighborBlockSync(b, false);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent e) {
		for (Block b : e.blockList()) {
			if (isIdContainerBlock(b.getTypeId())) {
				updatePipeNeighborBlockSync(b, false);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onExplosionPrime(ExplosionPrimeEvent e) {
		if (!TransportPipes.instance.generalConf.isDestroyPipeOnExplosion()) {
			return;
		}
		Map<BlockLoc, Pipe> pipeMap = TransportPipes.instance.getPipeMap(e.getEntity().getWorld());
		if (pipeMap == null) {
			return;
		}

		List<Block> explodedPipeBlocksBefore = new ArrayList<Block>();
		List<Block> explodedPipeBlocks = new ArrayList<Block>();

		for (Block block : LocationUtils.getNearbyBlocks(e.getEntity().getLocation(),
				(int) Math.floor(e.getRadius()))) {
			BlockLoc blockLoc = BlockLoc.convertBlockLoc(block.getLocation());
			Pipe pipe = pipeMap.get(blockLoc);
			if (pipe != null) {
				pipe.getBlockLoc().getBlock().setType(Material.GLASS);
				explodedPipeBlocksBefore.add(pipe.getBlockLoc().getBlock());
				explodedPipeBlocks.add(pipe.getBlockLoc().getBlock());
			}
		}

		EntityExplodeEvent explodeEvent = new EntityExplodeEvent(e.getEntity(), e.getEntity().getLocation(),
				explodedPipeBlocks, 1f);
		Bukkit.getPluginManager().callEvent(explodeEvent);
		if (!explodeEvent.isCancelled()) {
			for (Block b : explodeEvent.blockList()) {
				final Pipe pipe = pipeMap.get(BlockLoc.convertBlockLoc(b.getLocation()));
				if (pipe != null) {
					TransportPipes.instance.pipeThread.runTask(new Runnable() {

						@Override
						public void run() {
							pipe.explode(false);
						}
					}, 0);
				}
			}
		}

		for (Block b : explodedPipeBlocksBefore) {
			final Pipe pipe = pipeMap.get(BlockLoc.convertBlockLoc(b.getLocation()));
			if (pipe != null) {
				pipe.getBlockLoc().getBlock().setType(Material.AIR);
			}
		}

	}

	/**
	 * registers / unregisters a TransportPipesContainer object from this container
	 * block
	 * 
	 * @param toUpdate
	 *            the block which contains an InventoryHolder
	 * @param add
	 *            true: block added | false: block removed
	 */
	public void updatePipeNeighborBlockSync(Block toUpdate, boolean add) {
		BlockLoc bl = BlockLoc.convertBlockLoc(toUpdate.getLocation());

		if (add) {
			if (TransportPipes.instance.getContainerMap(toUpdate.getWorld()) == null
					|| !TransportPipes.instance.getContainerMap(toUpdate.getWorld()).containsKey(bl)) {
				TransportPipesContainer tpc = createContainerFromBlock(toUpdate);
				PipeAPI.registerTransportPipesContainer(toUpdate.getLocation(), tpc);
			}
		} else {
			if (TransportPipes.instance.getContainerMap(toUpdate.getWorld()) != null
					&& TransportPipes.instance.getContainerMap(toUpdate.getWorld()).containsKey(bl)) {
				PipeAPI.unregisterTransportPipesContainer(toUpdate.getLocation());
			}
		}
	}

	/**
	 * checks if this blockID is an InventoryHolder
	 */
	public boolean isIdContainerBlock(int id) {
		boolean v1_9or1_10 = Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10");
		return id == Material.CHEST.getId() || id == Material.TRAPPED_CHEST.getId() || id == Material.HOPPER.getId()
				|| id == Material.FURNACE.getId() || id == Material.BURNING_FURNACE.getId() || id == 379
				|| id == Material.DISPENSER.getId() || id == Material.DROPPER.getId()
				|| id == Material.BREWING_STAND.getId() || (!v1_9or1_10 && id >= Material.WHITE_SHULKER_BOX.getId()
						&& id <= Material.BLACK_SHULKER_BOX.getId());
	}

	public TransportPipesContainer createContainerFromBlock(Block block) {
		if (block.getState() instanceof Furnace) {
			return new FurnaceContainer(block);
		} else if (block.getState() instanceof BrewingStand) {
			return new BrewingStandContainer(block);
		} else if (block.getState() instanceof InventoryHolder) {
			return new SimpleInventoryContainer(block);
		}
		return null;
	}

	public boolean isInLoadedChunk(Location loc) {
		synchronized (loadedChunksGridCoords) {
			List<Chunk> loadedChunks = loadedChunksGridCoords.get(loc.getWorld());
			if (loadedChunks != null) {
				for (Chunk chunk : loadedChunks) {
					int chunkX = chunk.getX();
					int chunkZ = chunk.getZ();
					if (chunkX * 16 <= loc.getBlockX() && (chunkX + 1) * 16 > loc.getBlockX()) {
						if (chunkZ * 16 <= loc.getBlockZ() && (chunkZ + 1) * 16 > loc.getBlockZ()) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public void handleChunkLoadSync(Chunk loadedChunk) {
		synchronized (loadedChunksGridCoords) {
			if (!loadedChunksGridCoords.containsKey(loadedChunk.getWorld())) {
				loadedChunksGridCoords.put(loadedChunk.getWorld(),
						Collections.synchronizedList(new ArrayList<Chunk>()));
			}
			if (!loadedChunksGridCoords.get(loadedChunk.getWorld()).contains(loadedChunk)) {
				loadedChunksGridCoords.get(loadedChunk.getWorld()).add(loadedChunk);
			}
		}

		if (loadedChunk.getTileEntities() != null) {
			for (BlockState bs : loadedChunk.getTileEntities()) {
				if (isIdContainerBlock(bs.getTypeId())) {

					updatePipeNeighborBlockSync(bs.getBlock(), true);

					Map<BlockLoc, TransportPipesContainer> containerMap = TransportPipes.instance
							.getContainerMap(loadedChunk.getWorld());
					synchronized (containerMap) {
						BlockLoc bl = BlockLoc.convertBlockLoc(bs.getLocation());
						TransportPipesContainer tpc = containerMap.get(bl);
						if (tpc instanceof BlockContainer) {
							((BlockContainer) tpc).updateBlock();
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent e) {
		handleChunkLoadSync(e.getChunk());
	}

	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent e) {
		synchronized (loadedChunksGridCoords) {
			if (loadedChunksGridCoords.containsKey(e.getWorld())) {
				if (loadedChunksGridCoords.get(e.getWorld()).contains(e.getChunk())) {
					loadedChunksGridCoords.get(e.getWorld()).remove(e.getChunk());
				}
			}
		}
	}

}
