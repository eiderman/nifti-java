package edu.washington.biostr.sig.volume.surface;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import edu.washington.biostr.sig.volume.VolumeArray;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3i;

/**
 * MarchingIsosurface is a parent class for algorithms that extract an
 * isosurface by analyzing a volume cube by cube. This includes algorithms like
 * Marching Cubes and Marching Tetrahedron. This algorithm is not thread safe.
 * 
 * @author Eider Moore
 * @version 1
 */
public abstract class MarchingIsosurface {

	private Int2IntMap points;
	// private List<Vector3f> normals;
	private IntList faces;
	private List<Point3f> pointsf;
	private Vector3f scratchv1 = new Vector3f();
	private Vector3f scratchv2 = new Vector3f();
	private Vector3f scratchvn = new Vector3f();
	private Vector3f scratchv3 = new Vector3f();
	private Vector3f scratchv0 = new Vector3f();

	// smoothing factor.
	private final float LAMBDA = 0f;
	private int dir1;
	private int dir2;
	private int dir3;
	private int downscale;

	/**
	 * March through the given region of the array array and generate a surface.
	 * 
	 * @param array
	 *            The source
	 * @param isoLow
	 *            The bottom threshold (generally 0)
	 * @param isoHigh
	 *            The top threshold
	 * @param size
	 *            A guess at result size, an accurate guess gives the best
	 *            performance/space
	 * @param start
	 *            the smallest voxel to include (smallest x, y, z)
	 * @param end
	 *            the largest voxel to include (largest x, y, z)
	 * @return A Surface object.
	 */
	public Surface boundedMarch(VolumeArray array, int t, int i5,
			double isoLow, double isoHigh, int size, Point3i start, Point3i end) {
		return boundedMarch(array, t, i5, isoLow, isoHigh, size, start, end, 1);
	}

	/**
	 * March through the given region of the array array and generate a surface.
	 * 
	 * @param array
	 *            The source
	 * @param isoLow
	 *            The bottom threshold (generally 0)
	 * @param isoHigh
	 *            The top threshold
	 * @param size
	 *            A guess at result size, an accurate guess gives the best
	 *            performance/space
	 * @param start
	 *            the smallest voxel to include (smallest x, y, z)
	 * @param end
	 *            the largest voxel to include (largest x, y, z)
	 * @param downscale
	 *            the amount to down scale by, use 1 for full resolution, 2 for
	 *            1/8, and so on.
	 * @return A Surface object.
	 */
	public Surface boundedMarch(VolumeArray array, int t, int i5,
			double isoLow, double isoHigh, int size, Point3i start,
			Point3i end, int downscale) {
		this.downscale = downscale;
		dir1 = getIndex(array, 1, 0, 0) * downscale;
		dir2 = getIndex(array, 0, 1, 0) * downscale;
		dir3 = getIndex(array, 0, 0, 1) * downscale;
		points = new Int2IntOpenHashMap(size * 9 / 2);
		points.defaultReturnValue(-1);
		faces = new IntArrayList(size);
		pointsf = new ArrayList<Point3f>(size);
		// normals = new ArrayList<Vector3f>(size);

		Point3f p0 = new Point3f();
		Point3f p1 = new Point3f();
		Point3f p2 = new Point3f();
		Point3f p3 = new Point3f();
		Point3f p4 = new Point3f();
		Point3f p5 = new Point3f();
		Point3f p6 = new Point3f();
		Point3f p7 = new Point3f();
		double v0;
		double v1;
		double v2;
		double v3;
		double v4;
		double v5;
		double v6;
		double v7;
		int id0, id1, id2, id3, id4, id5, id6, id7;
		Matrix4d index2space = array.getIndex2Space();

		for (int k = start.z; k <= end.z; k += downscale) {
			// if we are interrupted just give up.
			if (Thread.currentThread().isInterrupted()) {
				return null;
			}
			for (int j = start.y; j <= end.y; j += downscale) {
				for (int i = start.x; i <= end.x; i += downscale) {
					v7 = array.getDouble(i, j, k, t, i5);
					p7.set(i, j, k);
					index2space.transform(p7);
					id7 = getIndex(array, i, j, k);
					// right
					i += downscale;
					v6 = array.getDouble(i, j, k, t, i5);
					p6.set(i, j, k);
					index2space.transform(p6);
					id6 = getIndex(array, i, j, k);
					// down
					j += downscale;
					v2 = array.getDouble(i, j, k, t, i5);
					p2.set(i, j, k);
					index2space.transform(p2);
					id2 = getIndex(array, i, j, k);
					// left
					i -= downscale;
					v3 = array.getDouble(i, j, k, t, i5);
					p3.set(i, j, k);
					index2space.transform(p3);
					id3 = getIndex(array, i, j, k);
					// in
					k += downscale;
					v0 = array.getDouble(i, j, k, t, i5);
					p0.set(i, j, k);
					index2space.transform(p0);
					id0 = getIndex(array, i, j, k);
					// right
					i += downscale;
					v1 = array.getDouble(i, j, k, t, i5);
					p1.set(i, j, k);
					index2space.transform(p1);
					id1 = getIndex(array, i, j, k);
					// up
					j -= downscale;
					v5 = array.getDouble(i, j, k, t, i5);
					p5.set(i, j, k);
					index2space.transform(p5);
					id5 = getIndex(array, i, j, k);
					// left
					i -= downscale;
					v4 = array.getDouble(i, j, k, t, i5);
					p4.set(i, j, k);
					index2space.transform(p4);
					id4 = getIndex(array, i, j, k);

					// bring it back
					k -= downscale;

					doCube(p0, v0, id0, p1, v1, id1, p2, v2, id2, p3, v3, id3,
							p4, v4, id4, p5, v5, id5, p6, v6, id6, p7, v7, id7,
							isoLow, isoHigh);
				}
			}
		}
		// make sure that we minimize garbage

		points = null;

		float[] pointsArr = new float[pointsf.size() * 3];
		int i = 0;
		for (Point3f v : pointsf) {
			pointsArr[i++] = v.x;
			pointsArr[i++] = v.y;
			pointsArr[i++] = v.z;
		}
		pointsf = null;

		int[] facesArr = faces.toIntArray();
		faces = null;

		// float[] normalsArr = new float[normals.size() * 3];
		// i = 0;
		// for (Vector3f v : normals)
		// {
		// v.normalize();
		// normalsArr[i++] = v.x;
		// normalsArr[i++] = v.y;
		// normalsArr[i++] = v.z;
		// }
		// assert i == normalsArr.length;
		return new Surface(facesArr, pointsArr, null);
	}

