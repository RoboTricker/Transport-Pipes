package de.robotricker.transportpipes.pipeutils.commands;

import org.bukkit.command.CommandSender;

import de.robotricker.transportpipes.TransportPipes;

public class ReloadPipesCommandExecutor implements PipesCommandExecutor {

	@Override
	public boolean onCommand(CommandSender cs) {
		if (!cs.hasPermission(TransportPipes.instance.generalConf.getPermissionReload())) {
			return false;
		}
		//TODO reload!

//		Block target = ((Player) cs).getTargetBlock((HashSet<Material>) null, 10);
//		if (target != null) {
//			PipeAPI.registerTransportPipesContainer(target.getLocation(), new TransportPipesContainer() {
//
//				@Override
//				public boolean isSpaceForItemAsync(PipeDirection insertDirection, ItemData insertion) {
//					return true;
//				}
//
//				@Override
//				public boolean insertItem(PipeDirection insertDirection, ItemData insertion) {
//					return true;
//				}
//
//				@Override
//				public ItemData extractItem(PipeDirection extractDirection) {
//					return new ItemData(new ItemStack(Material.ANVIL));
//				}
//			});
//		}

		cs.sendMessage("§cFeature doesn't work yet");
		return true;
	}

}