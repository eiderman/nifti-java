package edu.washington.biostr.sig.volume;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3f;

/**
 * This is a base class for image data as a single array.  It assumes that
 * we have an equation that converts the an index in the form of i,j,k,t to a
 * single integer.  This lets it try some minor optimizations including providing
 * a default iterator that is fairly efficient and a more efficient interpolate().
 * These should be efficient enough unless you have a weird index equation because
 * these assume that to get from index(5,6,1,0) to index(6,7,2,1) you can just
 * add index(5,6,1,0) to index(1,1,1,1).  But we don't assume that getIndex()
 * supports negative numbers so to get from index(6,7,2,1) to our original index
 * you can't add index(-1, -1, -1, -1) but must instead subtract index(1,1,1,1).<br>
 * so to summarize, if the following hold then you don't need to change interpolate
 * or iterator:<br>
 * index(x+i, y+j, z+k, t+l) == index(x, y, z) + index(i, j, k, l). <br>
 * index(x-i, y-j, z-k, t-l) == index(x, y, z) - index(i, j, k, l). <br>
 * index(-i, -j, -k, -l) is not usually equal to -index(i, j, k).<br>
 * @author Eider Moore
 * @version 1.0
 */
public abstract class IndexedVolumeArray
        extends VolumeArray {
    

        /**
     * 
     * @param maxX The maximum for the x dimension (fastest changing of the spacial indices)
     * @param maxY The maximum for the y dimension.
     * @param maxZ The maximum for the z dimension (slowest changing of the spacial indices).
     * @param maxTime The maximum for the time dimension.  Usually 1.
     * @param maxI5 The maximum for the 5th dimension.  Usually 1.
     * @param index2space The transform, usually turns indices to mm coordinates.  Must be invertable.
     */
    public IndexedVolumeArray(int maxX, int maxY, int maxZ, int maxTime,
            int maxI5,
            Matrix4d index2space) {
        super(maxX, maxY, maxZ, maxTime, maxI5, index2space);
    }

    public int lineNatural(Point3f p1, Point3f p2) {
        if (p1.x == p2.x) {
            if (p1.y == p2.y) {
                if (p1.z < p2.z) {
                    return Z_PLUS;
                } else {
                    return Z_MINUS;
                }
            } else if (p1.z == p2.z) {
                if (p1.y < p2.y) {
                    return Y_PLUS;
                } else {
                    return Y_MINUS;
                }
            }
        } else if ((p1.y == p2.y) && (p1.z == p2.z)) {
            if (p1.x < p2.x) {
                return X_PLUS;
            } else {
                return X_MINUS;
            }
        }
        return NONE;
    }

    public double getDouble(int i, int j, int k, int time, int i5) {
        int index = getIndex(i, j, k, time, i5);
        if (index < 0) {
            return 0;
        }
        return getDouble(index);
    }

    public IndexedIterator iterator(int i, int j, int k, int time, int i5) {
        return new IndexedIterator(i - 1, j, k, time, i5, this);
    }

    public IndexedIterator iterator() {
        return iterator(0, 0, 0, 0, 0);
    }

    public int getInt(int i, int j, int k, int time, int i5) {
        int index = getIndex(i, j, k, time, i5);
        if (index == -1) {
            return 0;
        } else {
            return getInt(index);
        }
    }

    /**
     * get the int value from this index.
     * @param index
     * @return
     */
    public abstract int getInt(int index);

    /**
     * get the double value from this index.
     * @param index
     * @return
     */
    public abstract double getDouble(int index);

    /**
     * Get the index for this value.  The default equation is:<br>
     * maxX * ((maxZ * time + z) * maxY + y) + x<br>
     * This also checks ranges and returns -1 if x,y,z or time is out of the
     * current range.<br>
     * If the default equation doesn't work for you, override this method.<br>
     * This assumes that the order in the index is x, y, z, t
     * @param x
     * @param y
     * @param z
     * @param time
     * @return
     */
    public int getIndex(int x, int y, int z, int time, int i5) {
        if ((x >= maxX) || (y >= maxY) || (z >= maxZ) || (time >= maxTime) ||
                (x < 0) || (y < 0) || (z < 0) || (time < 0) || (i5 < 0) || (i5 >= maxI5)) {
            return -1;
        }
        return (((i5 * maxTime + time) * maxZ + z) * maxY + y) * maxX + x;
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
    protected double interpolate(float x, float y, float z, int time, int i5) {
        int i = (int) x;
        int j = (int) y;
        int k = (int) z;
        if ((x + 1 >= maxX) || (y + 1 >= maxY) || (z + 1 >= maxZ) ||
                (time >= maxTime) ||
                (x < 1) || (y < 1) || (z < 1) || (time < 0)) {
            return 0;
        }

        int index = getIndex(i, j, k, time, i5);
        double corner0 = getDouble(index + c0off);
        double corner1;
        double corner2;
        double corner3;
        double corner4;
        double corner5;
        double corner6;
        double corner7;
        if (x - i < EPSILON) {
            corner1 = corner0;
        } else {
            corner1 = getDouble(index + c1off);
        }
        if (y - j < EPSILON) {
            corner2 = corner0;
            corner3 = corner1;
        } else {
            corner2 = getDouble(index + c2off);
            if (x - i < EPSILON) {
                corner3 = corner2;
            } else {
                corner3 = getDouble(index + c3off);
            }
        }
        if (z - k < EPSILON) {
            corner4 = corner0;
            corner5 = corner1;
            corner6 = corner2;
            corner7 = corner3;
        } else {
            corner4 = getDouble(index + c4off);
            if (x - i < EPSILON) {
                corner5 = corner4;
            } else {
                corner5 = getDouble(index + c5off);
            }
            if (y - j < EPSILON) {
                corner6 = corner4;
            } else {
                corner6 = getDouble(index + c6off);
            }
            if (x - i < EPSILON && y - j < EPSILON) {
                corner7 = corner4;
            } else {
                corner7 = getDouble(index + c7off);
            }
        }

        double answer = 0.0;

        double xLoc = x - i;
        double yLoc = y - j;
        double zLoc = z - k;

        answer = corner0 * (1 - xLoc) * (1 - yLoc) * (1 - zLoc) +
                corner1 * xLoc * (1 - yLoc) * (1 - zLoc) +
                corner2 * (1 - xLoc) * (yLoc) * (1 - zLoc) +
                corner3 * (xLoc) * (yLoc) * (1 - zLoc) +
                corner4 * (1 - xLoc) * (1 - yLoc) * (zLoc) +
                corner5 * (xLoc) * (1 - yLoc) * (zLoc) +
                corner6 * (1 - xLoc) * (yLoc) * (zLoc) +
                corner7 * (xLoc) * (yLoc) * (zLoc);
        return answer;
    }

    @Override
    public void setData(int x, int y, int z, int t, int i5, double value) {
        int index = getIndex(x, y, z, t, i5);
        if (index < 0) {
            throw new IndexOutOfBoundsException("Out of bounds: (" + x + "," + y + "," + z + "," + t + "," + i5 + ")");
        }
        setData(index, value);
    }

    @Override
    public void setData(int x, int y, int z, int t, int i5, int value) {
        int index = getIndex(x, y, z, t, i5);
        if (index < 0) {
            throw new IndexOutOfBoundsException("Out of bounds: (" + x + "," + y + "," + z + "," + t + "," + i5 + ")");
        }
        setData(index, value);
    }

    @Override
    public VolumeArray map(VolumeFunction filter) {
        return new FilteredIndexedVolumeArray(this, filter);
    }

    @Override
    public int[] getSeries(int[] rv, int x0, int y0, int z0, int t0, int i5_0,
            int width, int height, int depth, int duration, int i5_count) {
        int rindex = 0;
        for (int m = i5_0; m < i5_count + i5_0; m++) {
            for (int l = t0; l < duration + t0; l++) {
                for (int k = z0; k < depth + z0; k++) {
                    for (int j = y0; j < height + y0; j++) {
                        int index = getIndex(x0, j, k, l, m);
                        for (int i = x0; i < width + x0; i++) {
                            rv[rindex] = getInt(index);
                            rindex++;
                            index++;
                        }
                    }
                }
            }
        }
        return rv;
    }

    public void setSeries(int[] values, int x0, int y0, int z0, int t0, int i5_0,
            int width, int height, int depth, int duration, int i5_count) {
        int rindex = 0;
        for (int m = i5_0; m < i5_count + i5_0; m++) {
            for (int l = t0; l < duration + t0; l++) {
                for (int k = z0; k < depth + z0; k++) {
                    for (int j = y0; j < height + y0; j++) {
                        int index = getIndex(x0, j, k, l, m);
                        for (int i = x0; i < width + x0; i++) {
                            setData(index, values[rindex]);
                            rindex++;
                            index++;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setSeries(double[] values, int x0, int y0, int z0, int t0, int i5_0,
            int width, int height, int depth, int duration, int i5_count) {
        int rindex = 0;
        for (int m = i5_0; m < i5_count + i5_0; m++) {
            for (int l = t0; l < duration + t0; l++) {
                for (int k = z0; k < depth + z0; k++) {
                    for (int j = y0; j < height + y0; j++) {
                        int index = getIndex(x0, j, k, l, m);
                        for (int i = x0; i < width + x0; i++) {
                            setData(index, values[rindex]);
                            rindex++;
                            index++;
                        }
                    }
                }
            }
        }
    }

    public double[] getSeries(double[] rv, int x0, int y0, int z0, int t0, int i5_0,
            int width, int height, int depth, int duration, int i5_count) {
        int rindex = 0;
        for (int m = i5_0; m < i5_count + i5_0; m++) {
            for (int l = t0; l < duration + t0; l++) {
                for (int k = z0; k < depth + z0; k++) {
                    for (int j = y0; j < height + y0; j++) {
                        int index = getIndex(x0, j, k, l, m);
                        for (int i = x0; i < width + x0; i++) {
                            rv[rindex] = getDouble(index);
                            rindex++;
                            index++;
                        }
                    }
                }
            }
        }
        return rv;
    }

    public abstract void setData(int index, double value);

    public abstract void setData(int index, int value);
    
    public abstract Object getDataArray();
}
