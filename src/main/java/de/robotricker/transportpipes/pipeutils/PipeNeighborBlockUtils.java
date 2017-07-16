package de.robotricker.transportpipes.pipeutils;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import de.robotricker.transportpipes.PipeThread;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.api.PipeAPI;
import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.container.ChestContainer;
import de.robotricker.transportpipes.pipes.BlockLoc;
import de.robotricker.transportpipes.pipes.PipeDirection;
import de.robotricker.transportpipes.pipes.types.Pipe;

public class PipeNeighborBlockUtils implements Listener {

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent e) {
		if (isIdInventoryHolder(e.getBlockPlaced().getTypeId())) {
			updatePipeNeighborBlockSync(e.getBlock(), true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e) {
		if (isIdInventoryHolder(e.getBlock().getTypeId())) {
			updatePipeNeighborBlockSync(e.getBlock(), false);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockExplode(BlockExplodeEvent e) {
		for (Block b : e.blockList()) {
			if (isIdInventoryHolder(b.getTypeId())) {
				updatePipeNeighborBlockSync(b, false);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent e) {
		for (Block b : e.blockList()) {
			if (isIdInventoryHolder(b.getTypeId())) {
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

		for (Block block : LocationUtils.getNearbyBlocks(e.getEntity().getLocation(), (int) Math.floor(e.getRadius()))) {
			BlockLoc blockLoc = BlockLoc.convertBlockLoc(block.getLocation());
			final Pipe pipe = pipeMap.get(blockLoc);
			if (pipe == null) {
				continue;
			}
			PipeThread.runTask(new Runnable() {

				@Override
				public void run() {
					pipe.explode(false);
				}
			}, 0);
		}
	}

	/**
	 * updates the "pipeNeighborBlocks" list of all pipes around this block
	 * 
	 * @param add
	 *            true: block added | false: block removed
	 */
	public static void updatePipeNeighborBlockSync(Block toUpdate, boolean add) {

		Map<BlockLoc, Pipe> pipeMap = TransportPipes.instance.getPipeMap(toUpdate.getWorld());

		if (pipeMap != null) {

			BlockLoc bl = BlockLoc.convertBlockLoc(toUpdate.getLocation());
			boolean updateNeighborPipes = false;

			if (add) {
				if (TransportPipes.instance.getContainerMap(toUpdate.getWorld()) == null || !TransportPipes.instance.getContainerMap(toUpdate.getWorld()).containsKey(bl)) {
					TransportPipesContainer tpc = createContainerFromBlock(toUpdate);
					PipeAPI.registerTransportPipesContainer(toUpdate.getLocation(), tpc);
					updateNeighborPipes = true;
				}
			} else {
				if (TransportPipes.instance.getContainerMap(toUpdate.getWorld()) != null && TransportPipes.instance.getContainerMap(toUpdate.getWorld()).containsKey(bl)) {
					PipeAPI.unregisterTransportPipesContainer(toUpdate.getLocation());
					updateNeighborPipes = true;
				}
			}

			if (updateNeighborPipes) {
				for (PipeDirection pd : PipeDirection.values()) {
					Location blockLoc = toUpdate.getLocation().clone().add(pd.getX(), pd.getY(), pd.getZ());
					if (pipeMap.containsKey(BlockLoc.convertBlockLoc(blockLoc))) {
						final Pipe pipe = pipeMap.get(BlockLoc.convertBlockLoc(blockLoc));
						PipeThread.runTask(new Runnable() {

							@Override
							public void run() {
								TransportPipes.pipePacketManager.updatePipe(pipe);
							}
						}, 0);
					}
				}
			}

		}
	}

	/**
	 * checks if this blockID is an InventoryHolder
	 */
	public static boolean isIdInventoryHolder(int id) {
		boolean v1_9or1_10 = Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10");
		return id == Material.CHEST.getId() || id == Material.TRAPPED_CHEST.getId() || id == Material.HOPPER.getId() || id == Material.FURNACE.getId() || id == Material.BURNING_FURNACE.getId() || id == 379 || id == Material.DISPENSER.getId() || id == Material.DROPPER.getId() || id == Material.BREWING_STAND.getId() || (!v1_9or1_10 && id >= Material.WHITE_SHULKER_BOX.getId() && id <= Material.BLACK_SHULKER_BOX.getId());
	}

	public static TransportPipesContainer createContainerFromBlock(Block block) {
		if (block.getState() instanceof Chest) {
			return new ChestContainer(block);
		}
		return null;
	}

}
