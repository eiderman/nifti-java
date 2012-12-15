package edu.washington.biostr.sig.volume;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Random;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import edu.washington.biostr.sig.volume.colors.ColorLookupTable;
import edu.washington.biostr.sig.volume.colors.ColorTransformTable;

/**
 * A VolumeArray contains the backing data for a 3D Volume Image.  It functions
 * to retrieve useful information such as points in index coordinates or its
 * space's coordinates.  It also lets you get a lines of data points and
 * rasters of data.  Before the constructor returns, imageMin and imageMax should
 * be set to the correct values.  setMinMax() is provided as a convenience.<br>
 * When subclassing this, you should override interpolate.  There is a convenience
 * implementation, but since it uses the getDouble(i, j, k) method (and 
 * IndexedVolumeArray provides a more efficient solution), if you have
 * a more efficient way of getting a predefined index, you should.  This method
 * will be called a lot by looping code.  Very few other methods are likely to
 * give a large perfomance benefit by being overridden, but they all might give
 * a slight benefit.<br>
 * It is not synchronized, so it will cause problems if it can be changed once
 * it is put in the rest of the multithreaded program.<br>
 * @author Eider Moore
 * @version 1.0
 */
public abstract class VolumeArray implements Serializable, Iterable<Object> {

    /**
     * Used for comparing doubles to almost 0.
     */
    protected static final double EPSILON = .005;

    /**
     * Aligned with the X axis
     */
    public static final int X = 0x1;
    /**
     * Aligned with the Y axis
     */
    public static final int Y = 0x2;
    /**
     * Aligned with the Z axis
     */
    public static final int Z = 0x3;
    /**
     * Minus direction
     */
    public static final int MINUS = 0x10;
    /**
     * Positive direction
     */
    public static final int PLUS = 0x20;
    public static final int NONE = 0;
    /**
     * X | MINUS
     */
    public static final int X_MINUS = X | MINUS;
    /**
     * X | PLUS
     */
    public static final int X_PLUS = X | PLUS;
    /**
     * Y | MINUS
     */
    public static final int Y_MINUS = Y | MINUS;
    /**
     * Y | PLUS
     */
    public static final int Y_PLUS = Y | PLUS;
    /**
     * Z | MINUS
     */
    public static final int Z_MINUS = Z | MINUS;
    /**
     * Z | PLUS
     */
    public static final int Z_PLUS = Z | PLUS;
    private double mmPerX;
    private double mmPerY;
    private double mmPerZ;
    private Matrix4d mIndex2space;
    private Matrix4d mspace2Index;
    /**
     * This is the maximum x value in the array.
     */
    protected final int maxX;
    /**
     * This is the maximum y value in the array.
     */
    protected final int maxY;
    /**
     * This is the maximum z value in the array.
     */
    protected final int maxZ;
    /**
     * This is the maximum time value in the array.
     */
    protected final int maxTime;
    /**
     * This is the maximum fifth index, which has no standard
     * definition.
     */
    protected final int maxI5;
    protected double imageMax;
    protected double imageMin;

    
    private Plane[] box;
    

    /**
     * 
     * @param maxX The maximum for the x dimension (fastest changing of the spacial indices)
     * @param maxY The maximum for the y dimension.
     * @param maxZ The maximum for the z dimension (slowest changing of the spacial indices).
     * @param maxTime The maximum for the time dimension.  Usually 1.
     * @param maxI5 The maximum for the 5th dimension.  Usually 1.
     * @param index2space The transform, usually turns indices to mm coordinates.  Must be invertable.
     */
    public VolumeArray(int maxX, int maxY, int maxZ, int maxTime, int maxI5,
            Matrix4d index2space) {
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.maxI5 = maxI5;
        this.maxTime = maxTime;
        mspace2Index = new Matrix4d(index2space);
        mIndex2space = new Matrix4d(index2space);

        mspace2Index.invert();

        Vector3f p0 = new Vector3f(1,1,1);

        mIndex2space.transform(p0);
        mmPerX = Math.abs(p0.x);
        mmPerY = Math.abs(p0.y);
        mmPerZ = Math.abs(p0.z);
        
        box = new Plane[6];
        box[0] = new Plane(new Vector3f(0, 0, 1), maxZ - 1);
        box[1] = new Plane(new Vector3f(0, 0, -1), 0);
        box[2] = new Plane(new Vector3f(0, 1, 0), maxY - 1);
        box[3] = new Plane(new Vector3f(0, -1, 0), 0);
        box[4] = new Plane(new Vector3f(1, 0, 0), maxX - 1);
        box[5] = new Plane(new Vector3f(-1, 0, 0), 0);
    }

    /**
     * Get the transform that transforms an index to the space.
     * @return The transform from index -> mm
     */
    public Matrix4d getIndex2Space() {
        return mIndex2space;
    }

    /**
     * Get the transform that transforms a space to the index.
     * @return The transform from mm -> index
     */
    public Matrix4d getSpace2Index() {
        return mspace2Index;
    }

    /**
     * Returns the number of time components.
     * @return
     */
    public int getMaxTime() {
        return maxTime;
    }

    /**
     * Get the number of x indices.
     * @return
     */
    public int getMaxX() {
        return maxX;
    }

    /**
     * Get the number of y indices.
     * @return
     */
    public int getMaxY() {
        return maxY;
    }

    /**
     * Get the number of z indices.
     * @return
     */
    public int getMaxZ() {
        return maxZ;
    }

    /**
     * Get the number of i5 indices.
     * @return
     */
    public int getMaxI5() {
        return maxI5;
    }

    /**
     * Get the maximum value currently stored in the image.
     * @return
     */
    public double getImageMax() {
        return imageMax;
    }

    /**
     * Get the minimum value currently stored in the image.
     * @return
     */
    public double getImageMin() {
        return imageMin;
    }

