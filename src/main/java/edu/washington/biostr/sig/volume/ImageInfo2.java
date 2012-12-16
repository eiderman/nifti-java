package edu.washington.biostr.sig.volume;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import edu.washington.biostr.sig.volume.colors.BasicColorScheme;
import edu.washington.biostr.sig.volume.colors.ColorLookupTable;
import edu.washington.biostr.sig.volume.colors.ColorLookupTables;
import edu.washington.biostr.sig.volume.colors.ColorScheme;

/**
 * This attempts to provide a simple interface for getting a specified section
 * out of an image at a given center, viewing angle, up direction and size.<br>
 * It also provides methods to conveniently rotate it and change various views.
 * @author Eider Moore
 * @version beta
 */
public class ImageInfo2 {

    public enum Direction {

        RIGHT, UP, IN;
    }

    /**
     * Orientation specifies the desired convention to use.
     */
    public enum Orientation {

        /**
         * This is the radialogical convention.
         */
        STANDARD,
        /**
         * This is the neurological convention.
         */
        FLIPPED;
        
        static Orientation NEUROLOGICAL = FLIPPED;
        static Orientation RADIALOGICAL = STANDARD;
        
        /**
         * @return The other one (i.e. FLIPPED.other() returns STANDARD and vice versa)/
         */
		public Orientation other() {
			if (FLIPPED.equals(this)) {
				return STANDARD;
			} else {
				return FLIPPED;
			}
		}
    }
    Point3f point;
    Vector3f normal;
    private Vector3f up;
    private VolumeArray array;
    private float mmPerPixel;
    private ColorLookupTable table;
    private Vector3f down = new Vector3f();
    private Vector3f right = new Vector3f();
    private Point3f ul = new Point3f();
    float width, height;
    private Orientation orientation;

    /**
     * Create and initialize the given ImageInfo2.
     * @param array The source volume
     * @param point The center of the desired slice
     * @param normal The normal to the desired plane
     * @param up The direction of up, cannot be coincident with the normal.
     * @param width width in mm
     * @param height height in mm
     */
    public ImageInfo2(VolumeArray array, Point3f point, Vector3f normal, Vector3f up, float width, float height) {
        this(array, point, normal, up, width, height, Orientation.STANDARD);
    }

    /**
     * Create and initialize the given ImageInfo2.
     * @param array The source volume
     * @param point The center of the desired slice
     * @param normal The normal to the desired plane
     * @param up The direction of up, cannot be coincident with the normal.
     * @param width width in mm
     * @param height height in mm
     * @param standard Use the orientation that is considered standard by me (radialogical)
     */
    public ImageInfo2(VolumeArray array, Point3f point, Vector3f normal, Vector3f up, float width, float height,
    		boolean standard) {
        this(array, point, normal, up, width, height, standard ? Orientation.STANDARD : Orientation.FLIPPED);
    }

    /**
     * Create and initialize the given ImageInfo2.
     * @param array The source volume
     * @param point The center of the desired slice
     * @param normal The normal to the desired plane
     * @param up The direction of up, cannot be coincident with the normal.
     * @param width width in mm
     * @param height height in mm
     * @param orientation Choose the orientation.
     */
    public ImageInfo2(VolumeArray array, Point3f point, Vector3f normal, Vector3f up, float width, float height,
    		Orientation orientation) {
        init(array, point, normal, up, width, height, orientation);
    }

    /**
     * @param array The source volume
     * @param point The center of the desired slice
     * @param normal The normal to the desired plane
     * @param up The direction of up, cannot be coincident with the normal.
     * @param width width in mm
     * @param height height in mm
     * @param orientation
     */
    protected void init(VolumeArray array, Point3f point, Vector3f normal, Vector3f up, float width, float height,
    		Orientation orientation) {
    	if (orientation == null)
    		throw new IllegalArgumentException("Orientation cannot be null!");
        this.width = width;
        this.height = height;

        if (normal == null || normal.length() == 0) {
            throw new IllegalArgumentException("normal cannot be null!");
        }
        if (point == null) {
            throw new IllegalArgumentException("point cannot be null!");
        }
        if (array == null) {
            throw new IllegalArgumentException("array cannot be null!");
        }
        if (up == null) {
            throw new IllegalArgumentException("up cannot be null!");
        }
        this.normal = new Vector3f(normal);
        normal.normalize();
        this.point = new Point3f(point);
        this.up = new Vector3f(up);
        this.up.normalize();
        this.array = array;
        this.orientation = orientation;
        if (Math.abs(this.up.dot(this.normal)) == 1) {
            throw new IllegalArgumentException("up cannot be cooincident with the normal!");
        }

        setMMPerPixel();

        setRightDown();
    }

