package de.robotricker.transportpipes.pipeitems;

import de.robotricker.transportpipes.duct.pipe.Pipe;

/**
 * this class represents the location of a pipeItem inside a pipe.<br>
 * the coordinates x, y and z are given in a range between 0 and 1 but are converted to long values in order to have no float-calculation issues.<br>
 * Pipe.FLOAT_PRECISION is the power of 10 which converts from floats to longs and vice-versa.
 */
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

	/**
	 * switches all values in the following system:<br>
	 * 1 -> 0<br>
	 * 0 -> 1<br>
	 * 0.5 -> 0.5
	 */
	public void switchValues() {
		set(1f - getFloatX(), 1f - getFloatY(), 1f - getFloatZ());
	}

	public void addValues(float addX, float addY, float addZ) {
		set(getFloatX() + addX, getFloatY() + addY, getFloatZ() + addZ);
	}

	@Override
	public String toString() {
		return getFloatX() + ":" + getFloatY() + ":" + getFloatZ();
	}

	public static RelLoc fromString(String s) {
		return new RelLoc(Float.parseFloat(s.split(":")[0]), Float.parseFloat(s.split(":")[1]), Float.parseFloat(s.split(":")[2]));
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