	private int getIndex(VolumeArray arr, int x, int y, int z) {
		return (z * (arr.getMaxY() + 1) + y) * (arr.getMaxX() + 1) + x;
	}

	protected int getId(int id0, int id1) {
		int min, max;
		if (id0 < id1) {
			min = id0;
			max = id1;
		} else {
			min = id1;
			max = id0;
		}
		int dif = max - min;
		if (dif == dir1) {
			return min;
		} else if (dif == dir2) {
			return 1 << 30 | min;
		} else if (dif == dir3) {
			return 1 << 31 | min;
		} else {
			throw new IllegalArgumentException(
					"id1 and id0 don't specify an edge.");
		}
	}

	/**
	 * Add the triangle and compute the normal (smoothed across shared
	 * vertices). Also perform some slight laplacian smoothing
	 * 
	 * @param v0
	 * @param v1
	 * @param v2
	 * @param id0
	 * @param id1
	 * @param id2
	 */
	protected void addTriangle(Point3f v0, Point3f v1, Point3f v2, int id0,
			int id1, int id2) {
		// compute the normal vector but don't normalize yet as the vector's
		// length is porportional to the triangle's size.
		// scratchv1.sub(v2, v1);
		// scratchv2.sub(v0, v1);
		// scratchvn.cross(scratchv1, scratchv2);
		// // scratchvn.scale(1 / scratchvn.lengthSquared());
		// scratchvn.normalize();

		scratchv0.set(0, 0, 0);
		scratchv1.set(0, 0, 0);
		scratchv2.set(0, 0, 0);
		smooth(v0, v1, scratchv0, scratchv1, scratchv3);
		smooth(v0, v2, scratchv0, scratchv2, scratchv3);
		smooth(v1, v2, scratchv1, scratchv2, scratchv3);
		int index = addPoint(v0, id0, scratchvn);
		// add the smooth amount to the stored point
		pointsf.get(index).add(scratchv0);

		index = addPoint(v1, id1, scratchvn);
		// add the smooth amount to the stored point
		pointsf.get(index).add(scratchv1);

		index = addPoint(v2, id2, scratchvn);
		// add the smooth amount to the stored point
		pointsf.get(index).add(scratchv2);
	}

	/**
	 * Generate the amount by which to move pi based on its connectivity with
	 * pj. the amount is added to result.
	 * 
	 * @param pi
	 * @param pj
	 * @param resulti
	 * @param resultj
	 * @param scratch
	 *            A scratch vector.
	 */
	private void smooth(Point3f pi, Point3f pj, Vector3f resulti,
			Vector3f resultj, Vector3f scratch) {
		scratch.sub(pj, pi);
		scratch.scale(LAMBDA);
		resulti.add(scratch);
		resultj.sub(scratch);
	}

