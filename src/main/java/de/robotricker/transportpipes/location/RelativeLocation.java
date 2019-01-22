package de.robotricker.transportpipes.location;

import java.util.Objects;

public class RelativeLocation implements Cloneable {

    public static final long PRECISION = 100000;

    private long x;
    private long y;
    private long z;

    public RelativeLocation(double x, double y, double z) {
        set(x, y, z);
    }

    public RelativeLocation(long x, long y, long z) {
        set(x, y, z);
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

    public double getDoubleX() {
        return x / 1d / PRECISION;
    }

    public double getDoubleY() {
        return y / 1d / PRECISION;
    }

    public double getDoubleZ() {
        return z / 1d / PRECISION;
    }

    public RelativeLocation set(double x, double y, double z) {
        this.x = (long) (x * PRECISION);
        this.y = (long) (y * PRECISION);
        this.z = (long) (z * PRECISION);
        return this;
    }

    public RelativeLocation set(long x, long y, long z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public RelativeLocation add(double x, double y, double z) {
        set(getDoubleX() + x, getDoubleY() + y, getDoubleZ() + z);
        return this;
    }

    public RelativeLocation add(long x, long y, long z) {
        set(getLongX() + x, getLongY() + y, getLongZ() + z);
        return this;
    }

    public boolean isXEquals(double x) {
        return this.x == x * PRECISION;
    }

    public boolean isYEquals(double y) {
        return this.y == y * PRECISION;
    }

    public boolean isZEquals(double z) {
        return this.z == z * PRECISION;
    }

    public boolean isEquals(double x, double y, double z) {
        return isXEquals(x) && isYEquals(y) && isZEquals(z);
    }

    /**
     * switches all values in the following system:<br>
     * 1: 0<br>
     * 0: 1<br>
     * 0.5: 0.5
     */
    public void switchValues() {
        set(1d - getDoubleX(), 1d - getDoubleY(), 1d - getDoubleZ());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelativeLocation relLoc = (RelativeLocation) o;
        return x == relLoc.x &&
                y == relLoc.y &&
                z == relLoc.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public RelativeLocation clone() {
        return new RelativeLocation(getDoubleX(), getDoubleY(), getDoubleZ());
    }

    @Override
    public String toString() {
        return getLongX() + ", " + getLongY() + ", " + getLongZ();
    }

    public static RelativeLocation fromString(String s) {
        if (s == null) {
            return null;
        }
        String[] split = s.split(", ");
        if (split.length == 3) {
            try {
                long x = Long.parseLong(split[0]);
                long y = Long.parseLong(split[1]);
                long z = Long.parseLong(split[2]);
                return new RelativeLocation(x, y, z);
            } catch (NumberFormatException e) {

            }
        }
        return null;
    }

}