    /**
     * generate right and down
     */
    private void setRightDown() {
    	Vector3f normal = new Vector3f(this.normal);
    	Point3f point = new Point3f(this.point);
    	if (Orientation.FLIPPED.equals(orientation)) {
    		normal.negate();
    		// setters added later than vecmath 1.3
    		point.x = -point.x;
    	}
        // generate right and down
        right.cross(up, normal);
        down.cross(right, normal);

        right.normalize();
        down.normalize();

        // now set the length

        right.scale(width / 2);
        down.scale(height / 2);

        ul.set(point);
        ul.sub(right);
        ul.sub(down);
        down.scale(2);
        right.scale(2);
    }

    /**
     * Find the best mm/pixel to use by default.
     */
    private void setMMPerPixel() {
        mmPerPixel = getMMPerPixel(getArray());
    }
    
    public static float getMMPerPixel(VolumeArray array) {
        Matrix4d m = array.getIndex2Space();


        // extract voxel size
        Point3f p0 = new Point3f(0, 0, 0);
        m.transform(p0);
        Point3f p1 = new Point3f(1, 1, 1);
        m.transform(p1);
        p1.sub(p0);
        return Math.min(Math.abs(p1.x), Math.min(Math.abs(p1.y), Math.abs(p1.z)));
    	
    }

    /**
     * Generate a BufferedImage.<br>
     * The image will be colored by the table values (by default white and cyan)
     * and will be sizedin pixels based on the default mm/pixel and will support
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
     * and will be sizedin pixels based on the default mm/pixel.
     * @param interpolation Linear or Nearest Neighbor
     * @param alpha support transparency.
     * @return The image
     */
    public BufferedImage getImage(Interpolation interpolation, boolean alpha) {
        return array.getImage(ul, right, down, 0, 0,
                (int) Math.ceil(right.length() / mmPerPixel),
                (int) Math.ceil(down.length() / mmPerPixel), alpha, 1f,
                getTable(), null, interpolation);
    }

    /**
     * Generate a BufferedImage.<br>
     * The image will be colored by the table values (by default white and cyan)
     * and will be sizedin pixels based on the default mm/pixel.
     * @param interpolation Linear or Nearest Neighbor
     * @param img The image into which to put the data
     * @return The image
     */
    public BufferedImage getImage(Interpolation interpolation, 
            BufferedImage img) {
        return array.getImage(ul, right, down, 0, 0,
                img, 1f, getTable(), null, interpolation);
    }

    /**
     * Get the color table.
     * @return The color table or generate a default one if it has not been set.
     */
    public ColorLookupTable getTable() {
        if (table == null) {
            setTable(new BasicColorScheme(Color.WHITE),
                    new BasicColorScheme(Color.cyan), 1);
        }
        return table;
    }

    /**
     * Set the color table.
     * @param table The new color table.
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
     * Slide this ImageInfo2 in the given direction.
     * @param dir The direction (Up, Right, In).  Use negative mm for down, left and out
     * @param mm The distance to move this.
     * @return an ImageInfo2 that represents moving the center point of this one.
     */
    public ImageInfo2 move(Direction dir, float mm) {
        Point3f np = new Point3f();
        switch (dir) {
            case RIGHT:
                np.set(point.x + mm, point.y, point.z);
                break;
            case UP:
                np.set(point.x, point.y + mm, point.z);
                break;
            case IN:
                np.set(point.x, point.y, point.z + mm);
                break;
        }
        return new ImageInfo2(array, np, normal, up, width, height, getOrientation());
    }

    /**
     * Slide this ImageInfo2 so that it is the same except recentered.
     * @param center The new coordinate for the center. 
     * @return an ImageInfo2 that represents moving the center point of this one.
     */
    public ImageInfo2 moveTo(Point3f center) {
        return new ImageInfo2(array, center, normal, up, width, height, getOrientation());
    }

