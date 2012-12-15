package edu.washington.biostr.sig.volume;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import edu.washington.biostr.sig.volume.colors.BasicColorScheme;
import edu.washington.biostr.sig.volume.colors.ColorLookupTable;
import edu.washington.biostr.sig.volume.colors.ColorLookupTables;
import edu.washington.biostr.sig.volume.colors.ColorScheme;

/**
 * This attempts to provide a simple interface for getting a complete section
 * out of an image at the specified location and viewing angle.
 * This is experimental and doesn't work 
 * perfectly.  In particular it does a poor job of making sure that everything
 * is included at oblique angles.  It also fails when 'up' needs to be custom.<br>
 * While less automatic, ImageInfo2 is a more reliable interface.
 * @author Eider Moore
 * @version experimental
 */
public class ImageInfo {

    private static Vector3f UP1 = new Vector3f(0, 0, 1);
    private static Vector3f UP1INV = new Vector3f(0, 0, -1);
    private static Vector3f UP2 = new Vector3f(0, 1, 0);
    private Point3f point;
    private Vector3f normal;
    private VolumeArray array;
    private float mmPerPixel;
    private ColorLookupTable table;
    private Vector3f down = new Vector3f();
    private Vector3f right = new Vector3f();
    private Point3f ul = new Point3f();

    /**
     * Create an uninitialized ImageInfo
     */
    protected ImageInfo() {
    }

    public VolumeArray getArray() {
        return array;
    }

    /**
     * Create and initialize this ImageInfo and generate the required fields to extract a
     * unique section.
     * @param array The volume to extract information from.
     * @param point A point on the target plane.
     * @param normal The normal to target plane.
     */
    public ImageInfo(VolumeArray array, Point3f point, Vector3f normal) {
        this();
        init(array, point, normal);
    }

    /**
     * Initialize this ImageInfo and generate the required fields to extract a
     * unique section.
     * @param array The volume to extract information from.
     * @param point A point on the target plane.
     * @param normal The normal to target plane.
     */
    protected void init(VolumeArray array, Point3f point, Vector3f normal) {
        if (normal == null || normal.length() == 0) {
            throw new IllegalArgumentException("normal cannot be null!");
        }
        if (point == null) {
            throw new IllegalArgumentException("point cannot be null!");
        }
        if (array == null) {
            throw new IllegalArgumentException("array cannot be null!");
        }
        this.normal = new Vector3f(normal);
        this.normal.normalize();
        this.point = new Point3f(point);
        this.array = array;

        Matrix4d m = array.getIndex2Space();

        // extract voxel size
        Point3f p0 = new Point3f(0, 0, 0);
        m.transform(p0);
        Point3f p1 = new Point3f(1, 1, 1);
        m.transform(p1);
        p1.sub(p0);
        mmPerPixel = Math.min(Math.abs(p1.x), Math.min(Math.abs(p1.y), Math.abs(p1.z)));

        // extract the lines.  Where this intersect the plane is the upper left corner
        p0 = new Point3f(0, 0, 0);
        p1 = new Point3f(array.getMaxX(), array.getMaxY(), array.getMaxZ());
        m.transform(p0);
        m.transform(p1);
        // determine if we are using UP1 or UP2
        boolean sameDirAsUp1 = UP1.equals(normal) || UP1INV.equals(normal);

        // extract left, right and in
        Vector3f up;
        if (sameDirAsUp1) {
            up = UP2;
        } else {
            up = UP1;
        }

        // generate right and down
        right.cross(up, normal);
        down.cross(right, normal);

        right.normalize();
        down.normalize();
        findUL(p0, p1);

        // find the length of down.  This is the distance from ul to the nearest plane
        // intersected by down.  This can be found by finding the smallest non-negative u.
        Point3f pd = new Point3f();
        Point3f pr = new Point3f();
        Vector3f scratch = new Vector3f();
        pd.add(ul, down);
        pr.add(ul, right);
        Vector3f faceN = new Vector3f();
        Point3f faceP = new Point3f();
        float bestUdown = Float.MIN_VALUE;
        float bestUright = Float.MIN_VALUE;
        float u;

        // left
        faceN.set(1, 0, 0);
        faceP.set(p0);
        u = computeU(faceN, faceP, ul, pd, scratch);
        if (u > 0 && u > bestUdown) {
            bestUdown = u;
        }
        u = computeU(faceN, faceP, ul, pr, scratch);
        if (u > 0 && u > bestUright) {
            bestUright = u;
        }
        // right
        faceP.set(p1);
        u = computeU(faceN, faceP, ul, pd, scratch);
        if (u > 0 && u > bestUdown) {
            bestUdown = u;
        }
        u = computeU(faceN, faceP, ul, pr, scratch);
        if (u > 0 && u > bestUright) {
            bestUright = u;
        }
        // up
        faceN.set(0, 1, 0);
        faceP.set(p0);
        u = computeU(faceN, faceP, ul, pd, scratch);
        if (u > 0 && u > bestUdown) {
            bestUdown = u;
        }
        u = computeU(faceN, faceP, ul, pr, scratch);
        if (u > 0 && u > bestUright) {
            bestUright = u;
        }
        // down
        faceP.set(p1);
        u = computeU(faceN, faceP, ul, pd, scratch);
        if (u > 0 && u > bestUdown) {
            bestUdown = u;
        }
        u = computeU(faceN, faceP, ul, pr, scratch);
        if (u > 0 && u > bestUright) {
            bestUright = u;
        }
        // front
        faceN.set(0, 0, 1);
        faceP.set(p0);
        u = computeU(faceN, faceP, ul, pd, scratch);
        if (u > 0 && u > bestUdown) {
            bestUdown = u;
        }
        u = computeU(faceN, faceP, ul, pr, scratch);
        if (u > 0 && u > bestUright) {
            bestUright = u;
        }
        // back
        faceP.set(p1);
        u = computeU(faceN, faceP, ul, pd, scratch);
        if (u > 0 && u > bestUdown) {
            bestUdown = u;
        }
        u = computeU(faceN, faceP, ul, pr, scratch);
        if (u > 0 && u > bestUright) {
            bestUright = u;
        }

        scratch.sub(p1, p0);
        float max = scratch.length();
        down.scale(Math.min(bestUdown, max));
        right.scale(Math.min(bestUdown, max));
    }

