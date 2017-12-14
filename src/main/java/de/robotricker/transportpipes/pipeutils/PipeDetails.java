package de.robotricker.transportpipes.pipeutils;

import org.bukkit.Location;

import de.robotricker.transportpipes.pipes.Duct;
import de.robotricker.transportpipes.pipes.DuctType;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipes.colored.PipeColor;
import de.robotricker.transportpipes.pipes.types.ColoredPipe;
import de.robotricker.transportpipes.pipes.types.ExtractionPipe;
import de.robotricker.transportpipes.pipes.types.GoldenPipe;
import de.robotricker.transportpipes.pipes.types.IcePipe;
import de.robotricker.transportpipes.pipes.types.IronPipe;
import de.robotricker.transportpipes.pipes.types.Pipe;
import de.robotricker.transportpipes.pipes.types.VoidPipe;
import io.sentry.Sentry;

public class PipeDetails extends DuctDetails {

	private PipeType pipeType;
	private PipeColor pipeColor;

	/**
	 * creates new PipeDetails with PipeType pipeType (use other constructor for
	 * COLORED pipeType)
	 */
	public PipeDetails(PipeType pipeType) {
		super(DuctType.PIPE, pipeType.getCraftPermission());
		this.pipeType = pipeType;
	}

	/**
	 * automatically sets PipeType to COLORED
	 */
	public PipeDetails(PipeColor pipeColor) {
		super(DuctType.PIPE, PipeType.COLORED.getCraftPermission());
		this.pipeType = PipeType.COLORED;
		this.pipeColor = pipeColor;
	}

	public PipeType getPipeType() {
		return pipeType;
	}

	public PipeColor getPipeColor() {
		return pipeColor;
	}

	public void setPipeType(PipeType pipeType) {
		this.pipeType = pipeType;
		setCraftPermission(pipeType.getCraftPermission());
	}

	public void setPipeColor(PipeColor pipeColor) {
		this.pipeColor = pipeColor;
	}

	@Override
	public Duct createDuct(Location blockLoc) {
		if (getPipeType() == PipeType.COLORED) {
			return new ColoredPipe(blockLoc, getPipeColor());
		} else if (getPipeType() == PipeType.GOLDEN) {
			return new GoldenPipe(blockLoc);
		} else if (getPipeType() == PipeType.IRON) {
			return new IronPipe(blockLoc);
		} else if (getPipeType() == PipeType.ICE) {
			return new IcePipe(blockLoc);
		} else if (getPipeType() == PipeType.VOID) {
			return new VoidPipe(blockLoc);
		} else if (getPipeType() == PipeType.EXTRACTION) {
			return new ExtractionPipe(blockLoc);
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ductType == null) ? 0 : ductType.hashCode());
		result = prime * result + ((pipeColor == null) ? 0 : pipeColor.hashCode());
		result = prime * result + ((pipeType == null) ? 0 : pipeType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PipeDetails other = (PipeDetails) obj;
		if (pipeColor != other.pipeColor)
			return false;
		if (pipeType != other.pipeType)
			return false;
		return true;
	}

	@Override
	public String toString() {
		String pipeTypeString = "PipeType:" + pipeType.name() + ";";
		String pipeColorString = pipeColor != null ? "PipeColor:" + pipeColor.name() + ";" : "";
		return super.toString() + pipeTypeString + pipeColorString;
	}

	@Override
	public void fromString(String serialization) {
		try {
			for (String element : serialization.split(";")) {
				if (element.startsWith("PipeType:")) {
					setPipeType(PipeType.valueOf(element.substring(9)));
				} else if (element.startsWith("PipeColor:")) {
					setPipeColor(PipeColor.valueOf(element.substring(10)));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Sentry.capture(e);
		}
	}

}