    /**
     * Rotate around one of the axes defining this plane.<br>
     * Thanks goes to Confuted at http://www.cprogramming.com/tutorial/3d/rotation.html for the matrix.
     * @param dir RIGHT or UP to tilt the plane, IN to rotate
     * @param radians  The number of radians to rotate
     * @return an ImageInfo2 that represents a rotation of this one.
     * @throws IllegalArgumentException if dir is IN
     */
    public ImageInfo2 rotate(Direction dir, double radians) {
        return rotate(dir, radians, right, true);
    }

    /**
     * Rotate around one of the axes defining this plane.<br>
     * Thanks goes to Confuted at http://www.cprogramming.com/tutorial/3d/rotation.html for the matrix.
     * @param dir RIGHT or UP to tilt the plane, IN to rotate
     * @param radians The number of radians to rotate
     * @param upPlane constrain up so that it is perpendicular to upPlane
     * @return an ImageInfo2 that represents a rotation of this one.
     * @throws IllegalArgumentException if dir is IN
     */
    public ImageInfo2 rotate(Direction dir, double radians, Vector3f upPlane) {
        return rotate(dir, radians, upPlane, false);
    }

    /**
     * Rotate around one of the axes defining this plane.<br>
     * Thanks goes to Confuted at http://www.cprogramming.com/tutorial/3d/rotation.html for the matrix.
     * @param dir RIGHT or UP to tilt the plane, IN to rotate
     * @param radians The number of radians to rotate
     * @param upPlane constrain up so that it is perpendicular to upPlane
     * @param robust try to rotate even if upPlane is illegal.
     * @return an ImageInfo that represents a rotation of this one.
     * @throws IllegalArgumentException if dir is IN
     */
    public ImageInfo2 rotate(Direction dir, double radians, Vector3f upPlane,
            boolean robust) {

        if (upPlane.dot(right) < 0) {
            upPlane.negate();
        }

        Vector3f axis = new Vector3f();
        double c = Math.cos(radians);
        double s = Math.sin(radians);
        double t = 1 - c;
        switch (dir) {
            case RIGHT:
                axis = new Vector3f(down);
                axis.normalize();
                break;
            case UP:
                axis = new Vector3f(right);
                axis.normalize();
                break;
            case IN:
                axis = normal;
                break;
        }
        Vector3f normal = new Vector3f(this.normal);
    	if (Orientation.FLIPPED.equals(orientation)) {
    		axis.negate();
    		normal.negate();
    	}
        double x = axis.x, y = axis.y, z = axis.z;
        // thank god for Confuted!
        Matrix4d tform = new Matrix4d(new double[]{
            t * x * x + c, t * x * y - s * z, t * x * z + s * y, 0,
            t * x * y + s * z, t * y * y + c, t * y * z - s * x, 0,
            t * x * z - s * y, t * y * z + s * x, t * z * z + c, 0,
            0, 0, 0, 1
        });

        // now change up
        tform.transform(normal);
        Vector3f up = new Vector3f(this.up);
        tform.transform(up);

        // ok, now lets put up in the correct plane
        if (Math.abs(normal.dot(upPlane)) > .99) {
            if (!robust) {
                throw new IllegalArgumentException("The normal cannot be parallel to the up plane.");
            } else {
                System.out.println("Normal || " + right + " " + normal);
                up.cross(normal, right);
                up.normalize();
            }
        } else {
            up.cross(normal, upPlane);
            up.normalize();
        }

    	if (Orientation.FLIPPED.equals(orientation)) {
    		normal.negate();
    	}
        ImageInfo2 ii = new ImageInfo2(array, point, normal, up, width, height, getOrientation());
        return ii;
    }

    /**
     * Make a square sagittal ImageInfo2.
     * @param array The source data.
     * @param dir Radialogical or Neurological convention.
     * @param mmFromCenter Distance from (0,0,0)
     * @param size The width and height in mm.
     * @return An ImageInfo2 for the given standard section
     */
    public static ImageInfo2 makeSagittal(VolumeArray array, Orientation dir, double mmFromCenter, float size) {
        return makeSagittal(array, dir, mmFromCenter, size, size);
    }

