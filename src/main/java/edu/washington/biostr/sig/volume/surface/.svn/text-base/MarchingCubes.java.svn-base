package edu.washington.biostr.sig.volume.surface;

import javax.vecmath.Point3f;

/**
 * March through the image and perform the marching cubes algorithm.  This relies
 * on StaticMCTable to do the dirty work of picking out the up to 5 faces per
 * cube.
 * @author Eider Moore
 * @version 1.0
 */
public class MarchingCubes extends MarchingIsosurface {

    private int maxNumFaces;

    public MarchingCubes() {
        maxNumFaces = 4500000;
    }

    /**
     * Generate an int where the first 8 bits are set based on whether v0-v7 
     * are inside the thresholds or outside.
     * @param v0 The value at v0
     * @param v1 The value at v1
     * @param v2 The value at v2
     * @param v3 The value at v3
     * @param v4 The value at v4
     * @param v5 The value at v5
     * @param v6 The value at v6
     * @param v7 The value at v7
     * @param threshLow The low threshold
     * @param threshHigh The high threshold
     * @return An int where the first 8 bits are set based on whether v0-v7 are in or out.
     */
    public static final int getCube(double v0, double v1, double v2, double v3,
            double v4, double v5, double v6, double v7,
            double threshLow, double threshHigh) {
        int cube = 0;
        if (v0 > threshLow && v0 < threshHigh) {
            cube += 1;
        }
        if (v1 > threshLow && v1 < threshHigh) {
            cube += 2;
        }
        if (v2 > threshLow && v2 < threshHigh) {
            cube += 4;
        }
        if (v3 > threshLow && v3 < threshHigh) {
            cube += 8;
        }
        if (v4 > threshLow && v4 < threshHigh) {
            cube += 16;
        }
        if (v5 > threshLow && v5 < threshHigh) {
            cube += 32;
        }
        if (v6 > threshLow && v6 < threshHigh) {
            cube += 64;
        }
        if (v7 > threshLow && v7 < threshHigh) {
            cube += 128;
        }
        return cube;
    }

    @Override
    public void doCube(Point3f p0, double v0, int id0, Point3f p1,
            double v1, int id1, Point3f p2, double v2,
            int id2, Point3f p3, double v3, int id3,
            Point3f p4, double v4, int id4, Point3f p5,
            double v5, int id5, Point3f p6, double v6, int id6, Point3f p7, double v7, int id7, double threshLow, double threshHigh) {
        int cube = getCube(v0, v1, v2, v3, v4, v5, v6, v7, threshLow, threshHigh);
        char[] curfaces = StaticMCTable.triTableA[cube];
        Point3f[] tri = new Point3f[3];
        int[] ids = new int[3];
        for (int i = 0; i < curfaces.length; i += 3) {
            for (int j = 0; j < 3; j++) {
                switch (curfaces[i + j]) {
                    case 0:
                        tri[j] = interp(p0, v0, p1, v1, threshLow);
                        ids[j] = getId(id0, id1);
                        break;
                    case 1:
                        tri[j] = interp(p1, v1, p2, v2, threshLow);
                        ids[j] = getId(id1, id2);
                        break;
                    case 2:
                        tri[j] = interp(p3, v3, p2, v2, threshLow);
                        ids[j] = getId(id3, id2);
                        break;
                    case 3:
                        tri[j] = interp(p0, v0, p3, v3, threshLow);
                        ids[j] = getId(id0, id3);
                        break;
                    case 4:
                        tri[j] = interp(p4, v4, p5, v5, threshLow);
                        ids[j] = getId(id4, id5);
                        break;
                    case 5:
                        tri[j] = interp(p5, v5, p6, v6, threshLow);
                        ids[j] = getId(id5, id6);
                        break;
                    case 6:
                        tri[j] = interp(p7, v7, p6, v6, threshLow);
                        ids[j] = getId(id7, id6);
                        break;
                    case 7:
                        tri[j] = interp(p4, v4, p7, v7, threshLow);
                        ids[j] = getId(id4, id7);
                        break;
                    case 8:
                        tri[j] = interp(p0, v0, p4, v4, threshLow);
                        ids[j] = getId(id0, id4);
                        break;
                    case 9:
                        tri[j] = interp(p1, v1, p5, v5, threshLow);
                        ids[j] = getId(id1, id5);
                        break;
                    case 10:
                        tri[j] = interp(p3, v3, p7, v7, threshLow);
                        ids[j] = getId(id3, id7);
                        break;
                    case 11:
                        tri[j] = interp(p2, v2, p6, v6, threshLow);
                        ids[j] = getId(id2, id6);
                        break;
                    default:
                        throw new IllegalArgumentException("not a valid face " + curfaces[i]);
                }
            }
            addTriangle(tri[0], tri[1], tri[2], ids[0], ids[1], ids[2]);
        }
    }
}