    /**
     * Compute the point on a line <code>u = N * (POINT-LINE1) / N * (LINE2 - LINE1)</code>
     * @param normal
     * @param point
     * @param line1
     * @param line2
     * @param scratch a buffer to which to write scratch data, the contents will be changed. 
     * @return N * (POINT-LINE1) / N * (LINE2 - LINE1)
     */
    private float computeU(Vector3f normal, Point3f point,
            Point3f line1, Point3f line2, Vector3f scratch) {
        // compute the point on the line
        //u = N * (POINT-LINE1) / N * (LINE2 - LINE1)
        scratch.sub(line2, line1);
        float bottom = normal.dot(scratch);
        // this line is perpendicular
        if (bottom == 0) {
            return Float.NaN;
        }

        scratch.sub(point, line1);
        float top = normal.dot(scratch);
        return top / bottom;
    }

    @SuppressWarnings("unused")
    private Point3f findPointOnPlane(Vector3f normal, Point3f point,
            Point3f line1, Point3f line2, Vector3f scratch, Point3f result) {
        float u = computeU(normal, point, line1, line2, scratch);
        if (Float.isNaN(u)) {
            return null;
        }
        return findPointOnPlane(line1, line2, result, u);
    }

    private Point3f findPointOnPlane(
            Point3f line1, Point3f line2, Point3f result, float u) {
        // P = P1 + u (P2 - P1)
        result.sub(line2, line1);

        result.scale(u);

        result.add(line1);
        return result;
    }

