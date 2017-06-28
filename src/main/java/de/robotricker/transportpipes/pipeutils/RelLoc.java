package de.robotricker.transportpipes.pipeutils;

import de.robotricker.transportpipes.pipes.Pipe;

public class RelLoc {

	private long x;
	private long y;
	private long z;

	public RelLoc(float x, float y, float z) {
		this.x = (long) (x * Pipe.FLOAT_PRECISION);
		this.y = (long) (y * Pipe.FLOAT_PRECISION);
		this.z = (long) (z * Pipe.FLOAT_PRECISION);
	}

	public long getLongX() {
		return x;
	}

	public long getLongY() {
		return y;
	}

	public long getLongZ() {
		return z;
	}

	public float getFloatX() {
		return x / 1f / Pipe.FLOAT_PRECISION;
	}

	public float getFloatY() {
		return y / 1f / Pipe.FLOAT_PRECISION;
	}

	public float getFloatZ() {
		return z / 1f / Pipe.FLOAT_PRECISION;
	}

	public void set(float x, float y, float z) {
		this.x = (long) (x * Pipe.FLOAT_PRECISION);
		this.y = (long) (y * Pipe.FLOAT_PRECISION);
		this.z = (long) (z * Pipe.FLOAT_PRECISION);
	}

	public void set(long x, long y, long z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public String toString() {
		return getFloatX() + ":" + getFloatY() + ":" + getFloatZ();
	}

	public static RelLoc fromString(String s) {
		RelLoc rl = new RelLoc(Float.parseFloat(s.split(":")[0]), Float.parseFloat(s.split(":")[1]), Float.parseFloat(s.split(":")[2]));
		return rl;
	}

	public static boolean compare(RelLoc rl1, RelLoc rl2) {
		if (rl1.x == rl2.x) {
			if (rl1.y == rl2.y) {
				if (rl1.z == rl2.z) {
					return true;
				}
			}
		}
		return false;
	}

}
