package edu.washington.biostr.sig.volume;

import java.awt.Color;
import java.io.IOException;

import javax.vecmath.Matrix4d;

/**
 * Hold 3 channel RGB data in a volume.  The underlying data is an int with
 * 8 bits per channel.  Alpha is not directly supported.
 * @author Eider Moore
 * @version 1.0
 */
public class RGBIndexedVolumeArray extends IntIndexedVolumeArray {

    private int[] array;

    /**
     * 
     * @param maxX The maximum for the x dimension (fastest changing of the spacial indices)
     * @param maxY The maximum for the y dimension.
     * @param maxZ The maximum for the z dimension (slowest changing of the spacial indices).
     * @param maxTime The maximum for the time dimension.  Usually 1.
     * @param maxI5 The maximum for the 5th dimension.  Usually 1.
     * @param index2space The transform, usually turns indices to mm coordinates.  Must be invertable.
     * @param array The data where each entry is a 3 channel RGB component.
     */
    public RGBIndexedVolumeArray(int maxX, int maxY, int maxZ, int maxTime, int maxI5,
            Matrix4d index2space, int[] array) {
        super(maxX, maxY, maxZ, maxTime, maxI5, index2space, array);
        this.array = array;
    }

    @Override
    protected void setMinMax(boolean highRes) {
        // min/max are meaningless in RGB
        imageMin = 0;
        imageMax = 0;
    }

    @Override
    public void write(ByteEncoder out) throws IOException {
        for (int value : array) {
            int r = value >> 16;
            int g = value >> 8;
            int b = value;
            out.getOut().write(r);
            out.getOut().write(g);
            out.getOut().write(b);
        }
        out.getOut().flush();
    }

    @Override
    public DataType getNaturalType() {
        return DataType.TYPE_RGB;
    }

    @Override
    public DataType getType() {
        return DataType.TYPE_RGB;
    }

    /**
     * Interpolation is not supported and nearest neighbor is used instead.
     * @param x
     * @param y
     * @param z
     * @param time
     * @param i5
     * @return The value nearest to x,y,z
     */
    @Override
    protected double interpolate(float x, float y, float z, int time, int i5) {
        return getInt(quickRoundPositive(x), quickRoundPositive(y), quickRoundPositive(z), time, i5);
    }

    /**
     * 
     * @param x
     * @param y
     * @param z
     * @param time
     * @param i5
     * @return Cast the data int a Color object.
     */
    public Object get(float x, float y, float z, int time, int i5) {
        return new Color(getInt(x, y, z, time, i5));
    }

    @Override
    public int getValueVoxelsInt(float x, float y, float z, int time, int i5,
            Interpolation in) {
        switch (in) {
            case NEAREST_NEIGHBOR:
                return getInt(quickRoundPositive(x), quickRoundPositive(y), quickRoundPositive(z), time, i5);
            case LINEAR:
                return interpolateColor(x, y, z, time, i5);
            default:
                throw new IllegalArgumentException();
        }
    }
    private int c0off = getIndex(0, 0, 0, 0, 0);
    private int c1off = getIndex(1, 0, 0, 0, 0);
    private int c2off = getIndex(0, 1, 0, 0, 0);
    private int c3off = getIndex(1, 1, 0, 0, 0);
    private int c4off = getIndex(0, 0, 1, 0, 0);
    private int c5off = getIndex(1, 0, 1, 0, 0);
    private int c6off = getIndex(0, 1, 1, 0, 0);
    private int c7off = getIndex(1, 1, 1, 0, 0);

