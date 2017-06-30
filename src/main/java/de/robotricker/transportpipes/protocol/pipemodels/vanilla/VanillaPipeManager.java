package de.robotricker.transportpipes.protocol.pipemodels.vanilla;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robotricker.transportpipes.pipes.ColoredPipe;
import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipeutils.PipeColor;
import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.pipeutils.PipeUtils;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.protocol.ArmorStandProtocol;
import de.robotricker.transportpipes.protocol.pipemodels.PipeManager;

public class VanillaPipeManager extends PipeManager {

	private Map<Pipe, List<ArmorStandData>> pipeAsd = new HashMap<Pipe, List<ArmorStandData>>();

	public VanillaPipeManager(ArmorStandProtocol protocol) {
		super(protocol);
	}

	@Override
	public void sendPipe(Pipe pipe) {
		if (pipeAsd.containsKey(pipe)) {
			return;
		}

		PipeColor pc = PipeColor.WHITE;
		if (pipe.getPipeType() == PipeType.COLORED) {
			pc = ((ColoredPipe) pipe).getPipeColor();
		}
		List<PipeDirection> conns = PipeUtils.getPipeConnections(pipe.getBlockLoc(), pipe.getPipeType(), pc);
		VanillaPipeShape shape = VanillaPipeShape.getPipeShapeFromConnections(conns);

		pipeAsd.put(pipe, shape.getModel().createASD(pipe.getPipeType(), PipeDirection.NORTH, pc));

		//SEND TO CLIENTS

	}

	@Override
	public void updatePipe(Pipe pipe) {
		if (!pipeAsd.containsKey(pipe)) {
			return;
		}

		List<ArmorStandData> removeASD = new ArrayList<ArmorStandData>();
		List<ArmorStandData> addASD = new ArrayList<ArmorStandData>();

		List<ArmorStandData> oldASD = pipeAsd.get(pipe);

		PipeColor pc = PipeColor.WHITE;
		if (pipe.getPipeType() == PipeType.COLORED) {
			pc = ((ColoredPipe) pipe).getPipeColor();
		}
		List<PipeDirection> conns = PipeUtils.getPipeConnections(pipe.getBlockLoc(), pipe.getPipeType(), pc);
		VanillaPipeShape shape = VanillaPipeShape.getPipeShapeFromConnections(conns);

		List<ArmorStandData> newASD = shape.getModel().createASD(pipe.getPipeType(), PipeDirection.NORTH, pc);

		for (int i = 0; i < Math.max(oldASD.size(), newASD.size()); i++) {
			ArmorStandData ASD1 = i < oldASD.size() ? oldASD.get(i) : null;
			ArmorStandData ASD2 = i < newASD.size() ? newASD.get(i) : null;
			if (!((ASD1 != null && ASD1.isSimilar(ASD2)) || ASD1 == ASD2)) {
				if (ASD1 != null)
					removeASD.add(ASD1);
				if (ASD2 != null)
					addASD.add(ASD2);
			}
		}

		oldASD.clear();
		oldASD.addAll(newASD);

		//SEND TO CLIENTS

	}

	@Override
	public void destroyPipe(Pipe pipe) {
		if (!pipeAsd.containsKey(pipe)) {
			return;
		}
		List<ArmorStandData> ASD = pipeAsd.remove(pipe);

		//SEND TO CLIENTS

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