    /**
     * Make a sagittal ImageInfo2.
     * @param array The source data.
     * @param dir Radialogical or Neurological convention.
     * @param mmFromCenter Distance from (0,0,0)
     * @param width in mm
     * @param height in mm
     * @return An ImageInfo2 for the given standard section
     */
    public static ImageInfo2 makeSagittal(VolumeArray array, Orientation dir, double mmFromCenter, float width, float height) {
        return makeSagittal(array, dir, new Point3f((float) mmFromCenter, 0, 0), width, height);
    }

    /**
     * Make a sagittal ImageInfo2.
     * @param array The source data.
     * @param dir Radialogical or Neurological convention.
     * @param center center of this image.
     * @param width in mm
     * @param height in mm
     * @return An ImageInfo2 for the given standard section
     */
    public static ImageInfo2 makeSagittal(VolumeArray array, Orientation dir,
            Point3f center, float width, float height) {
        Point3f p;
        Vector3f n;
        Vector3f u;
//        switch (dir) {
//            case STANDARD:
//                p = new Point3f(center);
//                break;
//            case FLIPPED:
//                p = new Point3f(-center.x, center.y, center.z);
//                break;
//            default:
//                throw new IllegalArgumentException("Bad orientation");
//        }
        p = new Point3f(center);
        n = new Vector3f(1, 0, 0);
        u = new Vector3f(0, 0, 1);

        ImageInfo2 ii = new ImageInfo2(array, p, n, u, width, height, dir);
        return ii;
    }

    /**
     * Make a square coronal ImageInfo2.
     * @param array The source data.
     * @param dir Radialogical or Neurological convention.
     * @param mmFromCenter Distance from (0,0,0)
     * @param size The width and height in mm.
     * @return An ImageInfo2 for the given standard section
     */
    public static ImageInfo2 makeCoronal(VolumeArray array, Orientation dir, double mmFromCenter, float size) {
        return makeCoronal(array, dir, mmFromCenter, size, size);
    }

    /**
     * Make a  coronal ImageInfo2.
     * @param array The source data.
     * @param dir Radialogical or Neurological convention.
     * @param mmFromCenter Distance from (0,0,0)
     * @param width width in mm
     * @param height height in mm
     * @return An ImageInfo2 for the given standard section
     */
    public static ImageInfo2 makeCoronal(VolumeArray array, Orientation dir, double mmFromCenter, float width, float height) {
        return makeCoronal(array, dir, new Point3f(0, (float) mmFromCenter, 0),
                width, height);
    }

    /**
     * Make a  coronal ImageInfo2.
     * @param array The source data.
     * @param dir Radialogical or Neurological convention.
     * @param center Middle of the image
     * @param width width in mm
     * @param height height in mm
     * @return An ImageInfo2 for the given standard section
     */
    public static ImageInfo2 makeCoronal(VolumeArray array, Orientation dir,
            Point3f center, float width, float height) {
        Point3f p;
        Vector3f n;
        Vector3f u;

        p = center;
//        switch (dir) {
//            case STANDARD:
//                n = new Vector3f(0, -1, 0);
//                break;
//            case FLIPPED:
//                n = new Vector3f(0, 1, 0);
//                break;
//            default:
//                throw new IllegalArgumentException("Bad orientation");
//        }
        n = new Vector3f(0, -1, 0);
        u = new Vector3f(0, 0, 1);

        ImageInfo2 ii = new ImageInfo2(array, p, n, u, width, height, dir);
        return ii;
    }

    /**
     * Make a square transverse ImageInfo2.
     * @param array The source data.
     * @param dir Radialogical or Neurological convention.
     * @param mmFromCenter Distance from (0,0,0)
     * @param size The width and height in mm.
     * @return An ImageInfo2 for the given standard section
     */
    public static ImageInfo2 makeTransverse(VolumeArray array, Orientation dir, double mmFromCenter, float size) {
        return makeTransverse(array, dir, mmFromCenter, size, size);
    }

    /**
     * Make a  transverse ImageInfo2.
     * @param array The source data.
     * @param dir Radialogical or Neurological convention.
     * @param mmFromCenter Distance from (0,0,0)
     * @param width The width in mm
     * @param height the height in mm.
     * @return An ImageInfo2 for the given standard section
     */
    public static ImageInfo2 makeTransverse(VolumeArray array, Orientation dir, double mmFromCenter, float width, float height) {
        return makeTransverse(array, dir,
                new Point3f(0, 0, (float) mmFromCenter), width, height);
    }