    /**
     * Set the minimum and maximum for the image.  It should only traverse the
     * image once.<br>
     * Implementation Note: This uses the iterator to iterate over the entire
     * image and will store the min and max as doubles.
     * @param highRes if true, find the real min and max.  If false then approximate it.
     */
    protected void setMinMax(boolean highRes) {
        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;
        double value;
        int SAMPLE_SIZE = 10000;
        if (highRes || maxX * maxY * maxZ * maxTime <= SAMPLE_SIZE * 2) {
            for (VolumeArrayIterator it = iterator(); it.hasNext();) {
                value = it.nextDouble();
                if (value > max) {
                    max = value;
                }
                if (value < min) {
                    min = value;
                }
            }
        } else {
            Iterator<Double> it = randomSampling(SAMPLE_SIZE);
            while (it.hasNext()) {
                value = it.next();
                if (value > max) {
                    max = value;
                }
                if (value < min) {
                    min = value;
                }
            }
        }
        this.imageMax = max;
        this.imageMin = min;
    }

    /**
     * get a double close to the value specified by the coordinates in this's space.
     * Implementation Note:  For higher perfomance, use getDouble(p, time) with a point that
     * can be changed and that is reused for each method call..
     * Implementation Note: This just truncates the int.
     * @param x
     * @param y
     * @param z
     * @param time
     * @param i5
     * @return
     */
    public double getDouble(float x, float y, float z, int time, int i5) {
        Point3f p = new Point3f(x, y, z);
        double answer = getDouble(p, time, i5);
        return answer;
    }

    /**
     * Get an int close to that specified by the place in the space.  p will be changed as
     * a result of this function.  So use getDouble(x,y,z,time) if you don't want
     * p changed.  This returns a value close to the index.  <br>
     * Implementation Note: This just truncates the int.
     * @param p
     * @param time
     * @param i5
     * @return
     */
    public int getInt(Point3f p, int time, int i5) {
        mspace2Index.transform(p);
        return getInt((int) p.x, (int) p.y, (int) p.z, time, i5);
    }

    /**
     * get an int close to that specified in this things space.
     * Implementation Note: To reduce object creation/garbage collection, this
     * function will usually not create an object, but will access synchronized
     * code.  For higher perfomance, use getInt(p, time) with a point that
     * can be changed and that is reused for each method call..
     * @param x
     * @param y
     * @param z
     * @param time
     * @param i5
     * @return
     */
    public int getInt(float x, float y, float z, int time, int i5) {
        Point3f p = new Point3f(x, y, z);
        double answer = getDouble(p, maxTime == 1 ? 0 : time, i5);
        return (int) answer;
    }

    /**
     * Get a double close to the specified by the place in the space.  p will be changed as
     * a result of this function.  So use getDouble(x,y,z,time) if you don't want
     * p changed.  This returns a value close to the index.  <br>
     * Implementation Note: This just uses nearest neighbor.
     * @param p
     * @param time
     * @param i5 the fifth index
     * @return
     */
    public double getDouble(Point3f p, int time, int i5) {
        mspace2Index.transform(p);
        return getDouble(quickRoundPositive(p.x), quickRoundPositive(p.y), quickRoundPositive(p.z),
                maxTime == 1 ? 0 : time,
                i5);
    }

    /**
     * get a double for the value specified by the coordinates in this's space.
     * This use interpolation to get the double that best matches this location.
     * Implementation Note: To reduce object creation/garbage collection, this
     * function will usually not create an object, but will access synchronized
     * code.  For higher perfomance, use getBestDouble(p, time) with a point that
     * can be changed and that is reused for each method call..
     * @param x
     * @param y
     * @param z
     * @param time
     * @param i5
     * @return
     */
    public double getBestDouble(float x, float y, float z, int time, int i5) {
        Point3f p = new Point3f(x, y, z);
        double answer = getBestDouble(p, time, i5);
        return answer;
    }

    public Object get(float x, float y, float z, int time, int i5) {
        return new Double(getDouble(x, y, z, time, i5));
    }

    /**
     * Get the double specified by the place in the space.  p will be changed as
     * a result of this function.  So use getDouble(x,y,z,time) if you don't want
     * p changed.  This returns a value close to the index.  <br>
     * This use interpolation to get the double that best matches this location.
     * @param p
     * @param time
     * @param i5
     * @return
     */
    public double getBestDouble(Point3f p, int time, int i5) {
        mspace2Index.transform(p);
        return interpolate(p.x, p.y, p.z, maxTime == 1 ? 0 : time, i5);
    }

    /**
     * get an double value for the specified index.
     * @param i
     * @param j
     * @param k
     * @param time
     * @param i5
     * @return an double value for the specified index.
     */
    public abstract double getDouble(int i, int j, int k, int time, int i5);

    /**
     * get an integer value for the specified index.
     * @param i
     * @param j
     * @param k
     * @param time
     * @param i5
     * @return an integer value for the specified index.
     */
    public abstract int getInt(int i, int j, int k, int time, int i5);

    /**
     * Determine if the line specified by p1, p2 is part of the natural coordinates
     * and return the coord and direction (X_MINUS, X_PLUS, Y_MINUS...).  This is
     * used for optimizations.  The points specified are in index space. By natural
     * coordinates I mean that an iterator in a given direction can
     * go through this data.  It may require 2 iterators to interpolate, but
     * even if that is the case, this should return true.
     * @param p1 The first point
     * @param p2 The second point
     * @return one of NONE, X_MINUS, X_PLUS, Y_MINUS, etc.
     */
    public abstract int lineNatural(Point3f p1, Point3f p2);

    /**
     * Fill the given array with values for the line.<br>
     * Implementation Note: this calls getLine(p1, p2, time, double[] arr)
     * @param x1
     * @param y1
     * @param z1
     * @param x2
     * @param y2
     * @param z2
     * @param time
     * @param i5
     * @param arr The array to fill with data.  The length determines resolution.
     * @returns arr.
     */
    public double[] getLine(float x1, float y1, float z1,
            float x2, float y2, float z2, int time, int i5, double[] arr) {
        Point3f p1 = new Point3f(x1, y1, z1);
        Point3f p2 = new Point3f(x2, y2, z2);
        getLine(p1, p2, time, i5, arr);

        return arr;
    }