    /**
     * This is a moderately more efficient way to interpolate.  It gets the
     * offsets when the class is created and then just uses getDouble(index + offset)
     * to fetch the double.  This eliminates several checks and additions/multiplications.
     * @param x
     * @param y
     * @param z
     * @param time
     * @return
     */
    protected int interpolateColor(float x, float y, float z, int time, int i5) {
        int i = (int) x;
        int j = (int) y;
        int k = (int) z;
        if ((x + 1 >= maxX) || (y + 1 >= maxY) || (z + 1 >= maxZ) ||
                (time >= maxTime) ||
                (x < 1) || (y < 1) || (z < 1) || (time < 0)) {
            return 0;
        }

        int index = getIndex(i, j, k, time, i5);
        int corner0 = getInt(index + c0off);
        int corner1;
        int corner2;
        int corner3;
        int corner4;
        int corner5;
        int corner6;
        int corner7;
        if (x - i < EPSILON) {
            corner1 = corner0;
        } else {
            corner1 = getInt(index + c1off);
        }
        if (y - j < EPSILON) {
            corner2 = corner0;
            corner3 = corner1;
        } else {
            corner2 = getInt(index + c2off);
            if (x - i < EPSILON) {
                corner3 = corner2;
            } else {
                corner3 = getInt(index + c3off);
            }
        }
        if (z - k < EPSILON) {
            corner4 = corner0;
            corner5 = corner1;
            corner6 = corner2;
            corner7 = corner3;
        } else {
            corner4 = getInt(index + c4off);
            if (x - i < EPSILON) {
                corner5 = corner4;
            } else {
                corner5 = getInt(index + c5off);
            }
            if (y - j < EPSILON) {
                corner6 = corner4;
            } else {
                corner6 = getInt(index + c6off);
            }
            if (x - i < EPSILON && y - j < EPSILON) {
                corner7 = corner4;
            } else {
                corner7 = getInt(index + c7off);
            }
        }

        int r = 0xff << 16, g = 0xff << 8, b = 0xff;

        double xLoc = x - i;
        double yLoc = y - j;
        double zLoc = z - k;

        int rc = (int) ((corner0 & r) * (1 - xLoc) * (1 - yLoc) * (1 - zLoc) +
                (corner1 & r) * xLoc * (1 - yLoc) * (1 - zLoc) +
                (corner2 & r) * (1 - xLoc) * (yLoc) * (1 - zLoc) +
                (corner3 & r) * (xLoc) * (yLoc) * (1 - zLoc) +
                (corner4 & r) * (1 - xLoc) * (1 - yLoc) * (zLoc) +
                (corner5 & r) * (xLoc) * (1 - yLoc) * (zLoc) +
                (corner6 & r) * (1 - xLoc) * (yLoc) * (zLoc) +
                (corner7 & r) * (xLoc) * (yLoc) * (zLoc));
        int gc = (int) ((corner0 & g) * (1 - xLoc) * (1 - yLoc) * (1 - zLoc) +
                (corner1 & g) * xLoc * (1 - yLoc) * (1 - zLoc) +
                (corner2 & g) * (1 - xLoc) * (yLoc) * (1 - zLoc) +
                (corner3 & g) * (xLoc) * (yLoc) * (1 - zLoc) +
                (corner4 & g) * (1 - xLoc) * (1 - yLoc) * (zLoc) +
                (corner5 & g) * (xLoc) * (1 - yLoc) * (zLoc) +
                (corner6 & g) * (1 - xLoc) * (yLoc) * (zLoc) +
                (corner7 & g) * (xLoc) * (yLoc) * (zLoc));
        int bc = (int) ((corner0 & b) * (1 - xLoc) * (1 - yLoc) * (1 - zLoc) +
                (corner1 & b) * xLoc * (1 - yLoc) * (1 - zLoc) +
                (corner2 & b) * (1 - xLoc) * (yLoc) * (1 - zLoc) +
                (corner3 & b) * (xLoc) * (yLoc) * (1 - zLoc) +
                (corner4 & b) * (1 - xLoc) * (1 - yLoc) * (zLoc) +
                (corner5 & b) * (xLoc) * (1 - yLoc) * (zLoc) +
                (corner6 & b) * (1 - xLoc) * (yLoc) * (zLoc) +
                (corner7 & b) * (xLoc) * (yLoc) * (zLoc));
        return (0xff << 24) | rc | gc | bc;
    }
}
