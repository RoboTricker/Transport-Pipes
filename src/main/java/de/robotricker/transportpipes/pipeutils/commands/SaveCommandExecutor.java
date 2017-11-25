package de.robotricker.transportpipes.pipeutils.commands;

import org.bukkit.command.CommandSender;
import de.robotricker.transportpipes.TransportPipes;

public class SaveCommandExecutor implements PipesCommandExecutor {

	@Override
	public boolean onCommand(CommandSender cs, String[] args) {

		if (!cs.hasPermission("transportpipes.save")) {
			return false;
		}

		TransportPipes.instance.savingManager.savePipesAsync(true);

		/*final Block b = ((Player) cs).getLocation().getBlock();
		b.setType(Material.CHEST);
		Chest chest = (Chest) b.getState();
		chest.getInventory().addItem(new ItemStack(Material.DIAMOND, 64));
		PipeAPI.registerTransportPipesContainer(chest.getLocation(), new SimpleInventoryContainer(b));

		PipeAPI.buildPipe(b.getRelative(BlockFace.NORTH).getLocation(), PipeType.EXTRACTION, null);
		ExtractionPipe ep = (ExtractionPipe) PipeAPI.getPipeAtLocation(b.getRelative(BlockFace.NORTH).getLocation());
		ep.setExtractCondition(ExtractCondition.ALWAYS_EXTRACT);

		Bukkit.getScheduler().runTaskLater(TransportPipes.instance, new Runnable() {

			@Override
			public void run() {
				PipeAPI.buildPipe(b.getRelative(BlockFace.NORTH).getRelative(BlockFace.WEST).getLocation(), PipeType.COLORED, PipeColor.WHITE);
				Bukkit.getScheduler().runTaskLater(TransportPipes.instance, new Runnable() {

					@Override
					public void run() {
						PipeAPI.buildPipe(b.getRelative(BlockFace.WEST).getLocation(), PipeType.COLORED, PipeColor.WHITE);
					}
				}, 20L);
			}
		}, 20L);*/

		cs.sendMessage("§cPipes saved");

		return true;
	}

}