    /**
     * Fill the given line with values.  This uses interpolate(), so it is
     * important that interpolate() is fast.  p1 and p2 are likely to change
     * values.  If you care use getLine(x,y,z,x,y,z,time,arr).<br>
     * @todo use lineNatural and iterator to optimize.
     * @param p1
     * @param p2
     * @param time
     * @param i5
     * @param arr Fill arr with the evenly spaced values from p1 to p2
     * @return arr
     */
    public double[] getLine(Point3f p1, Point3f p2, int time, int i5, double[] arr) {
        mspace2Index.transform(p1);
        mspace2Index.transform(p2);
        Point3f step = new Point3f();
        step.sub(p2, p1);
        step.scale(1.0f / arr.length);

        for (int i = 0; i < arr.length; i++) {
            arr[i] = interpolate(p1.x, p1.y, p1.z, time, i5);
            p1.add(step);
        }
        return arr;
    }

    /**
     * Set the specified voxel to value.
     * @param x
     * @param y
     * @param z
     * @param t
     * @param i5
     * @param value
     */
    public abstract void setData(int x, int y, int z, int t, int i5, double value);

    /**
     * Set the specified voxel to value.
     * @param x
     * @param y
     * @param z
     * @param t
     * @param i5
     * @param value
     */
    public abstract void setData(int x, int y, int z, int t, int i5, int value);

    protected int getFloatAnd(float v) {
        if (v > 1) {
            v = 1f;
        }
        int r = (int) (v * 255);
        r = r << 24 | 0xffFFff;
        return r;
    }

    /**
     * Extract an image that is specified by the given slice.  The slice is
     * specified with the upper left hand corner (in mm), the right direction and
     * length (in mm) and the down direction and length (also in mm).
     * @param upperleftIn
     * @param rightIn
     * @param downIn
     * @param time The time point to focus on
     * @param i5 The i5 point to focus on
     * @param width The width in pixels
     * @param height The height in pixels
     * @param alpha Support alpha.
     * @param alphaV if alpha is supported, use alphaV (between 0 and 1).
     * @param table The table to convert values to RGB, must be specified for non RGB images.
     * @param interpolation Nearest Neighbor or Linear
     * @return A new image that is width X height.
     */
    public BufferedImage getImage(Point3f upperleftIn, Vector3f rightIn,
            Vector3f downIn,
            int time, int i5, int width, int height, boolean alpha,
            ColorLookupTable table, Interpolation interpolation) {
        return getImage(upperleftIn, rightIn, downIn, time, i5, width, height,
                alpha, 1f, table, null, interpolation);
    }

    /**
     * Extract an image that is specified by the given slice.  The slice is
     * specified with the upper left hand corner (in mm), the right direction and
     * length (in mm) and the down direction and length (also in mm).  Use this
     * for only RGB based images.
     * @param upperleftIn
     * @param rightIn
     * @param downIn
     * @param time The time point to focus on
     * @param i5 The i5 point to focus on
     * @param width The width in pixels
     * @param height The height in pixels
     * @param alpha Support alpha.
     * @param alphaV if alpha is supported, use alphaV (between 0 and 1).
     * @param table The table to convert values to RGB, must be specified for non RGB images.
     * @param interpolation Nearest Neighbor or Linear
     * @return A new image that is width X height.
     */
    public BufferedImage getImage(Point3f upperleftIn, Vector3f rightIn,
            Vector3f downIn,
            int time, int i5, int width, int height, boolean alpha,
            float alphaV, ColorLookupTable table, Interpolation interpolation) {
        return getImage(upperleftIn, rightIn, downIn, time, i5, width, height,
                alpha, alphaV, table, null, interpolation);
    }

    /**
     * Extract an image that is specified by the given slice.  The slice is
     * specified with the upper left hand corner (in mm), the right direction and
     * length (in mm) and the down direction and length (also in mm).
     * @param upperleftIn
     * @param rightIn
     * @param downIn
     * @param time The time point to focus on
     * @param i5 The i5 point to focus on
     * @param width The width in pixels
     * @param height The height in pixels
     * @param alpha Support alpha.
     * @param alphaV if alpha is supported, use alphaV (between 0 and 1).
     * @param table The table to convert values to RGB, must be specified for non RGB images.
     * @param colorTransform Used to transform RGB values to new colors.
     * @param interpolation Nearest Neighbor or Linear
     * @return A new image that is width X height.
     */
    public BufferedImage getImage(Point3f upperleftIn, Vector3f rightIn,
            Vector3f downIn,
            int time, int i5, int width, int height, boolean alpha,
            float alphaV, ColorLookupTable table,
            ColorTransformTable colorTransform, Interpolation interpolation) {
        BufferedImage img;
        int imageType;
        if (alpha) {
            imageType = BufferedImage.TYPE_INT_ARGB;
        } else {
            imageType = BufferedImage.TYPE_INT_RGB;
        }
        img = new BufferedImage(width, height, imageType);
        return getImage(upperleftIn, rightIn, downIn, time, i5, img, alphaV, table, colorTransform, interpolation);
    }

    
    private Matrix3f indexHelper;
    /**
     * Get the best index for the given cardinal direction.  On an aligned image
     * it will be one of i, j or k, but on an oblique image it may be wrong.
     * @param i
     * @param j
     * @param k
     * @param direction
     * @return
     */
    public float getBestIndex(float i, float j, float k, CardinalDirections direction) {
    	Vector3f v = new Vector3f(i, j, k);
    	if (indexHelper == null) {
    		indexHelper = new Matrix3f(
    				OneOrNone(mIndex2space.m00), OneOrNone(mIndex2space.m01), OneOrNone(mIndex2space.m02),
    				OneOrNone(mIndex2space.m10), OneOrNone(mIndex2space.m11), OneOrNone(mIndex2space.m12),
    				OneOrNone(mIndex2space.m20), OneOrNone(mIndex2space.m21), OneOrNone(mIndex2space.m22));
    	}
    	indexHelper.transform(v);
    	switch (direction){
    	case CORONAL:
    		return v.y;
    	case SAGITTAL:
    		return v.x;
    	case TRANSVERSE:
    		return v.z;
		default:
			throw new IllegalArgumentException("direction cannot be Oblique.");
    	}
    }
    
    private int OneOrNone(double v) {
    	if (Math.abs(v) < .000001) {
    		return 0;
    	} else {
    		return 1;
    	}
    }
    
