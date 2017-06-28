package de.robotricker.transportpipes.protocol.pipemodels.vanilla;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robotricker.transportpipes.pipes.ColoredPipe;
import de.robotricker.transportpipes.pipes.IronPipe;
import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipeutils.PipeColor;
import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.pipeutils.PipeUtils;
import de.robotricker.transportpipes.pipeutils.RelLoc;
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
		if (pipe.getPipeType() == PipeType.COLORED) {
			pipeAsd.put(pipe, shape.getModel().createColoredPipeArmorStandData(pc));
		} else if (pipe.getPipeType() == PipeType.ICE) {
			pipeAsd.put(pipe, shape.getModel().createIcePipeArmorStandData());
		} else if (pipe.getPipeType() == PipeType.GOLDEN) {
			pipeAsd.put(pipe, ((VanillaPipeMIDModel) shape.getModel()).createGoldenPipeArmorStandData());
		} else if (pipe.getPipeType() == PipeType.IRON) {
			pipeAsd.put(pipe, ((VanillaPipeMIDModel) shape.getModel()).createIronPipeArmorStandData());
		}

		if (pipeAsd.containsKey(pipe)) {

			//SEND TO CLIENTS

		}
	}

	@Override
	public void updatePipeShape(Pipe pipe) {
		if (!pipeAsd.containsKey(pipe)) {
			return;
		}
		List<ArmorStandData> asd = pipeAsd.get(pipe);

		//SEND TO CLIENTS

		asd.clear();

		PipeColor pc = PipeColor.WHITE;
		if (pipe.getPipeType() == PipeType.COLORED) {
			pc = ((ColoredPipe) pipe).getPipeColor();
		}
		List<PipeDirection> conns = PipeUtils.getPipeConnections(pipe.getBlockLoc(), pipe.getPipeType(), pc);

		VanillaPipeShape shape = VanillaPipeShape.getPipeShapeFromConnections(conns);
		if (pipe.getPipeType() == PipeType.COLORED) {
			asd.addAll(shape.getModel().createColoredPipeArmorStandData(pc));
		} else if (pipe.getPipeType() == PipeType.ICE) {
			asd.addAll(shape.getModel().createIcePipeArmorStandData());
		} else if (pipe.getPipeType() == PipeType.GOLDEN) {
			asd.addAll(((VanillaPipeMIDModel) shape.getModel()).createGoldenPipeArmorStandData());
		} else if (pipe.getPipeType() == PipeType.IRON) {
			asd.addAll(((VanillaPipeMIDModel) shape.getModel()).createIronPipeArmorStandData());
		}

		//SEND TO CLIENTS

	}

	@Override
	public void updateIronPipe(IronPipe pipe, PipeDirection oldOutput, PipeDirection newOutput) {
		if (!pipeAsd.containsKey(pipe)) {
			return;
		}
		List<ArmorStandData> asd = pipeAsd.get(pipe);

		ArmorStandData oldOutputNewAsd = ((VanillaPipeMIDModel) VanillaPipeShape.MID.getModel()).createIronPipeSideArmorStandData(oldOutput, false);
		ArmorStandData newOutputNewAsd = ((VanillaPipeMIDModel) VanillaPipeShape.MID.getModel()).createIronPipeSideArmorStandData(newOutput, true);
		ArmorStandData oldOutputOldAsd = null;
		ArmorStandData newOutputOldAsd = null;

		for (int i = 0; i < asd.size(); i++) {
			ArmorStandData data = asd.get(i);
			if (RelLoc.compare(data.getLoc(), oldOutputNewAsd.getLoc())) {
				oldOutputOldAsd = data;
				asd.set(i, oldOutputNewAsd);
			}
			if (RelLoc.compare(data.getLoc(), newOutputNewAsd.getLoc())) {
				newOutputOldAsd = data;
				asd.set(i, newOutputNewAsd);
			}
		}
		
		//SEND TO CLIENTS

	}

	@Override
	public void removePipe(Pipe pipe) {
		if (!pipeAsd.containsKey(pipe)) {
			return;
		}
		List<ArmorStandData> asd = pipeAsd.remove(pipe);

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
