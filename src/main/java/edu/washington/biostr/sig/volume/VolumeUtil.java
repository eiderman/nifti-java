package edu.washington.biostr.sig.volume;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;

/**
 * Provide some static utility methods that are useful for VolumeArrays 
 * and similiar code.
 * @author Eider Moore
 * @version 1.0
 */
public class VolumeUtil {

    /**
     * Do a linear interpolation of x, y and z with the four corners of the 
     * cube defined by v0-7.
     * @param x The location x (only the value after the decimal is used)
     * @param y The location y (only the value after the decimal is used)
     * @param z The location z (only the value after the decimal is used)
     * @param v0 The value at floor(x), floor(y), floor(z)
     * @param v1 The value at ceil(x), floor(y), floor(z)
     * @param v2 The value at floor(x), ceil(y), floor(z)
     * @param v3 The value at ceil(x), ceil(y), floor(z)
     * @param v4 The value at floor(x), floor(y), ceil(z)
     * @param v5 The value at ceil(x), floor(y), ceil(z)
     * @param v6 The value at floor(x), ceil(y), ceil(z)
     * @param v7 The value at ceil(x), ceil(y), ceil(z)
     * @return An interpolated value for (x,y,z)
     */
    public static double linearInterpolate(double x, double y, double z,
            double v0, double v1, double v2, double v3,
            double v4, double v5, double v6, double v7) {
        double xLoc = x - (int) x;
        double yLoc = y - (int) y;
        double zLoc = z - (int) z;

        return v0 * (1 - xLoc) * (1 - yLoc) * (1 - zLoc) +
                v1 * xLoc * (1 - yLoc) * (1 - zLoc) +
                v2 * (1 - xLoc) * (yLoc) * (1 - zLoc) +
                v3 * (xLoc) * (yLoc) * (1 - zLoc) +
                v4 * (1 - xLoc) * (1 - yLoc) * (zLoc) +
                v5 * (xLoc) * (1 - yLoc) * (zLoc) +
                v6 * (1 - xLoc) * (yLoc) * (zLoc) +
                v7 * (xLoc) * (yLoc) * (zLoc);
    }

    /**
     * Resample an image.
     * @param array The volume to resample
     * @param datatype one of VolumeArray's datatypes
     * @param corner1 one corner of a hexahedron (like a cube)
     * @param corner2 corner opposite corner1 of a hexahedron (like a cube)
     * @param voxelDim the dimension of one voxel in mm
     * @return a new VolumeArray as above
     */
    public static VolumeArray resample(VolumeArray array, DataType datatype,
            Point3f corner1, Point3f corner2, Tuple3f voxelDim, Interpolation interpolation) {
        float xMin = Math.min(corner1.x, corner2.x);
        float yMin = Math.min(corner1.y, corner2.y);
        float zMin = Math.min(corner1.z, corner2.z);
        float width = Math.abs(corner1.x - corner2.x);
        float height = Math.abs(corner1.y - corner2.y);
        float depth = Math.abs(corner1.z - corner2.z);
        int vWidth = (int) Math.ceil(width / voxelDim.x);
        int vHeight = (int) Math.ceil(height / voxelDim.y);
        int vDepth = (int) Math.ceil(depth / voxelDim.z);
        int vDuration = array.getMaxTime();
        int vi5 = array.getMaxI5();
        double[] transform = new double[]{
            voxelDim.x, 0, 0, xMin,
            0, voxelDim.y, 0, yMin,
            0, 0, voxelDim.z, zMin,
            0, 0, 0, 1
        };

        Matrix4d mat = new Matrix4d(transform);
        VolumeArray result = VolumeArrayFactory.createVolumeDataBuffer(
                mat, vWidth, vHeight, vDepth, vDuration, vi5, datatype);
        float x = xMin;
        float y = yMin;
        float z = zMin;
        Point3f p = new Point3f();
        for (int m = 0; m < vi5; m++) {
            for (int l = 0; l < vDuration; l++) {
                for (int k = 1; k <= vDepth; k++) {
                    for (int j = 1; j <= vHeight; j++) {
                        for (int i = 1; i <= vWidth; i++) {
                            p.set(x, y, z);
                            result.setData(i, j, k, l, m,
                                    array.getValueMM(p, l, m, interpolation));
                            x += voxelDim.x;
                        }
                        x = xMin;
                        y += voxelDim.y;
                    }
                    y = yMin;
                    z += voxelDim.z;
                }
                z = zMin;
            }
        }

        return result;
    }
}