    /**
     * Extract an image that is specified by the given slice.  The slice is
     * specified with the upper left hand corner (in mm), the right direction and
     * length (in mm) and the down direction and length (also in mm).
     * @param upperleftIn
     * @param rightIn
     * @param downIn
     * @param time The time point to focus on
     * @param i5 The i5 point to focus on
     * @param img The image to put this into
     * @param alpha Support alpha.
     * @param alphaV if alpha is supported, use alphaV (between 0 and 1).
     * @param table The table to convert values to RGB, must be specified for non RGB images.
     * @param colorTransform Used to transform RGB values to new colors.
     * @param interpolation Nearest Neighbor or Linear
     * @return A new image that is width X height.
     */
    public BufferedImage getImage(Point3f upperleftIn, Vector3f rightIn,
            Vector3f downIn,
            int time, int i5, BufferedImage img,
            float alphaV, ColorLookupTable table,
            ColorTransformTable colorTransform, Interpolation interpolation) {

        // create copies of upperleft, right and down and put them in index space.
        Point3f upperleft = new Point3f(upperleftIn);
        Vector3f right = new Vector3f(rightIn);
        Vector3f down = new Vector3f(downIn);
        mspace2Index.transform(upperleft);
        mspace2Index.transform(right);
        mspace2Index.transform(down);

        // a reusable array for each row
        int[] row = new int[img.getWidth()];

        // the width and height
        int width = img.getWidth();
        int height = img.getHeight();
        // the corner
        Vector3f v1 = new Vector3f();
        // the current pointer
        Point3f cur = new Point3f();
        Point3f widthStep = new Point3f(right);
        widthStep.scale(1f / width);

        // these loops are substantially the same and done this way to 
        // give maximum speed, I hope.
        /*
                    for (int i = 0; i < height; i++) {
                        v1.scale(((float) i) / height, down);
                        cur.add(upperleft, v1);
                            for (int j = 0; j < width; j++) {
                                int value = ;// specific code goes here
                                row[j] = value;
                                cur.add(widthStep);
                            }
                        img.setRGB(0, i, width, 1, row, 0, width);
                    }    
         */
        int alphaInt = getFloatAnd(alphaV);
        if (colorTransform == null) {
            if (supportsLookupTable()) {
                //value = table.getColor(getValueVoxels(cur.x, cur.y, cur.z, time, i5, interpolation));
                if (Interpolation.NEAREST_NEIGHBOR.equals(interpolation)) {
                    for (int i = 0; i < height; i++) {
                        v1.scale(((float) i) / height, down);
                        cur.add(upperleft, v1);
                            for (int j = 0; j < width; j++) {
                                int value = table.getColor(getValueNearestNeighbor(cur.x, cur.y, cur.z, time, i5));
                                row[j] = value;
                                cur.add(widthStep);
                            }
                        img.setRGB(0, i, width, 1, row, 0, width);
                    }    
                } else {
                    for (int i = 0; i < height; i++) {
                        v1.scale(((float) i) / height, down);
                        cur.add(upperleft, v1);
                            for (int j = 0; j < width; j++) {
                                int value = table.getColor(interpolate(cur.x, cur.y, cur.z, time, i5));
                                row[j] = value;
                                cur.add(widthStep);
                            }
                        img.setRGB(0, i, width, 1, row, 0, width);
                    }    
                }
            } else {
                // value = getValueVoxelsInt(cur.x, cur.y, cur.z, time, i5, interpolation) & alphaInt;
                if (Interpolation.NEAREST_NEIGHBOR.equals(interpolation)) {
                    for (int i = 0; i < height; i++) {
                        v1.scale(((float) i) / height, down);
                        cur.add(upperleft, v1);
                            for (int j = 0; j < width; j++) {
                                int value = getValueNearestNeighborInt(cur.x, cur.y, cur.z, time, i5) & alphaInt;
                                row[j] = value;
                                cur.add(widthStep);
                            }
                        img.setRGB(0, i, width, 1, row, 0, width);
                    }                     
                } else {
                    for (int i = 0; i < height; i++) {
                        v1.scale(((float) i) / height, down);
                        cur.add(upperleft, v1);
                            for (int j = 0; j < width; j++) {
                                int value = (int) interpolate(cur.x, cur.y, cur.z, time, i5) & alphaInt;
                                row[j] = value;
                                cur.add(widthStep);
                            }
                        img.setRGB(0, i, width, 1, row, 0, width);
                    }                     
                }
            }
        } else {
            if (supportsLookupTable()) {
                // value = colorTransform.get(table.getColor(getValueVoxels(cur.x, cur.y, cur.z, time, i5, interpolation)));
                if (Interpolation.NEAREST_NEIGHBOR.equals(interpolation)) {
                    for (int i = 0; i < height; i++) {
                        v1.scale(((float) i) / height, down);
                        cur.add(upperleft, v1);
                            for (int j = 0; j < width; j++) {
                                int value = colorTransform.get(table.getColor(getValueNearestNeighbor(cur.x, cur.y, cur.z, time, i5)));
                                row[j] = value;
                                cur.add(widthStep);
                            }
                        img.setRGB(0, i, width, 1, row, 0, width);
                    }                     
                } else {
                    for (int i = 0; i < height; i++) {
                        v1.scale(((float) i) / height, down);
                        cur.add(upperleft, v1);
                            for (int j = 0; j < width; j++) {
                                int value = colorTransform.get(table.getColor(interpolate(cur.x, cur.y, cur.z, time, i5)));
                                row[j] = value;
                                cur.add(widthStep);
                            }
                        img.setRGB(0, i, width, 1, row, 0, width);
                    }                     
                }
            } else {
                // value = colorTransform.get(getValueVoxelsInt(cur.x, cur.y, cur.z, time, i5, interpolation)) & alphaInt;
                if (Interpolation.NEAREST_NEIGHBOR.equals(interpolation)) {
                    for (int i = 0; i < height; i++) {
                        v1.scale(((float) i) / height, down);
                        cur.add(upperleft, v1);
                            for (int j = 0; j < width; j++) {
                                int value = colorTransform.get(getValueNearestNeighborInt(cur.x, cur.y, cur.z, time, i5)) & alphaInt;
                                row[j] = value;
                                cur.add(widthStep);
                            }
                        img.setRGB(0, i, width, 1, row, 0, width);
                    }                     
                } else {
                    for (int i = 0; i < height; i++) {
                        v1.scale(((float) i) / height, down);
                        cur.add(upperleft, v1);
                            for (int j = 0; j < width; j++) {
                                int value = colorTransform.get((int) interpolate(cur.x, cur.y, cur.z, time, i5)) & alphaInt;
                                row[j] = value;
                                cur.add(widthStep);
                            }
                        img.setRGB(0, i, width, 1, row, 0, width);
                    } 
                }
            }
        }
        return img;
    }

