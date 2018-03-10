package de.robotricker.transportpipes.utils.staticutils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.BlockChangeDelegate;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
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
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.hitbox.occlusionculling.BlockChangeListener.ChunkCoords;
import io.sentry.Sentry;

public class ContainerBlockUtils implements Listener {

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent e) {
		System.out.println("block place event");
		if (isIdContainerBlock(e.getBlockPlaced().getTypeId())) {
			updateDuctNeighborBlockSync(e.getBlock(), true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e) {
		if (isIdContainerBlock(e.getBlock().getTypeId())) {
			updateDuctNeighborBlockSync(e.getBlock(), false);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockExplode(BlockExplodeEvent e) {
		for (Block b : e.blockList()) {
			if (isIdContainerBlock(b.getTypeId())) {
				updateDuctNeighborBlockSync(b, false);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent e) {
		for (Block b : e.blockList()) {
			if (isIdContainerBlock(b.getTypeId())) {
				updateDuctNeighborBlockSync(b, false);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onExplosionPrime(ExplosionPrimeEvent e) {
		if (!TransportPipes.instance.generalConf.isDestroyPipeOnExplosion()) {
			return;
		}
		Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(e.getEntity().getWorld());
		if (ductMap == null) {
			return;
		}

		List<Block> explodedDuctBlocksBefore = new ArrayList<Block>();
		List<Block> explodedDuctBlocks = new ArrayList<Block>();

		for (Block block : LocationUtils.getNearbyBlocks(e.getEntity().getLocation(), (int) Math.floor(e.getRadius()))) {
			BlockLoc blockLoc = BlockLoc.convertBlockLoc(block.getLocation());
			Duct duct = ductMap.get(blockLoc);
			if (duct != null) {
				duct.getBlockLoc().getBlock().setType(Material.GLASS);
				explodedDuctBlocksBefore.add(duct.getBlockLoc().getBlock());
				explodedDuctBlocks.add(duct.getBlockLoc().getBlock());
			}
		}

		EntityExplodeEvent explodeEvent = new EntityExplodeEvent(e.getEntity(), e.getEntity().getLocation(), explodedDuctBlocks, 1f);
		Bukkit.getPluginManager().callEvent(explodeEvent);
		if (!explodeEvent.isCancelled()) {
			for (Block b : explodeEvent.blockList()) {
				final Duct duct = ductMap.get(BlockLoc.convertBlockLoc(b.getLocation()));
				if (duct != null) {
					TransportPipes.instance.pipeThread.runTask(new Runnable() {

						@Override
						public void run() {
							duct.explode(false, true);
						}
					}, 0);
				}
			}
		}

		for (Block b : explodedDuctBlocksBefore) {
			final Duct duct = ductMap.get(BlockLoc.convertBlockLoc(b.getLocation()));
			if (duct != null) {
				duct.getBlockLoc().getBlock().setType(Material.AIR);
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
	public void updateDuctNeighborBlockSync(Block toUpdate, boolean add) {
		System.out.println("updating vanilla block (" + add + ")");
		BlockLoc bl = BlockLoc.convertBlockLoc(toUpdate.getLocation());

		if (add) {
			if (!isIdContainerBlock(toUpdate.getTypeId())) {
				System.out.println("no container block ^^");
				return;
			}
			if (TransportPipes.instance.getContainerMap(toUpdate.getWorld()) == null || !TransportPipes.instance.getContainerMap(toUpdate.getWorld()).containsKey(bl)) {
				System.out.println("container does not yet exist");
				TransportPipesContainer tpc = createContainerFromBlock(toUpdate);
				PipeAPI.registerTransportPipesContainer(toUpdate.getLocation(), tpc);
			}
		} else {
			if (TransportPipes.instance.getContainerMap(toUpdate.getWorld()) != null && TransportPipes.instance.getContainerMap(toUpdate.getWorld()).containsKey(bl)) {
				PipeAPI.unregisterTransportPipesContainer(toUpdate.getLocation());
			}
		}
	}

	/**
	 * checks if this blockID is an InventoryHolder
	 */
	public boolean isIdContainerBlock(int id) {
		boolean v1_9or1_10 = Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10");
		return id == Material.CHEST.getId() || id == Material.TRAPPED_CHEST.getId() || id == Material.HOPPER.getId() || id == Material.FURNACE.getId() || id == Material.BURNING_FURNACE.getId() || id == 379 || id == Material.DISPENSER.getId() || id == Material.DROPPER.getId() || id == Material.BREWING_STAND.getId() || (!v1_9or1_10 && id >= Material.WHITE_SHULKER_BOX.getId() && id <= Material.BLACK_SHULKER_BOX.getId());
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

	public void handleChunkLoadSync(Chunk loadedChunk) {
		if (loadedChunk.getTileEntities() != null) {
			for (BlockState bs : loadedChunk.getTileEntities()) {
				if (isIdContainerBlock(bs.getTypeId())) {

					updateDuctNeighborBlockSync(bs.getBlock(), true);

					Map<BlockLoc, TransportPipesContainer> containerMap = TransportPipes.instance.getContainerMap(loadedChunk.getWorld());
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

}
