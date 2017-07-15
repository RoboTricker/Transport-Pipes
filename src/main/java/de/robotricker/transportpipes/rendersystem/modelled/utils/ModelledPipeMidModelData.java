package de.robotricker.transportpipes.rendersystem.modelled.utils;

import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipes.colored.PipeColor;
import de.robotricker.transportpipes.pipes.types.ColoredPipe;
import de.robotricker.transportpipes.pipes.types.Pipe;

public class ModelledPipeMidModelData {

	private PipeType pipeType;
	private PipeColor coloredPipe_pipeColor;

	public ModelledPipeMidModelData(PipeType pipeType) {
		this.pipeType = pipeType;
	}

	public ModelledPipeMidModelData(PipeType pipeType, PipeColor coloredPipe_pipeColor) {
		this(pipeType);
		this.coloredPipe_pipeColor = coloredPipe_pipeColor;
	}

	public PipeType getPipeType() {
		return pipeType;
	}

	public PipeColor getColoredPipe_pipeColor() {
		return coloredPipe_pipeColor;
	}

	public static ModelledPipeMidModelData createModelData(Pipe pipe) {
		switch (pipe.getPipeType()) {
		case COLORED:
			return new ModelledPipeMidModelData(pipe.getPipeType(), ((ColoredPipe) pipe).getPipeColor());
		case ICE:
			return new ModelledPipeMidModelData(pipe.getPipeType());
		case GOLDEN:
			return new ModelledPipeMidModelData(pipe.getPipeType());
		case IRON:
			return new ModelledPipeMidModelData(pipe.getPipeType());
		default:
			return null;
		}
	}

}