    /**
     * Extract an image that is specified by the given slice.  The slice is
     * specified with the upper left hand corner (in mm), the right direction and
     * length (in mm) and the down direction and length (also in mm).
     * @param upperleftIn
     * @param rightIn
     * @param downIn
     * @param time The time point to focus on
     * @param i5 The i5 point to focus on
     * @param width The width in pixels
     * @param height The height in pixels
     * @param alpha Support alpha.
     * @param alphaV if alpha is supported, use alphaV (between 0 and 1).
     * @param table The table to convert values to RGB, must be specified for non RGB images.
     * @param colorTransform Used to transform RGB values to new colors.
     * @param interpolation Nearest Neighbor or Linear
     * @return A new image that is width X height.
     */
    public BufferedImage renderInto(Point3f upperleftIn, Vector3f rightIn,
            Vector3f downIn,
            int time, int i5, final BufferedImage img,
            float alphaV, ColorLookupTable table,
            ColorTransformTable colorTransform, Interpolation interpolation) {
        // create copies of upperleft, right and down and put them in index space.
        Point3f upperleft = new Point3f(upperleftIn);
        Vector3f right = new Vector3f(rightIn);
        Vector3f down = new Vector3f(downIn);
        mspace2Index.transform(upperleft);
        mspace2Index.transform(right);
        mspace2Index.transform(down);

        // a reusable array for each row
        int[] row = new int[img.getWidth()];

        // the width and height
        int width = img.getWidth();
        int height = img.getHeight();
        // the corner
        Vector3f v1 = new Vector3f();
        // the current pointer
        Point3f cur = new Point3f();
        Point3f widthStep = new Point3f(right);
        widthStep.scale(1f / width);

        // these loops are substantially the same and done this way to 
        // give maximum speed, I hope.
        /*
                    for (int i = 0; i < height; i++) {
                        v1.scale(((float) i) / height, down);
                        cur.add(upperleft, v1);
                            for (int j = 0; j < width; j++) {
                                int value = ;// specific code goes here
                                row[j] = blend(row[j], value, alphaV);
                                cur.add(widthStep);
                            }
                        img.setRGB(0, i, width, 1, row, 0, width);
                    }    
         */
        if (colorTransform == null) {
            if (supportsLookupTable()) {
                //value = table.getColor(getValueVoxels(cur.x, cur.y, cur.z, time, i5, interpolation));
                if (Interpolation.NEAREST_NEIGHBOR.equals(interpolation)) {
                    for (int i = 0; i < height; i++) {
                        img.getRGB(0, i, width, 1, row, 0, width);
                        v1.scale(((float) i) / height, down);
                        cur.add(upperleft, v1);
                            for (int j = 0; j < width; j++) {
                                int value = table.getColor(getValueNearestNeighbor(cur.x, cur.y, cur.z, time, i5));
                                row[j] = blend(row[j], value, alphaV);
                                cur.add(widthStep);
                            }
                        img.setRGB(0, i, width, 1, row, 0, width);
                    }    
                } else {
                    for (int i = 0; i < height; i++) {
                        img.getRGB(0, i, width, 1, row, 0, width);
                        v1.scale(((float) i) / height, down);
                        cur.add(upperleft, v1);
                            for (int j = 0; j < width; j++) {
                                int value = table.getColor(interpolate(cur.x, cur.y, cur.z, time, i5));
                                row[j] = blend(row[j], value, alphaV);
                                cur.add(widthStep);
                            }
                        img.setRGB(0, i, width, 1, row, 0, width);
                    }    
                }
            } else {
                // value = getValueVoxelsInt(cur.x, cur.y, cur.z, time, i5, interpolation) & alphaInt;
                if (Interpolation.NEAREST_NEIGHBOR.equals(interpolation)) {
                    for (int i = 0; i < height; i++) {
                        img.getRGB(0, i, width, 1, row, 0, width);
                        v1.scale(((float) i) / height, down);
                        cur.add(upperleft, v1);
                            for (int j = 0; j < width; j++) {
                                int value = getValueNearestNeighborInt(cur.x, cur.y, cur.z, time, i5);
                                row[j] = blend(row[j], value, alphaV);
                                cur.add(widthStep);
                            }
                        img.setRGB(0, i, width, 1, row, 0, width);
                    }                     
                } else {
                    for (int i = 0; i < height; i++) {
                        img.getRGB(0, i, width, 1, row, 0, width);
                        v1.scale(((float) i) / height, down);
                        cur.add(upperleft, v1);
                            for (int j = 0; j < width; j++) {
                                int value = (int) interpolate(cur.x, cur.y, cur.z, time, i5);
                                row[j] = blend(row[j], value, alphaV);
                                cur.add(widthStep);
                            }
                        img.setRGB(0, i, width, 1, row, 0, width);
                    }                     
                }
            }
        } else {
            if (supportsLookupTable()) {
                // value = colorTransform.get(table.getColor(getValueVoxels(cur.x, cur.y, cur.z, time, i5, interpolation)));
                if (Interpolation.NEAREST_NEIGHBOR.equals(interpolation)) {
                    for (int i = 0; i < height; i++) {
                        img.getRGB(0, i, width, 1, row, 0, width);
                        v1.scale(((float) i) / height, down);
                        cur.add(upperleft, v1);
                            for (int j = 0; j < width; j++) {
                                int value = colorTransform.get(table.getColor(getValueNearestNeighbor(cur.x, cur.y, cur.z, time, i5)));
                                row[j] = blend(row[j], value, alphaV);
                                cur.add(widthStep);
                            }
                        img.setRGB(0, i, width, 1, row, 0, width);
                    }                     
                } else {
                    for (int i = 0; i < height; i++) {
                        img.getRGB(0, i, width, 1, row, 0, width);
                        v1.scale(((float) i) / height, down);
                        cur.add(upperleft, v1);
                            for (int j = 0; j < width; j++) {
                                int value = colorTransform.get(table.getColor(interpolate(cur.x, cur.y, cur.z, time, i5)));
                                row[j] = blend(row[j], value, alphaV);
                                cur.add(widthStep);
                            }
                        img.setRGB(0, i, width, 1, row, 0, width);
                    }                     
                }
            } else {
                // value = colorTransform.get(getValueVoxelsInt(cur.x, cur.y, cur.z, time, i5, interpolation)) & alphaInt;
                if (Interpolation.NEAREST_NEIGHBOR.equals(interpolation)) {
                    for (int i = 0; i < height; i++) {
                        img.getRGB(0, i, width, 1, row, 0, width);
                        v1.scale(((float) i) / height, down);
                        cur.add(upperleft, v1);
                            for (int j = 0; j < width; j++) {
                                int value = colorTransform.get(getValueNearestNeighborInt(cur.x, cur.y, cur.z, time, i5));
                                row[j] = blend(row[j], value, alphaV);
                                cur.add(widthStep);
                            }
                        img.setRGB(0, i, width, 1, row, 0, width);
                    }                     
                } else {
                    for (int i = 0; i < height; i++) {
                        img.getRGB(0, i, width, 1, row, 0, width);
                        v1.scale(((float) i) / height, down);
                        cur.add(upperleft, v1);
                            for (int j = 0; j < width; j++) {
                                int value = colorTransform.get((int) interpolate(cur.x, cur.y, cur.z, time, i5));
                                row[j] = blend(row[j], value, alphaV);
                                cur.add(widthStep);
                            }
                        img.setRGB(0, i, width, 1, row, 0, width);
                    } 
                }
            }
        }
        return img;
    }