    private void findUL(Point3f p0, Point3f p1) {
        // find UL
        Vector3f temp1 = new Vector3f();
        temp1.normalize(right);
        ul.set(point);
        ul.sub(temp1);
        temp1.normalize(down);
        ul.sub(temp1);

        // now min(ul.dist(candidate) - point.dist(candidate)) is the upper left corner
        Point3f bestCandidate = new Point3f();
        double fitness = Double.MAX_VALUE;
        double test;
        Point3f currentCandidate = new Point3f();
        Point3f line1 = new Point3f();
        Point3f line2 = new Point3f();
        Vector3f scratch = new Vector3f();

        //1 (0 0 0) (1 0 0)
        if ((test = testCandidate(point, ul, currentCandidate, line1, line2,
                p0.x, p0.y, p0.z, X, p1.x - p0.x, scratch)) < fitness) {
            fitness = test;
            bestCandidate.set(currentCandidate);
        }
        //2 (0 0 0) (0 1 0)
        if ((test = testCandidate(point, ul, currentCandidate, line1, line2,
                p0.x, p0.y, p0.z, Y, p1.y - p0.y, scratch)) < fitness) {
            fitness = test;
            bestCandidate.set(currentCandidate);
        }
        //3 (0 0 0) (0 0 1)
        if ((test = testCandidate(point, ul, currentCandidate, line1, line2,
                p0.x, p0.y, p0.z, Z, p1.z - p0.z, scratch)) < fitness) {
            fitness = test;
            bestCandidate.set(currentCandidate);
        }
        //4 (1 0 0) (1 1 0)
        if ((test = testCandidate(point, ul, currentCandidate, line1, line2,
                p1.x, p0.y, p0.z, Y, p1.y - p0.y, scratch)) < fitness) {
            fitness = test;
            bestCandidate.set(currentCandidate);
        }
        //5 (1 0 0) (1 0 1)
        if ((test = testCandidate(point, ul, currentCandidate, line1, line2,
                p1.x, p0.y, p0.z, Z, p1.z - p0.z, scratch)) < fitness) {
            fitness = test;
            bestCandidate.set(currentCandidate);
        }
        //6 (0 1 0) (1 1 0)
        if ((test = testCandidate(point, ul, currentCandidate, line1, line2,
                p0.x, p1.y, p0.z, X, p1.x - p0.x, scratch)) < fitness) {
            fitness = test;
            bestCandidate.set(currentCandidate);
        }
        //7 (0 1 0) (0 1 1)
        if ((test = testCandidate(point, ul, currentCandidate, line1, line2,
                p0.x, p1.y, p0.z, Z, p1.z - p0.z, scratch)) < fitness) {
            fitness = test;
            bestCandidate.set(currentCandidate);
        }
        //8 (0 0 1) (1 0 1)
        if ((test = testCandidate(point, ul, currentCandidate, line1, line2,
                p0.x, p0.y, p1.z, X, p1.x - p0.x, scratch)) < fitness) {
            fitness = test;
            bestCandidate.set(currentCandidate);
        }
        //9 (0 0 1) (0 1 1)
        if ((test = testCandidate(point, ul, currentCandidate, line1, line2,
                p0.x, p0.y, p1.z, Y, p1.y - p0.y, scratch)) < fitness) {
            fitness = test;
            bestCandidate.set(currentCandidate);
        }
        //10 (0 1 1) (1 1 1)
        if ((test = testCandidate(point, ul, currentCandidate, line1, line2,
                p1.x, p1.y, p1.z, X, p0.x - p1.x, scratch)) < fitness) {
            fitness = test;
            bestCandidate.set(currentCandidate);
        }
        //11 (1 0 1) (1 1 1)
        if ((test = testCandidate(point, ul, currentCandidate, line1, line2,
                p1.x, p1.y, p1.z, Y, p0.y - p1.y, scratch)) < fitness) {
            fitness = test;
            bestCandidate.set(currentCandidate);
        }
        //12 (1 1 0) (1 1 1)
        if ((test = testCandidate(point, ul, currentCandidate, line1, line2,
                p1.x, p1.y, p1.z, Z, p0.z - p1.z, scratch)) < fitness) {
            fitness = test;
            bestCandidate.set(currentCandidate);
        }
        ul.set(bestCandidate);
    }
    private static final int X = 1;
    private static final int Y = 2;
    private static final int Z = 3;

    private double testCandidate(Point3f point, Point3f ul, Point3f currentCandidate,
            Point3f line1, Point3f line2, float x, float y, float z, int dir, float len, Vector3f scratch) {
        line1.set(x, y, z);
        line2.set(line1);
        switch (dir) {
            case X:
                line2.set(x + len, y, z);
                break;
            case Y:
                line2.set(x, y + len, z);
                break;
            case Z:
                line2.set(x, y, z + len);
                break;
        }
        float u = computeU(normal, point, line1, line2, scratch);
        if (Float.isNaN(u)) {
            return Double.MAX_VALUE;
        } else if (u < 0 || u > 1) {
            return Double.MAX_VALUE;
        }

        findPointOnPlane(line1, line2, currentCandidate, u);
        // now we have the point on the line and we can compute fitness
        double fitness = ul.distance(currentCandidate) - point.distance(currentCandidate);
        return fitness;
    }

    /**
     * Generate a BufferedImage.<br>
     * The image will be colored by the table values (by default white and cyan)
     * and will be sized in pixels based on the default mm/pixel and will support
     * transparency.
     * @param interpolation Linear or Nearest Neighbor
     * @return The image
     */
    public BufferedImage getImage(Interpolation interpolation) {
        return getImage(interpolation, true);
    }

    /**
     * Generate a BufferedImage.<br>
     * The image will be colored by the table values (by default white and cyan)
     * and will support transparency.
     * @param interpolation Linear or Nearest Neighbor
     * @param res the resolution in mm/pixel.  Use -1 for the natural resolution.
     * @return The image
     */
    public BufferedImage getImage(Interpolation interpolation, float res) {
        return getImage(interpolation, true, res);
    }