	private int addPoint(Point3f p, int id, Vector3f n) {
		int index = points.get(id);
		// Vector3f normal;
		if (index < 0) {
			index = points.size();
			points.put(id, index);
			pointsf.add(p);
			// normal = new Vector3f();
			// normals.add(normal);
		} else {
			// normal = normals.get(index);
		}
		// add n to it. The magnitude should reflect the triangle's size.
		// normal.add(n);
		faces.add(index);
		return index;
	}

	/**
	 * Interpolate to find a vertex and add them to points.<br>
	 * Currently this uses linear interpolation using:<br>
	 * <code>P = P0 + (isovalue - V0) (P1 - P0) / (V1 - V0)</code>
	 * 
	 * @param p0
	 *            The first coordinate
	 * @param v0
	 *            The value at p0
	 * @param p1
	 *            The second coordinate
	 * @param v1
	 *            The value at p1
	 * @param iso
	 *            The isosurface value.
	 * @return The point interpreted based on the values and isovalue between p0
	 *         ande p1.
	 */
	protected Point3f interp(Point3f p0, double v0, Point3f p1, double v1,
			double iso) {
		Point3f p = new Point3f();
		p.add(p0, p1);
		p.scale(.5f);
		// float mult = (float) ((iso - v0) / (v1 - v0));
		// if (mult > .99)
		// mult = .99f;
		// else if (mult < .01)
		// mult = .01f;
		// Point3f p = new Point3f(
		// p0.x + mult * (p1.x - p0.x),
		// p0.y + mult * (p1.y - p0.y),
		// p0.z + mult * (p1.z - p0.z));

		return p;
	}

	public abstract void doCube(Point3f p0, double v0, int id0, Point3f p1,
			double v1, int id1, Point3f p2, double v2, int id2, Point3f p3,
			double v3, int id3, Point3f p4, double v4, int id4, Point3f p5,
			double v5, int id5, Point3f p6, double v6, int id6, Point3f p7,
			double v7, int id7, double isoLow, double isoHigh);

	/**
	 * Extract the surface.
	 * 
	 * @param data
	 *            The source data.
	 * @param isovalue
	 *            The upper threshold.
	 * @return A generated surface
	 */
	public Surface extractSurface(VolumeArray data, double isovalue) {
		return extractBoundedSurface(data, isovalue, new Point3i(0, 0, 0),
				new Point3i(data.getMaxX(), data.getMaxY(), data.getMaxZ()));
	}

	/**
	 * Extract the surface.
	 * 
	 * @param data
	 *            The source data.
	 * @param isovalue
	 *            The upper threshold.
	 * @param downscale
	 *            the amount to down scale by, use 1 for full resolution, 2 for
	 *            1/8, and so on.
	 * @return A generated surface
	 */
	public Surface extractSurface(VolumeArray data, double isovalue,
			int downscale) {
		return extractBoundedSurface(data, isovalue, new Point3i(0, 0, 0),
				new Point3i(data.getMaxX(), data.getMaxY(), data.getMaxZ()),
				downscale);
	}

	/**
	 * Extract the surface from a region of the data
	 * 
	 * @param data
	 *            The source data.
	 * @param isovalue
	 *            The upper threshold.
	 * @param start
	 *            the smallest voxel to include (smallest x, y, z)
	 * @param end
	 *            the largest voxel to include (largest x, y, z)
	 * @return A generated surface
	 */
	public Surface extractBoundedSurface(VolumeArray data, double isovalue,
			Point3i start, Point3i end) {
		return extractBoundedSurface(data, isovalue, start, end, 1);
	}

	/**
	 * Extract the surface from a region of the data
	 * 
	 * @param data
	 *            The source data.
	 * @param isovalue
	 *            The upper threshold.
	 * @param start
	 *            the smallest voxel to include (smallest x, y, z)
	 * @param end
	 *            the largest voxel to include (largest x, y, z)
	 * @param downscale
	 *            the amount to down scale by, use 1 for full resolution, 2 for
	 *            1/8, and so on.
	 * @return A generated surface
	 */
	public Surface extractBoundedSurface(VolumeArray data, double isovalue,
			Point3i start, Point3i end, int downscale) {
		return boundedMarch(data, 0, 0, isovalue, Double.MAX_VALUE,
				(end.x - start.x) * (end.y - start.y) * (end.z - start.z) / 14,
				start, end, downscale);
	}
}
