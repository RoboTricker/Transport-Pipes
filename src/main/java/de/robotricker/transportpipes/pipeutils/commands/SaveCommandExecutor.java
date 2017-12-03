package de.robotricker.transportpipes.pipeutils.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import de.robotricker.transportpipes.TransportPipes;

public class SaveCommandExecutor implements PipesCommandExecutor {

	@Override
	public boolean onCommand(final CommandSender cs, final String[] args) {

		if (!cs.hasPermission("transportpipes.save")) {
			return false;
		}

		// TransportPipes.instance.savingManager.savePipesAsync(true);

		/*
		 * final Block b = ((Player) cs).getLocation().getBlock();
		 * b.setType(Material.CHEST); Chest chest = (Chest) b.getState();
		 * chest.getInventory().addItem(new ItemStack(Material.DIAMOND, 64));
		 * PipeAPI.registerTransportPipesContainer(chest.getLocation(), new
		 * SimpleInventoryContainer(b));
		 * 
		 * PipeAPI.buildPipe(b.getRelative(BlockFace.NORTH).getLocation(),
		 * PipeType.EXTRACTION, null); ExtractionPipe ep = (ExtractionPipe)
		 * PipeAPI.getPipeAtLocation(b.getRelative(BlockFace.NORTH).getLocation());
		 * ep.setExtractCondition(ExtractCondition.ALWAYS_EXTRACT);
		 * 
		 * Bukkit.getScheduler().runTaskLater(TransportPipes.instance, new Runnable() {
		 * 
		 * @Override public void run() {
		 * PipeAPI.buildPipe(b.getRelative(BlockFace.NORTH).getRelative(BlockFace.WEST).
		 * getLocation(), PipeType.COLORED, PipeColor.WHITE);
		 * Bukkit.getScheduler().runTaskLater(TransportPipes.instance, new Runnable() {
		 * 
		 * @Override public void run() {
		 * PipeAPI.buildPipe(b.getRelative(BlockFace.WEST).getLocation(),
		 * PipeType.COLORED, PipeColor.WHITE); } }, 20L); } }, 20L);
		 */

		cs.sendMessage("chunk loaded before: " + TransportPipes.cachedChunk.isLoaded());

		
		if (args.length >= 2) {
			if (args[0].equalsIgnoreCase("sync")) {
				if (args[1].equalsIgnoreCase("1")) {
					TransportPipes.cachedBlockLoc.getBlock();
				} else if (args[1].equalsIgnoreCase("2")) {
					TransportPipes.cachedBlockLoc.getBlock().getType();
				} else if (args[1].equalsIgnoreCase("3")) {
					TransportPipes.cachedBlockLoc.getBlock().getChunk();
				} else if (args[1].equalsIgnoreCase("4")) {
					TransportPipes.cachedChunk.getChunkSnapshot();
				} else if (args[1].equalsIgnoreCase("5")) {
					TransportPipes.cachedBlockLoc.getWorld();
				} else if (args[1].equalsIgnoreCase("6")) {
					TransportPipes.cachedBlockLoc.getWorld().getChunkAt(TransportPipes.cachedBlockLoc);
				}
			} else if (args[0].equalsIgnoreCase("async")) {
				Bukkit.getScheduler().runTaskAsynchronously(TransportPipes.instance, new Runnable() {

					@Override
					public void run() {
						if (args[1].equalsIgnoreCase("1")) {
							TransportPipes.cachedBlockLoc.getBlock();
						} else if (args[1].equalsIgnoreCase("2")) {
							TransportPipes.cachedBlockLoc.getBlock().getType();
						} else if (args[1].equalsIgnoreCase("3")) {
							TransportPipes.cachedBlockLoc.getBlock().getChunk();
						} else if (args[1].equalsIgnoreCase("4")) {
							TransportPipes.cachedChunk.getChunkSnapshot();
						} else if (args[1].equalsIgnoreCase("5")) {
							TransportPipes.cachedBlockLoc.getWorld();
						} else if (args[1].equalsIgnoreCase("6")) {
							TransportPipes.cachedBlockLoc.getWorld().getChunkAt(TransportPipes.cachedBlockLoc);
						}
						cs.sendMessage("chunk loaded after after: " + TransportPipes.cachedChunk.isLoaded());
					}
				});
			}
		}

		cs.sendMessage("chunk loaded after: " + TransportPipes.cachedChunk.isLoaded());

		cs.sendMessage("-------------------------§cPipes saved");

		return true;
	}

}
