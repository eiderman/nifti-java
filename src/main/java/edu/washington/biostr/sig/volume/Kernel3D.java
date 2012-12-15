package edu.washington.biostr.sig.volume;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

/**
 * This class handles 3D kernel application to 3D data sets.  This code is based on the Kernel3D
 * created by Eider Moore, and updated to be compatable with the current BrainJ3D codebase.
 * @author Peter Lincoln
 * @version 1
 */
public class Kernel3D {

    private float[] kernel;
    private int width;
    private int height;
    private int depth;

    /**
     * Create a new 3D filter kernel using the specified 1D array as the kernel. 
     * @param width width of the filter kernel
     * @param height height of the filter kernel
     * @param depth depth of the filter kernel
     * @param kernel the filter kernal data
     */
    public Kernel3D(int width, int height, int depth, float[] kernel) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.kernel = kernel;

        if ((width % 2 != 1) || (height % 2 != 1) || (depth % 2 != 1)) {
            throw new IllegalArgumentException("The width, depth and height must be odd");
        }

        if (width * height * depth != kernel.length) {
            throw new IllegalArgumentException(
                    "The size of the filter kernel data is not equal to the size specified by height, width, and depth");
        }
    }

    private static double calcGaussian(double x, double y, double z, double stdDev) {
        double val = (Math.exp(-(x * x + y * y + z * z) / (2 * stdDev)) / Math.sqrt(2 * Math.PI * stdDev));
        return val;
    }

    /**
     * Creates a 3D Gaussian filter kernel of the specified size in voxels using the specified standard deviation
     * @param width
     * @param height
     * @param depth
     * @param stdDev
     * @return a 3D Gaussian filter kernel
     */
    public static Kernel3D createGaussian(int width, int height, int depth, float stdDev) {
        if ((width % 2 != 1) || (height % 2 != 1) || (depth % 2 != 1)) {
            throw new IllegalArgumentException("The width, depth and height must be odd");
        }

        float[] kernel = new float[width * height * depth];
        int index = 0;
        double sum = 0.0f;

        for (int k = -(depth / 2); k <= depth / 2; k++) {
            for (int j = -(height / 2); j <= height / 2; j++) {
                for (int i = -(width / 2); i <= width / 2; i++) {
                    double val = calcGaussian(i, j, k, stdDev);
                    kernel[index] = (float) val;
                    sum += val;
                    index++;
                }
            }
        }

        if (sum != 0.0) {
            // Normalize
            for (int i = 0; i < kernel.length; i++) {
                kernel[i] /= sum;
            }
        }
        return new Kernel3D(width, height, depth, kernel);
    }

    /**
     * Creates a 3D Gaussian filter kernel of the specified size in units of the underling VolumeArray using the
     * specified standard deviation.  For use with VolumeArray's with non-cubic voxels.
     * @param width
     * @param height
     * @param depth
     * @param stdDev
     * @param scale a 3D scale factor for specifying the size of a voxel
     * @return a 3D Gaussian filter kernel
     */
    public static Kernel3D createGaussian(float width, float height, float depth, float stdDev, Vector3d scale) {
        // TODO: verify these formulas

        final float scaleW = (float) scale.x;
        final float scaleH = (float) scale.y;
        final float scaleD = (float) scale.z;

        final int iWidth = Math.max(2 * (int) Math.ceil(width / (2.0 * scaleW)) - 1, 3);
        final int iHeight = Math.max(2 * (int) Math.ceil(height / (2.0 * scaleH)) - 1, 3);
        final int iDepth = Math.max(2 * (int) Math.ceil(depth / (2.0 * scaleD)) - 1, 3);

        float[] kernel = new float[iWidth * iHeight * iDepth];
        int index = 0;
        double sum = 0.0;

        float d = -(depth / 2.0f) + (scaleD / 2.0f);
        for (int k = -(iDepth / 2); k <= iDepth / 2; k++) {
            float h = -(height / 2.0f) + (scaleH / 2.0f);
            for (int j = -(iHeight / 2); j <= iHeight / 2; j++) {
                float w = -(width / 2.0f) + (scaleW / 2.0f);
                for (int i = -(iWidth / 2); i <= iWidth / 2; i++) {
                    double val = calcGaussian(d, h, w, stdDev);
                    kernel[index] = (float) val;

                    sum += val;
                    index++;
                    w += scaleW;
                }
                h += scaleH;
            }
            d += scaleD;
        }

        if (sum != 0.0) {
            // Normalize
            for (int i = 0; i < kernel.length; i++) {
                kernel[i] /= sum;
            }
        }

        return new Kernel3D(iWidth, iHeight, iDepth, kernel);
    }

    /**
     * Returns true if a Kernel3D can be applied to a particular volume array.
     * @param volArray
     * @return
     */
    public static boolean canApply(VolumeArray volArray) {
        return volArray instanceof IndexedVolumeArray;
    }

    /**
     * Prints this kernel in a multiline format to System.out.
     */
    public String getDebugString() {
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < depth; k++) {
            sb.append('[').append('\n');
            for (int j = 0; j < height; j++) {
                sb.append(" [ ");
                for (int i = 0; i < width; i++) {
                    sb.append(getKernelValue(i, j, k));
                    sb.append(' ');
                }
                sb.append("]").append('\n');
            }
            sb.append(']').append('\n');
        }
        return sb.toString();
    }

    /**
     * Returns a single line format of this kernel.
     */
    public String toString() {
        StringBuilder builder = new StringBuilder(depth * height * width * 10);

        builder.append('[');
        for (int k = 0; k < depth; k++) {
            builder.append('[');
            for (int j = 0; j < height; j++) {
                builder.append("[");
                for (int i = 0; i < width; i++) {
                    builder.append(getKernelValue(i, j, k));
                    builder.append(' ');
                }
                builder.append(']');
            }
            builder.append(']');
        }
        builder.append(']');

        return builder.toString();
    }

    public float[] getKernel() {
        return kernel;
    }

    /**
     * Gets the value of the kernel at the specified index
     * @return the value of the kernel at the specified index
     */
    public float getKernelValue(int x, int y, int z) {
        return kernel[x + height * (y + depth * z)];
    }

    public int getDepth() {
        return depth;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

// private int[] makeOffsets(IndexedVolumeArray volArray)
// {
// int index = 0;
// int maxX = volArray.getMaxX();
// int maxY = volArray.getMaxY();
////int maxZ = volArray.getMaxZ();
////int maxT = volArray.getMaxTime();
// int[] offsets = new int[kernel.length];

// for (int k = - (depth / 2); k <= depth / 2; k++)
// {
// for (int j = - (height / 2); j <= height / 2; j++)
// {
// for (int i = - (width / 2); i <= width / 2; i++)
// {
// offsets[index] = ( k * maxY + j) * maxX + i;
////offsets[index] = volArray.getOffset(i, j, k);
// index++;
// }
// }
// }
// return offsets;
// }
    /**
     * Applies this filter kernel to a Volume Array.  Edges of source data are ignored based on radius of filter kernel.
     * Equivalent to a call to <code>applyAdd(volArray, false)</code>.
     * @param volArray the source data to filter
     * @return A filtered array
     */
    public VolumeArray applyAdd(VolumeArray volArray) {
        return this.applyAdd(volArray, false);
    }

    /**
     * Applies this filter kernel to a Volume Array.  If <code>maskArray</code> is null, then edges of source data are
     * ignored based on radius of filter kernel.  If a mask is supplied, then areas not marked as part of the brain by
     * the mask will not be used in applying the kernel, but all indices in the source data will receive a value (non-zero
     * if kernel reaches into non-zero source data that is not dropped by the mask).
     * Equivalent to a call to <code>applyAdd(volArray, maskArray, false)</code>.
     * @param volArray the source data to filter
     * @param maskArray the mask to apply for determining valid data points
     * @return A filtered array
     */
    public VolumeArray applyAdd(VolumeArray volArray, VolumeArray maskArray) {
        return this.applyAdd(volArray, maskArray, false);
    }

    /**
     * Applies this filter kernel to a Volume Array.  Edges of source data are ignored based on radius of filter kernel.
     * Equivalent to a call to <code>applyAdd(volArray, null, testForInterrupt)</code>.
     * @param volArray the source data to filter
     * @param testForInterrupt if true, then this method will periodically check if the current thread is
     * interrupted.  If so, then it immediately returns null.  If false, then the method will run normally.
     * @return A filtered array
     */
    public VolumeArray applyAdd(VolumeArray volArray, boolean testForInterrupt) {
        return this.applyAdd(volArray, null, false);
    }

    /**
     * Applies this filter kernel to a Volume Array.  If <code>maskArray</code> is null, then edges of source data are
     * ignored based on radius of filter kernel.  If a mask is supplied, then areas not marked as part of the brain by
     * the mask will not be used in applying the kernel, but all indices in the source data will receive a value (non-zero
     * if kernel reaches into non-zero source data that is not dropped by the mask).
     * @param volArray the source data to filter
     * @param maskArray the mask to apply for determining valid data points
     * @param testForInterrupt if true, then this method will periodically check if the current thread is
     * interrupted.  If so, then it immediately returns null.  If false, then the method will run normally.
     * @return A filtered array
     */
    public VolumeArray applyAdd(VolumeArray volArray, VolumeArray maskArray, boolean testForInterrupt) {
        String validationError = validateArrays(volArray, maskArray);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        return this.applyAdd((IndexedVolumeArray) volArray, (IndexedVolumeArray) maskArray, testForInterrupt);
    }

    private VolumeArray applyAdd(IndexedVolumeArray volArray, IndexedVolumeArray maskArray, boolean testForInterrupt) {
        final int maxX = volArray.getMaxX();
        final int maxY = volArray.getMaxY();
        final int maxZ = volArray.getMaxZ();
        final int maxT = volArray.getMaxTime();
        final int maxI5 = volArray.getMaxI5();
        final int size = maxX * maxY * maxZ * maxT;

        final int xEnd;
        final int yEnd;
        final int zEnd;
        final int xStart;
        final int yStart;
        final int zStart;

        final boolean useTforMask;
        final boolean use5forMask;

        if (maskArray == null) {
            xEnd = maxX - width / 2;
            yEnd = maxY - height / 2;
            zEnd = maxZ - depth / 2;
            xStart = width / 2;
            yStart = height / 2;
            zStart = depth / 2;
            useTforMask = false;
            use5forMask = false;
        } else {
            xEnd = maxX;
            yEnd = maxY;
            zEnd = maxZ;
            xStart = 0;
            yStart = 0;
            zStart = 0;
            useTforMask = (maskArray.getMaxTime() > 0);
            use5forMask = (maskArray.getMaxI5() > 0);
        }

        final float[] floatArray = new float[size];

        for (int i5 = 0; i5 < maxI5; i5++) {
            final int i5Mask = use5forMask ? i5 : 0;
            for (int t = 0; t < maxT; t++) {
                final int tMask = useTforMask ? t : 0;

                for (int z = zStart; z < zEnd; z++) {
                    for (int y = yStart; y < yEnd; y++) {
                        for (int x = xStart; x < xEnd; x++) {
                            float result = 0.0f;
                            final int index = volArray.getIndex(x, y, z, t, i5);

                            if (maskArray == null) {
                                for (int zK = 0; zK < depth; zK++) {
                                    final int zPos = z + zK - (depth / 2);

                                    for (int yK = 0; yK < height; yK++) {
                                        final int yPos = y + yK - (height / 2);

                                        for (int xK = 0; xK < width; xK++) {
                                            final int xPos = x + xK - (width / 2);
                                            final int posIndex = volArray.getIndex(xPos, yPos, zPos, t, i5);

                                            result += volArray.getDouble(posIndex) * getKernelValue(xK, yK, zK);
                                        }
                                    }
                                }
                            } else {
                                int maskCount = 0;
                                float kernelSum = 0.0f;

                                for (int zK = 0; zK < depth; zK++) {
                                    final int zPos = z + zK - (depth / 2);

                                    for (int yK = 0; yK < height; yK++) {
                                        final int yPos = y + yK - (height / 2);

                                        for (int xK = 0; xK < width; xK++) {
                                            final int xPos = x + xK - (width / 2);
                                            final int posIndex = volArray.getIndex(xPos, yPos, zPos, t, i5);
                                            final int maskIndex = useTforMask ? posIndex : maskArray.getIndex(xPos, yPos, zPos, tMask, i5Mask);

                                            if ((posIndex > -1) && (maskArray.getInt(maskIndex) != 0)) {
                                                maskCount++;
                                                final float kernelValue = getKernelValue(xK, yK, zK);
                                                kernelSum += kernelValue;
                                                final float value = (float) (volArray.getDouble(posIndex) * kernelValue);
                                                result += value;
                                            }
                                        }
                                    }
                                }

                                if ((maskCount != 0) && (maskCount != this.kernel.length)) {
                                    //double factor = ((double) this.kernel.length) / maskCount;
                                    result /= kernelSum;
                                }
                            } // end maskArray == null;

                            floatArray[index] = result;
                        } // end x

                        if (testForInterrupt && Thread.currentThread().isInterrupted()) {
                            System.out.println("applyAdd(...) interrputed, cancelling...");
                            return null;
                        }
                    } // end y
                } // end z
            } // end t
        }

        Matrix4d index2Space = new Matrix4d(volArray.getIndex2Space());
        return new FloatIndexedVolumeArray(maxX, maxY, maxZ, maxT, maxI5, index2Space, floatArray);

    }

    private static String validateArrays(VolumeArray volArray, VolumeArray maskArray) {
        if (volArray == null) {
            return "volArray cannot be null";
        }

        if (!Kernel3D.canApply(volArray)) {
            return "Type of volArray is incompatable";
        }

        if (!Kernel3D.canApply(maskArray)) {
            return "Type of maskArray is incompatable";
        }

        if (maskArray == null) {
            return null;
        }

        if ((volArray.getMaxX() != maskArray.getMaxX()) || (volArray.getMaxY() != maskArray.getMaxY()) || (volArray.getMaxZ() != maskArray.getMaxZ())) {
            return "Dimensions do not agree";
        }

        if (!volArray.getSpace2Index().epsilonEquals(maskArray.getSpace2Index(), 0.000001)) {
            return "Transforms do not agree to within 0.000001";
        }


        return null;
    }
}
