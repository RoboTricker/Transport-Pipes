package de.robotricker.transportpipes.rendersystem.modelled.utils;

import de.robotricker.transportpipes.pipes.WrappedDirection;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipes.colored.PipeColor;
import de.robotricker.transportpipes.pipes.goldenpipe.GoldenPipeColor;
import de.robotricker.transportpipes.pipes.types.ColoredPipe;
import de.robotricker.transportpipes.pipes.types.ExtractionPipe;
import de.robotricker.transportpipes.pipes.types.IronPipe;
import de.robotricker.transportpipes.pipes.types.Pipe;

public class ModelledPipeConnModelData {

	private PipeType pipeType;
	private WrappedDirection connDirection;
	private GoldenPipeColor goldenPipe_color;
	private boolean iron_extractionPipe_activeSide;
	private PipeColor coloredPipe_pipeColor;

	public ModelledPipeConnModelData(PipeType pipeType, WrappedDirection connDirection) {
		this.pipeType = pipeType;
		this.connDirection = connDirection;
	}

	public ModelledPipeConnModelData(PipeType pipeType, WrappedDirection connDirection, GoldenPipeColor goldenPipe_color) {
		this(pipeType, connDirection);
		this.goldenPipe_color = goldenPipe_color;
	}

	public ModelledPipeConnModelData(PipeType pipeType, WrappedDirection connDirection, boolean iron_extractionPipe_activeSide) {
		this(pipeType, connDirection);
		this.iron_extractionPipe_activeSide = iron_extractionPipe_activeSide;
	}

	public ModelledPipeConnModelData(PipeType pipeType, WrappedDirection connDirection, PipeColor coloredPipe_pipeColor) {
		this(pipeType, connDirection);
		this.coloredPipe_pipeColor = coloredPipe_pipeColor;
	}

	public PipeType getPipeType() {
		return pipeType;
	}

	public WrappedDirection getConnDirection() {
		return connDirection;
	}

	public GoldenPipeColor getGoldenPipe_color() {
		return goldenPipe_color;
	}

	public boolean isIron_ExtractionPipe_ActiveSide() {
		return iron_extractionPipe_activeSide;
	}

	public PipeColor getColoredPipe_pipeColor() {
		return coloredPipe_pipeColor;
	}

	public static ModelledPipeConnModelData createModelData(Pipe pipe, WrappedDirection connDirection) {
		switch (pipe.getPipeType()) {
		case COLORED:
			return new ModelledPipeConnModelData(pipe.getPipeType(), connDirection, ((ColoredPipe) pipe).getPipeColor());
		case GOLDEN:
			return new ModelledPipeConnModelData(pipe.getPipeType(), connDirection, GoldenPipeColor.getColorWithDirection(connDirection));
		case IRON:
			return new ModelledPipeConnModelData(pipe.getPipeType(), connDirection, ((IronPipe) pipe).getCurrentOutputDir() == connDirection);
		case EXTRACTION:
			return new ModelledPipeConnModelData(pipe.getPipeType(), connDirection, ((ExtractionPipe) pipe).getExtractDirection() == connDirection);
		default:
			return new ModelledPipeConnModelData(pipe.getPipeType(), connDirection);
		}
	}

}
