package de.robotricker.transportpipes.pipeutils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

		for (Block block : LocationUtils.getNearbyBlocks(e.getEntity().getLocation(), (int) Math.floor(e.getRadius()))) {
			BlockLoc blockLoc = BlockLoc.convertBlockLoc(block.getLocation());
			Pipe pipe = pipeMap.get(blockLoc);
			if (pipe != null) {
				pipe.blockLoc.getBlock().setType(Material.GLASS);
				explodedPipeBlocksBefore.add(pipe.blockLoc.getBlock());
				explodedPipeBlocks.add(pipe.blockLoc.getBlock());
			}
		}

		EntityExplodeEvent explodeEvent = new EntityExplodeEvent(e.getEntity(), e.getEntity().getLocation(), explodedPipeBlocks, 1f);
		Bukkit.getPluginManager().callEvent(explodeEvent);
		if (!explodeEvent.isCancelled()) {
			for (Block b : explodeEvent.blockList()) {
				final Pipe pipe = pipeMap.get(BlockLoc.convertBlockLoc(b.getLocation()));
				if (pipe != null) {
					PipeThread.runTask(new Runnable() {

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
				pipe.blockLoc.getBlock().setType(Material.AIR);
			}
		}

	}

	/**
	 * registers / unregisters a TransportPipesContainer object from this container block
	 * 
	 * @param toUpdate
	 *            the block which contains an InventoryHolder
	 * @param add
	 *            true: block added | false: block removed
	 */
	public static void updatePipeNeighborBlockSync(Block toUpdate, boolean add) {

		Map<BlockLoc, Pipe> pipeMap = TransportPipes.instance.getPipeMap(toUpdate.getWorld());

		if (pipeMap != null) {
			BlockLoc bl = BlockLoc.convertBlockLoc(toUpdate.getLocation());

			if (add) {
				if (TransportPipes.instance.getContainerMap(toUpdate.getWorld()) == null || !TransportPipes.instance.getContainerMap(toUpdate.getWorld()).containsKey(bl)) {
					TransportPipesContainer tpc = createContainerFromBlock(toUpdate);
					PipeAPI.registerTransportPipesContainer(toUpdate.getLocation(), tpc);
				}
			} else {
				if (TransportPipes.instance.getContainerMap(toUpdate.getWorld()) != null && TransportPipes.instance.getContainerMap(toUpdate.getWorld()).containsKey(bl)) {
					PipeAPI.unregisterTransportPipesContainer(toUpdate.getLocation());
				}
			}

		}
	}

	/**
	 * checks if this blockID is an InventoryHolder
	 */
	public static boolean isIdContainerBlock(int id) {
		boolean v1_9or1_10 = Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10");
		return id == Material.CHEST.getId() || id == Material.TRAPPED_CHEST.getId() || id == Material.HOPPER.getId() || id == Material.FURNACE.getId() || id == Material.BURNING_FURNACE.getId() || id == 379 || id == Material.DISPENSER.getId() || id == Material.DROPPER.getId() || id == Material.BREWING_STAND.getId() || (!v1_9or1_10 && id >= Material.WHITE_SHULKER_BOX.getId() && id <= Material.BLACK_SHULKER_BOX.getId());
	}

	public static TransportPipesContainer createContainerFromBlock(Block block) {
		if (block.getState() instanceof Furnace) {
			return new FurnaceContainer(block);
		} else if (block.getState() instanceof BrewingStand) {
			return new BrewingStandContainer(block);
		} else if (block.getState() instanceof InventoryHolder) {
			return new SimpleInventoryContainer(block);
		}
		return null;
	}

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent e) {
		//update all BlockContainer inventories because the BlockState and Inventory instance of a block changes if the chunk get's unloaded
		Map<BlockLoc, TransportPipesContainer> containerMap = TransportPipes.instance.getContainerMap(e.getWorld());
		if (containerMap != null) {
			synchronized (containerMap) {
				for (BlockLoc bl : containerMap.keySet()) {
					if (containerMap.get(bl) instanceof BlockContainer) {
						if (bl.toLocation(e.getWorld()).getBlockX() / 16 == e.getChunk().getX() && bl.toLocation(e.getWorld()).getBlockZ() / 16 == e.getChunk().getZ()) {
							((BlockContainer) containerMap.get(bl)).updateBlock();
						}
					}
				}
			}
		}
	}

}
