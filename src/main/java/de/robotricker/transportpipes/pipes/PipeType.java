package de.robotricker.transportpipes.pipes;

import org.bukkit.Location;

import de.robotricker.transportpipes.pipeutils.PipeColor;

public enum PipeType {

	COLORED(),
	GOLDEN(),
	IRON(),
	ICE();

	public Pipe createPipe(Location blockLoc, PipeColor pc) {
		if (this == COLORED) {
			return new ColoredPipe(blockLoc, pc);
		} else if (this == GOLDEN) {
			return new GoldenPipe(blockLoc);
		} else if (this == IRON) {
			return new IronPipe(blockLoc);
		} else if (this == ICE) {
			return new IcePipe(blockLoc);
		}
		return null;
	}

}
