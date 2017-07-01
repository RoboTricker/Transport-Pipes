package de.robotricker.transportpipes.pipes;

import org.bukkit.Location;

import de.robotricker.transportpipes.pipeutils.PipeColor;

public enum PipeType {

	COLORED(0),
	GOLDEN(1),
	IRON(2),
	ICE(3);

	private int id;

	private PipeType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

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

	public static PipeType getFromId(int id) {
		for (PipeType pt : PipeType.values()) {
			if (pt.getId() == id) {
				return pt;
			}
		}
		return null;
	}

}