    /**
     * Transform p from mm to voxel indices and call getValueVoxels
     * @param p The location in mm.
     * @param time
     * @param i5
     * @param in
     * @return return the value at p after interpolating
     */
    public double getValueMM(Point3f p, int time, int i5, Interpolation in) {
        mspace2Index.transform(p);
        return getValueVoxels(p.x, p.y, p.z, time, i5, in);
    }

    /**
     * This takes floating point representations of the indices (in voxels) and 
     * performs the required interpolation.
     * @param x
     * @param y
     * @param z
     * @param time
     * @param i5
     * @param in
     * @return the value at (x,y,z,time,i5) after interpolating
     */
    public double getValueVoxels(float x, float y, float z, int time, int i5, Interpolation in) {
        if (in == Interpolation.NEAREST_NEIGHBOR) {
            return getValueNearestNeighbor(x, y, z, time, i5);
        } else if (in == Interpolation.LINEAR) {
            return interpolate(x, y, z, time, i5);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private int blend(int dst, int src, float alphaV) {
        int rd = (dst >> 16) & 0xff;
        int gd = (dst >> 8) & 0xff;
        int bd = (dst) & 0xff;
        int as = (src >> 24) & 0xff;
        int rs = (src >> 16) & 0xff;
        int gs = (src >> 8) & 0xff;
        int bs = (src) & 0xff;
        float alpha = alphaV * as / 255f;
        float alpha1 = 1 - alpha;
        int r = ((int) ((rd * alpha1) + (rs * alpha)));
        int g = ((int) ((gd * alpha1) + (gs * alpha)));
        int b = ((int) ((bd * alpha1) + (bs * alpha)));
        return (0xff << 24) | (r << 16) | (g << 8) | b;
    }
    
    private double getValueNearestNeighbor(float x, float y, float z, int time, int i5) {
        return getDouble(quickRoundPositive(x), quickRoundPositive(y), quickRoundPositive(z), time, i5);
    }

    private int getValueNearestNeighborInt(float x, float y, float z, int time, int i5) {
        return getInt(quickRoundPositive(x), quickRoundPositive(y), quickRoundPositive(z), time, i5);
    }

    /**
     * This takes floating point representations of the indices (in voxels) and 
     * performs the required interpolation.
     * @param x
     * @param y
     * @param z
     * @param time
     * @param i5
     * @param in
     * @return the value at (x,y,z,time,i5) after interpolating as an int.
     */
    public int getValueVoxelsInt(float x, float y, float z, int time, int i5, Interpolation in) {
        return (int) getValueVoxels(x, y, z, time, i5, in);
    }

    /**
     * This takes floating point representations of the index and requests an
     * interpolation between the indices for the return value.<br>
     * The default implementation uses the getDouble(int i, int j, int k) method.
     * This means that it will likely be a performance benefit if this is
     * overridden by subclasses.<br>
     * Currently the implementation does a trilinear interpolation.
     * @param x index
     * @param y index
     * @param z index
     * @param time
     * @param i5
     * @return The interpolated value.
     */
    protected double interpolate(float x, float y, float z, int time, int i5) {
        int i = (int) x;
        int j = (int) y;
        int k = (int) z;
        double corner0 = getDouble(i, j, k, time, i5);
        ;
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
            corner1 = getDouble(i + 1, j, k, time, i5);
        }
        if (y - j < EPSILON) {
            corner2 = corner0;
            corner3 = corner1;
        } else {
            corner2 = getDouble(i, j + 1, k, time, i5);
            if (x - i < EPSILON) {
                corner3 = corner2;
            } else {
                corner3 = getDouble(i + 1, j + 1, k, time, i5);
            }
        }
        if (z - k < EPSILON) {
            corner4 = corner0;
            corner5 = corner1;
            corner6 = corner2;
            corner7 = corner3;
        } else {
            corner4 = getDouble(i, j, k + 1, time, i5);
            if (x - i < EPSILON) {
                corner5 = corner4;
            } else {
                corner5 = getDouble(i + 1, j, k + 1, time, i5);
            }
            if (y - j < EPSILON) {
                corner6 = corner4;
            } else {
                corner6 = getDouble(i, j + 1, k + 1, time, i5);
            }
            if (x - i < EPSILON && y - j < EPSILON) {
                corner7 = corner4;
            } else {
                corner7 = getDouble(i + 1, j + 1, k + 1, time, i5);
            }
        }

        return VolumeUtil.linearInterpolate(x, y, z,
                corner0, corner1, corner2, corner3,
                corner4, corner5, corner6, corner7);
    }

    /**
     * Get an iterator that starts at the specified position.
     * @param i
     * @param j
     * @param k
     * @param time
     * @param i5
     * @return an iterator over this volume (starting at (i,j,k,time,i5))
     */
    public abstract VolumeArrayIterator iterator(int i, int j, int k, int time, int i5);

    /**
     * Get an iterator starting at 0, 0, 0.  <br>
     * Implementation Note:  This calls iterator(0, 0, 0, 0), but there is no
     * guarantee that it will do that if it is overridden or rewritten.  It is
     * designed so that a call to right() or next() is required, but no data points
     * are missed (ie the first returned point is (0, 0, 0, 0))
     * @return an iterator over this volume (starting at (0,0,0,0,0))
     */
    public VolumeArrayIterator iterator() {
        return iterator(0, 0, 0, 0, 0);
    }

    /**
     * The natural type is either float or int and is determined based on
     * whether the backing array is an integer or a floating point.
     * @return The natural type (int, rgb or float)
     */
    public abstract DataType getNaturalType();

    /**
     * Returns a type appropriate for saving.  Type can be TYPE_BYTE, TYPE_FLOAT, etc.
     * @return a type appropriate for saving.
     */
    public abstract DataType getType();

    /**
     * @return Get the number of entries in this array.
     */
    public int getNumEntries() {
        return getMaxX() * getMaxTime() * getMaxY() * getMaxZ();
    }

    /**
     * Write this to out.  By default throw unsupported operation
     * @param out
     */
    public void write(ByteEncoder out) throws IOException {
        throw new UnsupportedOperationException("This array does not support being written");
    }

    /**
     * 
     * @return number of mm per index in x direction
     */
    public double getMmPerX() {
        return mmPerX;
    }

    /**
     * 
     * @return number of mm per index in y direction
     */
    public double getMmPerY() {
        return mmPerY;
    }

    /**
     * 
     * @return number of mm per index in z direction
     */
    public double getMmPerZ() {
        return mmPerZ;
    }

    /**
     * Do a random sampling from this volume.  This just hops around randomly
     * and returns intensities from samplings.
     * @param size The maximum number of samplings to take.
     * @return an iterator with size iterations that takes random values from the volume.
     */
    public Iterator<Double> randomSampling(final int size) {
        return new Iterator<Double>() {

            int index = 0;
            Random random = new Random();

            public void remove() {
                throw new UnsupportedOperationException("Remove is not supported!");
            }

            public Double next() {
                int x = random.nextInt(getMaxX());
                int y = random.nextInt(getMaxY());
                int z = random.nextInt(getMaxZ());
                int time = random.nextInt(getMaxTime());
                int i5 = random.nextInt(getMaxI5());
                index++;
                return getDouble(x, y, z, time, i5);
            }

            public boolean hasNext() {
                return index < size;
            }
        };
    }

    /**
     * 
     * @return true if this supports lookup tables for coloring or false if it has internal colors.
     */
    public boolean supportsLookupTable() {
        return !getNaturalType().equals(DataType.TYPE_RGB);
    }

    /**
     * Creata a volume array backed by this one that appears as if filter 
     * has been applied to every voxel
     * @param filter
     * @return The filtered volume.
     */
    public abstract VolumeArray map(VolumeFunction filter);

    //   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
//   {
//      mIndex2space = (Matrix4d) in.readObject();
//      mspace2Index = new Matrix4d();
//      mspace2Index.invert(mIndex2space);
//      maxX = in.readInt();
//      maxY = in.readInt();
//      maxZ = in.readInt();
//      maxTime = in.readInt();
//      imageMax = in.readDouble();
//      imageMin = in.readDouble();
//   }
//   
//   public void writeExternal(ObjectOutput out) throws IOException
//   {
//      out.writeObject(mIndex2space);
//      out.writeInt(maxX);
//      out.writeInt(maxY);
//      out.writeInt
//      // TODO Auto-generated method stub
//      
//   }
    /**
     * Extract a chunk from this volume.  This is a convenience when only a part
     * of the volume is interesting or when a slice is required.
     * @param rv Where to store the result.
     * @param x0 First x
     * @param y0  First y
     * @param z0 First z
     * @param t0 First t
     * @param i5_0 First i5
     * @param width Distance in x dir in voxels.
     * @param height Distance in y dir in voxels.
     * @param depth Distance in z dir in voxels.
     * @param duration Distance in time dir in voxels.
     * @param i5_count Distance in i5 dir in voxels.
     * @return rv
     */
    public int[] getSeries(int[] rv, int x0, int y0, int z0, int t0, int i5_0,
            int width, int height, int depth, int duration, int i5_count) {
        int index = 0;
        for (int m = i5_0; m < i5_count + i5_0; m++) {
            for (int l = t0; l < duration + t0; l++) {
                for (int k = z0; k < depth + z0; k++) {
                    for (int j = y0; j < height + y0; j++) {
                        for (int i = x0; i < width + x0; i++) {
                            rv[index] = getInt(i, j, k, l, m);
                            index++;
                        }
                    }
                }
            }
        }
        return rv;
    }

    /**
     * Set a chunk from this volume.  This is a convenience when only a part
     * of the volume is changed or when a slice is changed.
     * @param values The new data.
     * @param x0 First x
     * @param y0  First y
     * @param z0 First z
     * @param t0 First t
     * @param i5_0 First i5
     * @param width Distance in x dir in voxels.
     * @param height Distance in y dir in voxels.
     * @param depth Distance in z dir in voxels.
     * @param duration Distance in time dir in voxels.
     * @param i5_count Distance in i5 dir in voxels.
     */
    public void setSeries(int[] values, int x0, int y0, int z0, int t0, int i5_0,
            int width, int height, int depth, int duration, int i5_count) {
        int index = 0;
        for (int m = i5_0; m < i5_count + i5_0; m++) {
            for (int l = t0; l < duration + t0; l++) {
                for (int k = z0; k < depth + z0; k++) {
                    for (int j = y0; j < height + y0; j++) {
                        for (int i = x0; i < width + x0; i++) {
                            setData(i, j, k, l, m, values[index]);
                            index++;
                        }
                    }
                }
            }
        }
    }

    /**
     * Set a chunk from this volume.  This is a convenience when only a part
     * of the volume is changed or when a slice is changed.
     * @param values The new data.
     * @param x0 First x
     * @param y0  First y
     * @param z0 First z
     * @param t0 First t
     * @param i5_0 First i5
     * @param width Distance in x dir in voxels.
     * @param height Distance in y dir in voxels.
     * @param depth Distance in z dir in voxels.
     * @param duration Distance in time dir in voxels.
     * @param i5_count Distance in i5 dir in voxels.
     */
    public void setSeries(double[] values, int x0, int y0, int z0, int t0, int i5_0,
            int width, int height, int depth, int duration, int i5_count) {
        int index = 0;
        for (int m = i5_0; m < i5_count + i5_0; m++) {
            for (int l = t0; l < duration + t0; l++) {
                for (int k = z0; k < depth + z0; k++) {
                    for (int j = y0; j < height + y0; j++) {
                        for (int i = x0; i < width + x0; i++) {
                            setData(i, j, k, i, m, values[index]);
                            index++;
                        }
                    }
                }
            }
        }
    }

    /**
     * Extract a chunk from this volume.  This is a convenience when only a part
     * of the volume is interesting or when a slice is required.
     * @param rv Where to store the result.
     * @param x0 First x
     * @param y0  First y
     * @param z0 First z
     * @param t0 First t
     * @param i5_0 First i5
     * @param width Distance in x dir in voxels.
     * @param height Distance in y dir in voxels.
     * @param depth Distance in z dir in voxels.
     * @param duration Distance in time dir in voxels.
     * @param i5_count Distance in i5 dir in voxels.
     * @return rv
     */
    public double[] getSeries(double[] rv, int x0, int y0, int z0, int t0, int i5_0,
            int width, int height, int depth, int duration, int i5_count) {
        int index = 0;
        for (int m = i5_0; m < i5_count + i5_0; m++) {
            for (int l = t0; l < duration + t0; l++) {
                for (int k = z0; k < depth + z0; k++) {
                    for (int j = y0; j < height + y0; j++) {
                        for (int i = x0; i < width + x0; i++) {
                            rv[index] = getDouble(i, j, k, l, m);
                            index++;
                        }
                    }
                }
            }
        }
        return rv;
    }

    /**
     * This is provided because it can be inlined (not native, unlike Math.round)
     * and so is up to 6 times faster.  It is only valid for positive values.
     * @param a a positive value
     * @return the nearest int to a
     */
    public static int quickRoundPositive(float a) {
        return (int) (a + .5f);
    }

    /**
     * This is provided because it can be inlined (not native, unlike Math.round)
     * and so is up to 6 times faster.  It is only valid for positive values.
     * @param a a positive value
     * @return the nearest int to a
     */
    public static int quickRoundPositive(double a) {
        return (int) (a + .5);
    }
    
    /**
     * Calculate the intersection points for the given ray and the bounding box.<br>
     * This uses voxels.
     * @param start The start point
     * @param ray The ray vector
     * @return {entry, exit}, either or both may be null.
     */
    public Point3f[] getIntersectionVoxels(Point3f origin, Vector3f ray) {
    	float t_front = Float.NEGATIVE_INFINITY;
    	float t_back = Float.POSITIVE_INFINITY;
    	
    	for (Plane p : getBoxSides()) {
    		float nDOTdir = ray.dot(p.getNormal());
    		if (nDOTdir > 0) {
    			// back face
    			float t = p.computeT(origin, ray, nDOTdir);
    			if (t < t_back) {
    				t_back = t;
    			}
    		} else if (nDOTdir < 0) {
    			// front face
    			float t = p.computeT(origin, ray, nDOTdir);
    			if (t > t_front) {
    				t_front = t;
    			}
    		}
    	}
    	Point3f p1;
    	Point3f p2;
    	assert !Float.isInfinite(t_front) && !Float.isInfinite(t_back); 
    	if (t_front < 0) {
    		p1 = null;
    	} else {
    		p1 = Plane.computeIntersectionPoint(origin, ray, t_front);
    		if (!insideBounds(p1)) {
    			p1 = null;
    		}
    	}
    	if (t_back < 0) {
    		p2 = null;
    	} else {
    		p2 = Plane.computeIntersectionPoint(origin, ray, t_back);
    		if (!insideBounds(p2)) {
    			p1 = null;
    		}
    	}
    	
    	return new Point3f[] {p1, p2};
    }
    
    /**
     * Test if p is inside the box's bounds
     * @param p
     * @return
     */
    private boolean insideBounds(Point3f p) {
    	int x = Math.round(p.x);
    	int y = Math.round(p.y);
    	int z = Math.round(p.z);
    	
    	return  (x >= 0 && y >= 0 && z >= 0) && 
    			x < maxX && y < maxY && z < maxZ;
	}

	private Plane[] getBoxSides() {
		return box;
    }
    

    /**
     * Calculate the intersection points for the given ray and the bounding box.<br>
     * @param start The start point
     * @param ray The ray vector
     * @return {entry, exit}, either or both may be null.
     */
    public Point3f[] getIntersection(Point3f startMM, Vector3f rayMM) {
    	// convert to voxels and dispatch to voxel form.
    	// this ensures that it is axis aligned.
    	Point3f start = new Point3f(startMM);
    	Vector3f ray = new Vector3f(rayMM);
    	mspace2Index.transform(ray);
    	mspace2Index.transform(start);
    	Point3f[] result = getIntersectionVoxels(start, ray);
    	for (Point3f p : result) {
    		if (p != null)
    			mIndex2space.transform(p);
    	}
    	return result;
    }
}