    /**
     * Generate a BufferedImage.<br>
     * The image will be colored by the table values (by default white and cyan)
     * and will be sized in pixels based on the default mm/pixel.
     * @param interpolation Linear or Nearest Neighbor
     * @param alpha support transparency.
     * @return The image
     */
    public BufferedImage getImage(Interpolation interpolation, boolean alpha) {
        return getImage(interpolation, -1);
    }

    /**
     * Generate a BufferedImage.<br>
     * The image will be colored by the table values (by default white and cyan)
     * and will be sizedin pixels based on res.
     * @param interpolation Linear or Nearest Neighbor
     * @param alpha support transparency.
     * @param res the resolution in mm/pixel.  Use -1 for the natural resolution.
     * @return The image
     */
    public BufferedImage getImage(Interpolation interpolation, boolean alpha, float res) {
        if (res <= 0) {
            res = mmPerPixel;
        }
        return array.getImage(ul, right, down, 0, 0,
                (int) Math.ceil(right.length() / res),
                (int) Math.ceil(down.length() / res), alpha, 1f,
                getTable(), null, interpolation);
    }

    /**
     * 
     * @return The table used for coloring.  Generate a default table if unset.
     */
    public ColorLookupTable getTable() {
        if (table == null) {
            setTable(new BasicColorScheme(Color.WHITE),
                    new BasicColorScheme(Color.cyan), 1);
        }
        return table;
    }

    /**
     * 
     * @param table The table used to assign colors to the image.
     */
    public void setTable(ColorLookupTable table) {
        this.table = table;
    }

    /**
     * Helper to generate a color table from color schemes.
     * @param positive The scheme for positive values
     * @param negative The scheme for negative values
     * @param alpha transparency (0 transparent, 1 opaque)
     */
    public void setTable(ColorScheme positive, ColorScheme negative, float alpha) {
        if (array.supportsLookupTable()) {
            table = ColorLookupTables.getTable(
                    positive, array.getImageMax(), 0, array.getImageMax(),
                    negative, array.getImageMin(), 0, array.getImageMin(),
                    alpha, array.getNaturalType().equals(DataType.TYPE_INT));
        } else {
            table = null;
        }
    }

    /**
     * Get the default mm/pixel that is derived from the source volume.
     * @return The default mm/pixel.
     */
    public float getMMPerPixel() {
        return mmPerPixel;
    }

    /**
     * Get the corners, this goes clockwise starting with the upper left hand
     * corner.  The results are placed in p1-p4.
     * @param p1
     * @param p2
     * @param p3
     * @param p4
     */
    public void getCorners(Point3f p1, Point3f p2, Point3f p3, Point3f p4) {
        p1.set(ul);
        p2.set(ul);
        p2.add(right);
        p3.set(ul);
        p3.add(right);
        p3.add(down);
        p4.set(ul);
        p4.add(down);

    }

    /**
     * Get a set of tuples that uniquely describe this image size and location.
     * Results are placed in ul, right and down.
     * @param ul Upper left hand corner
     * @param right the right vector in mm from ul
     * @param down the down vector in mm from ul
     */
    public void getTuples(Point3f ul, Vector3f right, Vector3f down) {
        ul.set(this.ul);
        right.set(this.right);
        down.set(this.down);
    }

    /**
     * Make an ImageInfo for a sagittal section.
     * @param array The source volume
     * @param mmFromCenter mm measured from coordinate (0,0,0)
     * @return the specified ImageInfo
     */
    public static ImageInfo makeSagittal(VolumeArray array, double mmFromCenter) {
        Point3f p;
        Vector3f n;

        p = new Point3f((float) mmFromCenter, 0, 0);
        n = new Vector3f(-1, 0, 0);

        ImageInfo ii = new ImageInfo(array, p, n);
        return ii;
    }

    /**
     * Make an ImageInfo for a coronal section.
     * @param array The source volume
     * @param mmFromCenter mm measured from coordinate (0,0,0)
     * @return the specified ImageInfo
     */
    public static ImageInfo makeCoronal(VolumeArray array, double mmFromCenter) {
        Point3f p;
        Vector3f n;

        p = new Point3f(0, (float) mmFromCenter, 0);
        n = new Vector3f(0, 1, 0);

        ImageInfo ii = new ImageInfo(array, p, n);
        return ii;
    }

    /**
     * Make an ImageInfo for a transverse section.
     * @param array The source volume
     * @param mmFromCenter mm measured from coordinate (0,0,0)
     * @return the specified ImageInfo
     */
    public static ImageInfo makeTransverse(VolumeArray array, double mmFromCenter) {
        Point3f p;
        Vector3f n;

        p = new Point3f(0, 0, (float) mmFromCenter);
        n = new Vector3f(0, 0, -1);

        ImageInfo ii = new ImageInfo(array, p, n);
        return ii;
    }
}
