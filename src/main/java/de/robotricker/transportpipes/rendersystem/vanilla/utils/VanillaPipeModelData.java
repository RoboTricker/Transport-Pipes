package de.robotricker.transportpipes.rendersystem.vanilla.utils;

import de.robotricker.transportpipes.pipes.PipeDirection;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipes.colored.PipeColor;
import de.robotricker.transportpipes.pipes.types.ColoredPipe;
import de.robotricker.transportpipes.pipes.types.IronPipe;
import de.robotricker.transportpipes.pipes.types.Pipe;

public class VanillaPipeModelData {

	private PipeType pipeType;
	private PipeDirection ironPipe_outputDirection;
	private PipeColor coloredPipe_pipeColor;

	public VanillaPipeModelData(PipeType pipeType) {
		this.pipeType = pipeType;
	}

	public VanillaPipeModelData(PipeType pipeType, PipeDirection ironPipe_outputDirection) {
		this(pipeType);
		this.ironPipe_outputDirection = ironPipe_outputDirection;
	}

	public VanillaPipeModelData(PipeType pipeType, PipeColor coloredPipe_pipeColor) {
		this(pipeType);
		this.coloredPipe_pipeColor = coloredPipe_pipeColor;
	}

	public PipeType getPipeType() {
		return pipeType;
	}

	public PipeDirection getIronPipe_outputDirection() {
		return ironPipe_outputDirection;
	}

	public PipeColor getColoredPipe_pipeColor() {
		return coloredPipe_pipeColor;
	}

	public static VanillaPipeModelData createModelData(Pipe pipe) {
		switch (pipe.getPipeType()) {
		case COLORED:
			return new VanillaPipeModelData(pipe.getPipeType(), ((ColoredPipe) pipe).getPipeColor());
		case ICE:
			return new VanillaPipeModelData(pipe.getPipeType());
		case GOLDEN:
			return new VanillaPipeModelData(pipe.getPipeType());
		case IRON:
			return new VanillaPipeModelData(pipe.getPipeType(), ((IronPipe) pipe).getCurrentOutputDir());
		default:
			return null;
		}
	}

}
