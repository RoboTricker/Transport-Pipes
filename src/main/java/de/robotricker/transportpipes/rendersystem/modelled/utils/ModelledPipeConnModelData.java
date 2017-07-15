package de.robotricker.transportpipes.rendersystem.modelled.utils;

import de.robotricker.transportpipes.pipes.PipeDirection;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipes.colored.PipeColor;
import de.robotricker.transportpipes.pipes.goldenpipe.GoldenPipeColor;
import de.robotricker.transportpipes.pipes.types.ColoredPipe;
import de.robotricker.transportpipes.pipes.types.IronPipe;
import de.robotricker.transportpipes.pipes.types.Pipe;

public class ModelledPipeConnModelData {

	private PipeType pipeType;
	private PipeDirection connDirection;
	private GoldenPipeColor goldenPipe_color;
	private boolean ironPipe_output;
	private PipeColor coloredPipe_pipeColor;

	public ModelledPipeConnModelData(PipeType pipeType, PipeDirection connDirection) {
		this.pipeType = pipeType;
		this.connDirection = connDirection;
	}

	public ModelledPipeConnModelData(PipeType pipeType, PipeDirection connDirection, GoldenPipeColor goldenPipe_color) {
		this(pipeType, connDirection);
		this.goldenPipe_color = goldenPipe_color;
	}

	public ModelledPipeConnModelData(PipeType pipeType, PipeDirection connDirection, boolean ironPipe_output) {
		this(pipeType, connDirection);
		this.ironPipe_output = ironPipe_output;
	}

	public ModelledPipeConnModelData(PipeType pipeType, PipeDirection connDirection, PipeColor coloredPipe_pipeColor) {
		this(pipeType, connDirection);
		this.coloredPipe_pipeColor = coloredPipe_pipeColor;
	}

	public PipeType getPipeType() {
		return pipeType;
	}

	public PipeDirection getConnDirection() {
		return connDirection;
	}

	public GoldenPipeColor getGoldenPipe_color() {
		return goldenPipe_color;
	}

	public boolean isIronPipe_output() {
		return ironPipe_output;
	}

	public PipeColor getColoredPipe_pipeColor() {
		return coloredPipe_pipeColor;
	}

	public static ModelledPipeConnModelData createModelData(Pipe pipe, PipeDirection connDirection) {
		switch (pipe.getPipeType()) {
		case COLORED:
			return new ModelledPipeConnModelData(pipe.getPipeType(), connDirection, ((ColoredPipe) pipe).getPipeColor());
		case ICE:
			return new ModelledPipeConnModelData(pipe.getPipeType(), connDirection);
		case GOLDEN:
			return new ModelledPipeConnModelData(pipe.getPipeType(), connDirection, GoldenPipeColor.getColorWithDirection(connDirection));
		case IRON:
			return new ModelledPipeConnModelData(pipe.getPipeType(), connDirection, ((IronPipe) pipe).getCurrentOutputDir() == connDirection);
		default:
			return null;
		}
	}

}
