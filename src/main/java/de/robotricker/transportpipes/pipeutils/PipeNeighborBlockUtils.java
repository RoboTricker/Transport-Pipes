package de.robotricker.transportpipes.pipeutils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
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
			for (PipeDirection pd : PipeDirection.values()) {
				PipeDirection opposite = pd.getOpposite();
				Location blockLoc = toUpdate.getLocation().clone().add(pd.getX(), pd.getY(), pd.getZ());

				if (pipeMap.containsKey(BlockLoc.convertBlockLoc(blockLoc))) {
					final Pipe pipe = pipeMap.get(BlockLoc.convertBlockLoc(blockLoc));
					if (add) {
						//add pipe neighbor block
						if (!pipe.pipeNeighborBlocks.contains(opposite)) {
							pipe.pipeNeighborBlocks.add(opposite);
							PipeThread.runTask(new Runnable() {

								@Override
								public void run() {
									TransportPipes.pipePacketManager.updatePipe(pipe);
								}
							}, 0);
						}
					} else {
						//remove pipe neighbor block
						if (pipe.pipeNeighborBlocks.contains(opposite)) {
							pipe.pipeNeighborBlocks.remove(opposite);
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
	}

	/**
	 * checks if this blockID is an InventoryHolder
	 */
	public static boolean isIdInventoryHolder(int id) {
		boolean v1_9or1_10 = Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10");
		return id == 54 || id == 146 || id == 154 || id == 61 || id == 62 || id == 379 || id == 23 || id == 158 || id == 117 || (!v1_9or1_10 && id >= 219 && id <= 234);
	}

	/**
	 * ONLY IN MAIN THREAD gets block connection dirs (not pipe connections)
	 */
	public static List<PipeDirection> getOnlyNeighborBlocksConnectionsSync(final Location pipeLoc) {
		List<PipeDirection> dirs = new ArrayList<>();
		for (PipeDirection dir : PipeDirection.values()) {
			Location blockLoc = pipeLoc.clone().add(dir.getX(), dir.getY(), dir.getZ());
			if (PipeNeighborBlockUtils.isIdInventoryHolder(blockLoc.getBlock().getTypeId())) {
				dirs.add(dir);
			}
		}
		return dirs;
	}

}
