package de.robotricker.transportpipes.pipes;

public enum PipeType {

	COLORED(),
	ICE();

	private PipeType() {

	}

	public Pipe createPipe() {
		if (this == COLORED) {
			return null;
		} else if (this == ICE) {
			return null;
		}
		return null;
	}

}
