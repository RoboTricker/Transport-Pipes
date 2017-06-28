package de.robotricker.transportpipes.protocol.pipemodels.vanilla;

import java.util.List;

import org.bukkit.entity.Player;

import de.robotricker.transportpipes.pipes.IronPipe;
import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.protocol.pipemodels.PipeManager;

public class VanillaPipeManager extends PipeManager {

	@Override
	public void sendPipe(Player p, Pipe pipe) {

	}

	@Override
	public void updatePipeShape(Player p, Pipe pipe) {

	}
	
	@Override
	public void updateIronPipe(Player p, IronPipe pipe){
		
	}

	@Override
	public void removePipe(Player p, Pipe pipe) {

	}

	private enum VanillaPipeShape {
		EAST_WEST(new VanillaPipeEWModel()),
		NORTH_SOUTH(new VanillaPipeNSModel()),
		UP_DOWN(new VanillaPipeUDModel()),
		MID(new VanillaPipeMIDModel());

		private VanillaPipeModel model;

		private VanillaPipeShape(VanillaPipeModel model) {
			this.model = model;
			model.getClass().cast(model);
		}

		public VanillaPipeModel getModel() {
			return model;
		}

		public static VanillaPipeShape getPipeShapeFromConnections(List<PipeDirection> conn) {
			if (conn.size() == 1) {
				PipeDirection pd = conn.get(0);
				if (pd == PipeDirection.NORTH || pd == PipeDirection.SOUTH) {
					return NORTH_SOUTH;
				} else if (pd == PipeDirection.UP || pd == PipeDirection.DOWN) {
					return UP_DOWN;
				} else if (pd == PipeDirection.EAST || pd == PipeDirection.WEST) {
					return EAST_WEST;
				}
			} else if (conn.size() == 2) {
				PipeDirection pd1 = conn.get(0);
				PipeDirection pd2 = conn.get(1);
				if (pd1.getOpposite() == pd2) {
					if (pd1 == PipeDirection.NORTH || pd1 == PipeDirection.SOUTH) {
						return NORTH_SOUTH;
					} else if (pd1 == PipeDirection.UP || pd1 == PipeDirection.DOWN) {
						return UP_DOWN;
					} else if (pd1 == PipeDirection.EAST || pd1 == PipeDirection.WEST) {
						return EAST_WEST;
					}
				}
			}
			return MID;
		}
	}

}
