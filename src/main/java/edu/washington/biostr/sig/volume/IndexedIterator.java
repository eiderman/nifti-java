package edu.washington.biostr.sig.volume;

import java.util.NoSuchElementException;

import javax.vecmath.Point3f;

/**
 * Provide an implementation of the multidimensional VolumeArrayIterator.  This
 * is appropriate to use with any IndexedVolumeArray and efficiently tracks
 * the current index along with the focused voxel.
 * @author Eider Moore
 * @version 1.0
 */
public class IndexedIterator
        implements VolumeArrayIterator {

    private int index;
    private int x;
    private int y;
    private int z;
    private int t;
    private int i5;
    private int yskip = 0;
    private int zskip = 0;
    private int minX;
    private int minY;
    private int minZ;
    private int maxX;
    private int maxY;
    private int maxZ;
    private int maxT;
    private int maxI5;
    private int down;
    private int right;
    private int in;
    private int forward;
    private int nextI5;
    private int maxindex;
    private int minIndex;
    private IndexedVolumeArray data;

    /**
     * The first call to next will return the data at (x,y,z,t,i5).
     * @param x
     * @param y
     * @param z
     * @param t
     * @param i5
     * @param data The source
     */
    protected IndexedIterator(int x, int y, int z, int t, int i5,
            IndexedVolumeArray data) {
        this.index = data.getIndex(x, y, z, t, i5);
        this.data = data;
        this.x = x;
        this.y = y;
        this.z = z;
        this.t = t;

        maxX = data.getMaxX() - 1;
        maxY = data.getMaxY() - 1;
        maxZ = data.getMaxZ() - 1;
        maxT = data.getMaxTime() - 1;
        maxI5 = data.getMaxI5() - 1;
        maxindex = data.getIndex(maxX, maxY, maxZ, maxT, maxI5);
        minIndex = data.getIndex(minX, minY, minZ, 0, 0);

        down = data.getIndex(0, 1, 0, 0, 0);
        right = data.getIndex(1, 0, 0, 0, 0);
        in = data.getIndex(0, 0, 1, 0, 0);
        forward = data.getIndex(0, 0, 0, 1, 0);
    }

    /**
     * Put this image at (0,0,0,time)
     */
    public void reset() {
        index = minIndex + forward * t;
        x = minX;
        y = minY;
        z = minZ;
    }

    /**
     * move the index up and x--
     * @return index
     */
    private final int goLeft() {
        x--;
        index -= right;
        return index;
    }

    public final int goPrevI5() {
        i5--;
        index -= nextI5;
        return index;
    }

    public final int goNextI5() {
        i5++;
        index += nextI5;
        return index;
    }

    /**
     * move the index down and x++.
     * @return index
     */
    private final int goRight() {
        x++;
        index += right;
        return index;
    }

    /**
     * move the index up and y--
     * @return index
     */
    private final int goUp() {
        y--;
        index -= down;
        return index;
    }

    /**
     * move the index down and y++.
     * @return index
     */
    private final int goDown() {
        y++;
        index += down;
        return index;
    }

    /**
     * move the index in and z++
     * @return index
     */
    private final int goIn() {
        z++;
        index += in;
        return index;
    }

    /**
     * move the index out and z--.
     * @return index
     */
    private final int goOut() {
        z--;
        index -= in;
        return index;
    }

    /**
     * move the index in and t++
     * @return index
     */
    private final int goForward() {
        t++;
        index += forward;
        return index;
    }

    /**
     * move the index out and t--.
     * @return index
     */
    private final int goBack() {
        t--;
        index -= forward;
        return index;
    }

    private final int goPrev() {
        index--;
        if (x > minX) {
            x--;
        } else if (y > minY) {
            x = maxX;
            y--;
        } else if (z > minZ) {
            x = maxX;
            y = maxY;
            z--;
        } else if (t > 0) {
            x = maxX;
            y = maxY;
            z = maxZ;
            t--;
        } else {
            throw new NoSuchElementException("Index out of bounds");
        }
        return index;
    }

    public final int goNext() {
        index++;
        if (x < maxX) {
            x++;
        } else if (y < maxY) {
            x = minX;
            index += yskip;
            y++;
        } else if (z < maxZ) {
            x = minX;
            y = minY;
            index += zskip;
            z++;
        } else if (t < maxT) {
            x = minX;
            y = minY;
            z = minZ;
            t++;
            index = data.getIndex(x, y, z, t, i5);
        } else {
            throw new NoSuchElementException("Index out of bounds: (" + x + ", " +
                    y + ", " + z + ", " + t + "), " +
                    index);
        }
        return index;
    }

    public DataType getNaturalType() {
        return data.getNaturalType();
    }

    public Object prev() {
        return new Double(data.getDouble(goPrev()));
    }

    public int prevInt() {
        return data.getInt(goPrev());
    }

    public double prevDouble() {
        return data.getDouble(goPrev());
    }

    public boolean hasPrev() {
        return index > minIndex;
    }

    public Object peakPrev() {
        return new Double(data.getDouble(index - 1));
    }

    public int peakPrevInt() {
        return data.getInt(index - 1);
    }

    public double peakPrevDouble() {
        return data.getDouble(index - 1);
    }

    public Object next() {
        return new Double(data.getDouble(goNext()));
    }

    public int nextInt() {
        return data.getInt(goNext());
    }

    public double nextDouble() {
        return data.getDouble(goNext());
    }

    public Object peakNext() {
        return new Double(data.getDouble(index + 1));
    }

    public int peakNextInt() {
        return data.getInt(index + 1);
    }

    public double peakNextDouble() {
        return data.getDouble(index + 1);
    }

    public Object right() {
        return new Double(data.getDouble(goRight()));
    }

    public int rightInt() {
        return data.getInt(goRight());
    }

    public double rightDouble() {
        return data.getDouble(goRight());
    }

    public boolean hasRight() {
        return (x < maxX);
    }

    public Object peakRight() {
        return new Double(data.getDouble(index + right));
    }

    public int peakRightInt() {
        return data.getInt(index + right);
    }

    public double peakRightDouble() {
        return data.getDouble(index + right);
    }

    public Object left() {
        return new Double(data.getDouble(goLeft()));
    }

    public int leftInt() {
        return data.getInt(goLeft());
    }

    public double leftDouble() {
        return data.getDouble(goLeft());
    }

    public boolean hasLeft() {
        return (x > minX);
    }

    public Object peakLeft() {
        return new Double(data.getDouble(index - right));
    }

    public int peakLeftInt() {
        return data.getInt(index - right);
    }

    public double peakLeftDouble() {
        return data.getDouble(index - right);
    }

    public Object up() {
        return new Double(data.getDouble(goUp()));
    }

    public int upInt() {
        return data.getInt(goUp());
    }

    public double upDouble() {
        return data.getDouble(goUp());
    }

    public boolean hasUp() {
        return (y > minY);
    }

    public Object peakUp() {
        return new Double(data.getDouble(index - down));
    }

    public int peakUpInt() {
        return data.getInt(index - down);
    }

    public double peakUpDouble() {
        return data.getDouble(index - down);
    }

    public Object down() {
        return new Double(data.getDouble(goDown()));
    }

    public int downInt() {
        return data.getInt(goDown());
    }

    public double downDouble() {
        return data.getDouble(goDown());
    }

    public boolean hasDown() {
        return (y < maxY);
    }

    public Object peakDown() {
        return new Double(data.getDouble(index + down));
    }

    public int peakDownInt() {
        return data.getInt(index + down);
    }

    public double peakDownDouble() {
        return data.getDouble(index + down);
    }

    public Object in() {
        return new Double(data.getDouble(goIn()));
    }

    public int inInt() {
        return data.getInt(goIn());
    }

    public double inDouble() {
        return data.getDouble(goIn());
    }

    public boolean hasIn() {
        return (z < maxZ);
    }

    public Object peakIn() {
        return new Double(data.getDouble(index + in));
    }

    public int peakInInt() {
        return data.getInt(index + in);
    }

    public double peakInDouble() {
        try {
            return data.getDouble(index + in);
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    public Object out() {
        return new Double(data.getDouble(goOut()));
    }

    public int outInt() {
        return data.getInt(goOut());
    }

    public double outDouble() {
        return data.getDouble(goOut());
    }

    public boolean hasOut() {
        return (z > minZ);
    }

    public Object peakOut() {
        return new Double(data.getDouble(index - in));
    }

    public int peakOutInt() {
        return data.getInt(index - in);
    }

    public double peakOutDouble() {
        return data.getDouble(index - in);
    }

    public Object forward() {
        return new Double(data.getDouble(goForward()));
    }

    public int forwardInt() {
        return data.getInt(goForward());
    }

    public double forwardDouble() {
        return data.getDouble(goForward());
    }

    public boolean hasForward() {
        return (t <= maxT);
    }

    public Object peakForward() {
        return new Double(data.getDouble(index + forward));
    }

    public int peakForwardInt() {
        return data.getInt(index + forward);
    }

    public double peakForwardDouble() {
        return data.getDouble(index + forward);
    }

    public Object back() {
        return new Double(data.getDouble(goBack()));
    }

    public int backInt() {
        return data.getInt(goBack());
    }

    public double backDouble() {
        return data.getDouble(goBack());
    }

    public boolean hasBack() {
        return (t > 0);
    }

    public Object peakBack() {
        return new Double(data.getDouble(index - forward));
    }

    public int peakBackInt() {
        return data.getInt(index - forward);
    }

    public double peakBackDouble() {
        return data.getDouble(index - forward);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public int getTime() {
        return t;
    }

    public boolean hasNext() {
        return index < maxindex;
    }

    public void remove() {
        throw new java.lang.UnsupportedOperationException(
                "We don't support removal");
    }

    /**
     * This toString is merely for debuging purposes only.
     * @return
     */
    public String toString() {
        String result = "IndexedIterator: " + index + " (" + getX() + ", " + getY() +
                ", " + getZ() + ", " + getTime() + ")";
        return result;
    }

    public long getLocationLong() {
        return index;
    }

    public void setLocationLong(long location) {
        this.index = (int) location;
    }

    public Point3f getCoord(Point3f p) {
        p.set(x, y, z);
        data.getIndex2Space().transform(p);
        return p;
    }

    public int getI5() {
        return i5;
    }

    public boolean hasNextI5() {
        return i5 < maxI5;
    }

    public boolean hasPrevI5() {
        return i5 > 0;
    }

    public Object peakPrevI5() {
        return new Double(peakPrevI5Double());
    }

    public double peakPrevI5Double() {
        return data.getDouble(index - nextI5);
    }

    public int peakPrevI5Int() {
        return data.getInt(index - nextI5);
    }

    public Object prevI5() {
        return new Double(prevI5Double());
    }

    public double prevI5Double() {
        return data.getDouble(goPrevI5());
    }

    public int prevI5Int() {
        return data.getInt(goPrevI5());
    }

    public Object peakNextI5() {
        return new Double(peakNextI5Double());
    }

    public double peakNextI5Double() {
        return data.getDouble(index - nextI5);
    }

    public int peakNextI5Int() {
        return data.getInt(index - nextI5);
    }

    public Object nextI5() {
        return new Double(nextI5Double());
    }

    public double nextI5Double() {
        return data.getDouble(goNextI5());
    }

    public int nextI5Int() {
        return data.getInt(goNextI5());
    }

    public void setNext(int x, int y, int z, int t, int i5) {
        this.index = data.getIndex(x, y, z, t, i5);
        this.x = x;
        this.y = y;
        this.z = z;
        this.t = t;
        this.i5 = i5;
        try {
            goPrev();
        } catch (NoSuchElementException e) {
            // in the rare case that we are at 0, 0, 0, 0, 0
            this.index = -1;
            this.x = minX;
        }
    }

    /**
     * Get the current index.
     * @return The index that was used for the last result.
     */
    public int getIndex() {
        return index;
    }
    
    public void setBounds(int x_min, int x_max, int y_min, int y_max,
    		int z_min, int z_max) {
    	if (x_max > data.getMaxX() || y_max > data.getMaxY() || z_max > data.getMaxZ()) {
    		throw new IllegalArgumentException(String.format("Max out of range: (%d,%d,%d) > (%d,%d,%d)", x_max, y_max, z_max, data.getMaxX(), data.getMaxY(), data.getMaxZ()));
    	}
    	if (x_min < 0 || y_min < 0 || z_min < 0) {
    		throw new IllegalArgumentException(String.format("Min out of range: (%d,%d,%d) < (0,0,0)", x_min, y_min, z_min));
    	}
    	if (x_max <= x_min || y_max <= y_min || z_max <= z_min) {
    		throw new IllegalArgumentException(String.format("Max is less than min!: (%d,%d,%d) <= (%d,%d,%d)", x_max, y_max, z_max, x_min, y_min, z_min));
    	}
    	this.maxX = x_max - 1;
    	this.maxY = y_max - 1;
    	this.maxZ = z_max - 1;
    	this.minX = x_min;
    	this.minY = y_min;
    	this.minZ = z_min;
    	this.maxindex = data.getIndex(maxX, maxY, maxZ, maxT, maxI5);
    	this.minIndex = data.getIndex(minX, minY, minZ, 0, 0);
    	
    	int i1 = data.getIndex(maxX, minY, minZ, 0, 0);
    	int i2 = data.getIndex(minX, minY + 1, minZ, 0, 0);
    	int i3 = data.getIndex(maxX, maxY, minZ, 0, 0);
    	int i4 = data.getIndex(minX, minY, minZ + 1, 0, 0);
    	yskip = i2 - i1 - 1;
    	zskip = i4 - i3 - 1;
		index = minIndex - 1;
		x = minX - 1;
		y = minY;
		z = minZ;
		i5 = 0;
		t = 0;
    }
}