    /**
     * Make a  transverse ImageInfo2.
     * @param array The source data.
     * @param dir Radialogical or Neurological convention.
     * @param center The center of this image.
     * @param width The width in mm
     * @param height the height in mm.
     * @return An ImageInfo2 for the given standard section
     */
    public static ImageInfo2 makeTransverse(VolumeArray array, Orientation dir,
            Point3f center, float width, float height) {
        Point3f p;
        Vector3f n;
        Vector3f u;

        p = center;
//        switch (dir) {
//            case STANDARD:
//                n = new Vector3f(0, 0, 1);
//                break;
//            case FLIPPED:
//                n = new Vector3f(0, 0, -1);
//                break;
//            default:
//                throw new IllegalArgumentException("Bad orientation");
//        }
        n = new Vector3f(0, 0, 1);
        u = new Vector3f(0, 1, 0);

        ImageInfo2 ii = new ImageInfo2(array, p, n, u, width, height, dir);
        return ii;
    }

    /**
     * Get the center point.
     * @return The center
     */
    public Point3f getPoint() {
        return point;
    }

    /**
     * The normal to the desired plane.
     * @return The normal to the desired plane.
     */
    public Vector3f getNormal() {
        return normal;
    }

    /**
     * The up direction.
     * @return The up direction.
     */
    public Vector3f getUp() {
        return up;
    }

    /**
     *  The source volume.
     * @return The source volume.
     */
    public VolumeArray getArray() {
        return array;
    }

    /**
     * The default mm/pixel used.
     * @return The default mm/pixel used.
     */
    public float getMmPerPixel() {
        return mmPerPixel;
    }

    /**
     * Get the width in mm.
     * @return Get the width in mm.
     */
    public float getWidth() {
        return width;
    }

    /**
     * Get the height in mm.
     * @return the height in mm.
     */
    public float getHeight() {
        return height;
    }

    /**
     * Generate a BufferedImage.<br>
     * The image will be colored by the table values (by default white and cyan)
     * and will be sized in pixels based on the mm/pixel in res.
     * @param interpolation Linear or Nearest Neighbor
     * @param res the resolution in mm/pixel or -1 for the default.
     * @return The image
     */
    public BufferedImage getImage(Interpolation interpolation, float res) {
        return getImage(interpolation, true, res);
    }

    /**
     * Generate a BufferedImage.<br>
     * The image will be colored by the table values (by default white and cyan)
     * and will be sized in pixels based on the mm/pixel in res.
     * @param interpolation Linear or Nearest Neighbor
     * @param alpha support transparency
     * @param res the resolution in mm/pixel or -1 for the default.
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

    @Override
    public String toString() {
        return "n" + normal + ":u" + up + ":c" + point + " tuples: ul" + ul + ",r" + right + ",d" + down;
    }

    /**
     * Right is the length and direction in mm from the upper left corner to 
     * the upper right corner.
     * @return The right vector.
     */
    public Vector3f getRight() {
        return right;
    }

    /**
     * Generate a transform based on the scale dictated by the given resolution.
     * @param res The desired resolution in mm/pixel or -1 for default.
     * @return A transform that converts voxel indices to mm coordinates.
     */
    public AffineTransform getToMM(double res) {
        if (res <= 0) {
            res = getMmPerPixel();
        }
        // set the transform.
        AffineTransform at = new AffineTransform();

        Vector3f r = new Vector3f(right), d = new Vector3f(down);
        r.normalize();
        double x1 = r.dot(new Vector3f(ul));
        d.normalize();
        double y1 = d.dot(new Vector3f(ul));

        double scale = 1 / res;
        at.scale(scale, scale);
        at.translate(-x1, -y1);

        return at;
    }

    /**
     * Create a copy of this ImageInfo2 except pointing at
     * array instead of the current one.
     * @param array
     * @return A copy with array as the source.
     */
    public ImageInfo2 copy(VolumeArray array) {
        return new ImageInfo2(array, point, normal, up, width, height, orientation);
    }
    
    public Orientation getOrientation() {
		return orientation;
	}
}
