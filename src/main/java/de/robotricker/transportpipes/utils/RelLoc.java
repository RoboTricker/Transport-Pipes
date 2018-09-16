package de.robotricker.transportpipes.utils;

import java.util.Objects;

public class RelLoc {

    private static long PRECISION = 100000;

    private long x;
    private long y;
    private long z;

    public RelLoc(double x, double y, double z) {
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

    public void set(double x, double y, double z) {
        this.x = (long) (x * PRECISION);
        this.y = (long) (y * PRECISION);
        this.z = (long) (z * PRECISION);
    }

    public void set(long x, long y, long z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelLoc relLoc = (RelLoc) o;
        return x == relLoc.x &&
                y == relLoc.y &&
                z == relLoc.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f, %.2f)", getDoubleX(), getDoubleY(), getDoubleZ());
    }
}
