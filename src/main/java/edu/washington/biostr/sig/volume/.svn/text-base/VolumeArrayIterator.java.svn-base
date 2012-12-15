package edu.washington.biostr.sig.volume;

import java.util.Iterator;

import javax.vecmath.Point3f;

/**
 * The comments for individual functions are rather sparse, but this provides an
 * overview that should be sufficient to tell you how to implement and use
 * these functions.<br>
 * This is an 12-directional iterator that is returned by ImageData.iterator().<br>
 * If we are at index x, y, z, t, i5.  right = x+1, left = x-1, up = y-1, down = y+1,
 * in=z+1, out=z-1, forward=t+1, back=t-1, next5=i5+1, prev5 = i5-1.<br>
 * next moves right and then down and the in:<br>
 * next = (hasRight() ? right() : (hasDown() ? {x=0; down()} :
 * (hasIn() ? {x=0; y=0;in()} : (hasForward() ? {x=0;y=0;z=0;t++}:
 * throw new NoSuchElementException())))).<br>
 * reset() goes to point 0,0,0,t.  It does not reset t.<br>
 * prev is the opposite of next.<br>
 * It also supports doubles and ints and contains the value from ImageData about
 * the natural type.<br>
 * It supports peak in all directions too.  Peak returns the value in that
 * direction without moving the iterator.  If the next value is outside of the image,
 * peak should return 0.<br><br>
 * Implementation suggestion: You should not be creating objects in this class
 * when possible.  Obviously when we are translating from a double to a Double
 * or an int to an Integer it is impossible to avoid making a new object.<br>
 * It is also suggested that any implementation be as optimized as possible.
 * Since the images are large and this is designed specifically for classes that
 * iterate over the entire image, it is fairly critical that this be efficient
 * so that it is not the bottleneck.<br>
 * @author Eider Moore
 * @version 1.0
 */
public interface VolumeArrayIterator
        extends Iterator<Object> {

    public void reset();

    /**
     * Get the natural type supported by this the backing data.
     * @return
     */
    public DataType getNaturalType();

    /**
     * Returns the previous element.
     * @return
     */
    public Object prev();

    public int prevInt();

    public double prevDouble();

    /**
     * true if we have an element before this one.
     * @return
     */
    public boolean hasPrev();

    public Object peakPrev();

    public int peakPrevInt();

    public double peakPrevDouble();

    /**
     * next = (hasRight() ? right() : (hasDown() ? down() : (hasIn() ? in() :
     * NoSuchElementException)))<br>
     */
    public Object next();

    public int nextInt();

    public double nextDouble();

    public Object peakNext();

    public int peakNextInt();

    public double peakNextDouble();

    public Object right();

    public int rightInt();

    public double rightDouble();

    public boolean hasRight();

    public Object peakRight();

    public int peakRightInt();

    public double peakRightDouble();

    public Object left();

    public int leftInt();

    public double leftDouble();

    public boolean hasLeft();

    public Object peakLeft();

    public int peakLeftInt();

    public double peakLeftDouble();

    public Object up();

    public int upInt();

    public double upDouble();

    public boolean hasUp();

    public Object peakUp();

    public int peakUpInt();

    public double peakUpDouble();

    public Object down();

    public int downInt();

    public double downDouble();

    public boolean hasDown();

    public Object peakDown();

    public int peakDownInt();

    public double peakDownDouble();

    public Object in();

    public int inInt();

    public double inDouble();

    public boolean hasIn();

    public Object peakIn();

    public int peakInInt();

    public double peakInDouble();

    public Object out();

    public int outInt();

    public double outDouble();

    public boolean hasOut();

    public Object peakOut();

    public int peakOutInt();

    public double peakOutDouble();

    public Object forward();

    public int forwardInt();

    public double forwardDouble();

    public boolean hasForward();

    public Object peakForward();

    public int peakForwardInt();

    public double peakForwardDouble();

    public Object back();

    public int backInt();

    public double backDouble();

    public boolean hasBack();

    public Object peakBack();

    public int peakBackInt();

    public double peakBackDouble();

    public Object peakNextI5();

    public Object peakPrevI5();

    public double peakNextI5Double();

    public double peakPrevI5Double();

    public int peakNextI5Int();

    public int peakPrevI5Int();

    public Object nextI5();

    public Object prevI5();

    public double nextI5Double();

    public double prevI5Double();

    public int nextI5Int();

    public int prevI5Int();

    public boolean hasNextI5();

    public boolean hasPrevI5();

    /**
     * Return the current x coordinate.
     * @return
     */
    public int getX();

    /**
     * Return the current y coordinate.
     * @return
     */
    public int getY();

    /**
     * Return the current z coordinate.
     * @return
     */
    public int getZ();

    public int getTime();

    public int getI5();

    /**
     * Get the current coordinates in real space.
     * @param p the result array
     * @return p (for ease of use)
     */
    public Point3f getCoord(Point3f p);

    /**
     * Set the next value that will be sampled.
     * @param x
     * @param y
     * @param z
     * @param t
     * @param i5
     */
    public void setNext(int x, int y, int z, int t, int i5);
    
    public void setBounds(int x_min, int x_max, int y_min, int y_max, int z_min, int z_max);
}
