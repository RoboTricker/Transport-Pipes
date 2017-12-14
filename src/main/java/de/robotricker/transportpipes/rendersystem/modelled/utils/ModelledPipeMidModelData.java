package de.robotricker.transportpipes.rendersystem.modelled.utils;

import de.robotricker.transportpipes.duct.pipe.ColoredPipe;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.duct.pipe.utils.PipeColor;
import de.robotricker.transportpipes.duct.pipe.utils.PipeType;

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
		default:
			return new ModelledPipeMidModelData(pipe.getPipeType());
		}
	}

}
