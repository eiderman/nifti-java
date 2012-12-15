package edu.washington.biostr.sig.volume;

import java.util.BitSet;

import javax.vecmath.Matrix4d;

/**
 * This will take in various arrays and convert them into VolumeDataBuffers.
 * It also provides convenience methods to convert signed data into the next
 * type up because Java doesn't support unsigned primitives.
 * @author Eider Moore
 * @version 1.0
 */
public class VolumeArrayFactory {

    /**
     * Get a VolumeDataBuffer.  data is one of byte[], int[], short[], char[]
     * (for unsigned shorts), double[] or float[].<br>
     * In the future this may support int[][][][], short[][][][], etc. but
     * for now, it requires that you provide the information as a 1 dimensional
     * array.  The type is inferred from the type of data, so the VolumeDataBuffer.getType()
     * may not returned the loaded type, but will contain a type appropriate for
     * saving.
     * @param index2space
     * @param data
     * @param maxX
     * @param maxY
     * @param maxZ
     * @param maxTime
     * @param dataType
     * @return
     */
    public static VolumeArray getVolumeDataBuffer(Matrix4d index2space,
            Object data, int maxX,
            int maxY, int maxZ,
            int maxTime, int maxI5) {
        if (data instanceof BitSet) {
            return new BitIndexedVolumeArray(maxX, maxY, maxZ, maxTime, maxI5,
                    index2space, (BitSet) data);
        } else if (data instanceof byte[]) {
            return new ByteIndexedVolumeArray(maxX, maxY, maxZ, maxTime, maxI5,
                    index2space, (byte[]) data);
        } else if (data instanceof double[]) {
            return new DoubleIndexedVolumeArray(maxX, maxY, maxZ, maxTime, maxI5,
                    index2space, (double[]) data);
        } else if (data instanceof float[]) {
            return new FloatIndexedVolumeArray(maxX, maxY, maxZ, maxTime, maxI5,
                    index2space, (float[]) data);
        } else if (data instanceof int[]) {
            return new IntIndexedVolumeArray(maxX, maxY, maxZ, maxTime, maxI5,
                    index2space, (int[]) data);
        } else if (data instanceof short[]) {
            return new ShortIndexedVolumeArray(maxX, maxY, maxZ, maxTime, maxI5,
                    index2space, (short[]) data);
        } else if (data instanceof char[]) {
            return new CharIndexedVolumeArray(maxX, maxY, maxZ, maxTime, maxI5,
                    index2space, (char[]) data);
        } else {
            throw new IllegalArgumentException(
                    "Please choose a type from ImageData. bad: " + data.getClass());
        }
    }

    /**
     * Create a volume with the specified structure that holds RGB data.
     * @param index2space
     * @param data
     * @param maxX
     * @param maxY
     * @param maxZ
     * @param maxTime
     * @param maxI5
     * @return The specified RGB volume.
     */
    public static VolumeArray getRGBBuffer(Matrix4d index2space,
            int[] data, int maxX,
            int maxY, int maxZ,
            int maxTime, int maxI5) {
        return new RGBIndexedVolumeArray(maxX, maxY, maxZ, maxTime, maxI5,
                index2space, data);
    }

//   public static VolumeArray getVolumeDataBuffer(Matrix4d index2space,
//                                                 byte[] data, int xMax,
//                                                 int yMax, int zMax,
//                                                 int maxTime, int maxI5, DataType dataType,
//                                                 ByteOrder order)
//   {
//      return getVolumeDataBuffer(index2space, ByteBuffer.wrap(data).order(order),
//                                 xMax, yMax, zMax, maxTime, maxI5, dataType);
//   }
    /**
     * Copy the basic structure (dimensions, indices to coordinate transform, etc),
     * but make the data type <code>type</code> instead of the original data type.
     * @param other The array to derive the structure from.
     * @param type The new data type
     * @return A new VolumeArray that lines up with other.
     */
    public static VolumeArray copyStructure(VolumeArray other, DataType type) {
        return createVolumeDataBuffer(other.getIndex2Space(),
                other.getMaxX(), other.getMaxY(), other.getMaxZ(), other.getMaxTime(), other.getMaxI5(),
                type);
    }

    /**
     * Copy the basic structure (dimensions, data type, 
     * indices to coordinate transform, etc).
     * @param other The array to derive the structure from.
     * @return A new VolumeArray that lines up with other.
     */
    public static VolumeArray copyStructure(VolumeArray other) {
        return copyStructure(other, other.getType());
    }

    /**
     * Create an empty VolumeArray
     * @param index2space
     * @param xMax
     * @param yMax
     * @param zMax
     * @param maxTime
     * @param dataType
     * @return
     */
    public static VolumeArray createVolumeDataBuffer(Matrix4d index2space,
            int xMax,
            int yMax, int zMax,
            int maxTime, int maxI5, DataType dataType) {
        Object data;
        int len = xMax * yMax * zMax * maxTime;
        switch (dataType) {
            case TYPE_BINARY:
                data = new BitSet(len);
                break;
            case TYPE_BYTE:
                data = new byte[len];
                break;
            case TYPE_DOUBLE:
                data = new double[len];
                break;
            case TYPE_FLOAT:
                data = new float[len];
                break;
            case TYPE_INT:
                data = new int[len];
                break;
            case TYPE_SHORT:
                data = new short[len];
                break;
            case TYPE_USHORT:
                data = new char[len];
                break;
            case TYPE_UBYTE:
                return new UnsignedByteIndexedVolumeArray(xMax, yMax, zMax, maxTime, maxI5,
                        index2space, new byte[len]);
            case TYPE_RGB:
                return new RGBIndexedVolumeArray(xMax, yMax, zMax, maxTime, maxI5,
                        index2space, new int[len]);
            default:
                throw new IllegalArgumentException(
                        "Please choose a type from ImageData " + dataType + " is not valid");
        }

        return getVolumeDataBuffer(index2space, data, xMax, yMax, zMax, maxTime, maxI5);
    }
//   public static VolumeArray getVolumeDataBuffer(Matrix4d index2space,
//                                                 ByteBuffer data, int xMax,
//                                                 int yMax, int zMax,
//                                                 int maxTime, int maxI5, DataType dataType)
//   {
//      switch (dataType)
//      {
//         case TYPE_BYTE:
//            return getVolumeDataBuffer(index2space, data.array(),
//                                       xMax, yMax, zMax, maxTime, maxI5);
//         case TYPE_DOUBLE:
//            return getVolumeDataBuffer(index2space,
//                                       data.asDoubleBuffer().array(),
//                                       xMax, yMax, zMax, maxTime, maxI5);
//         case TYPE_FLOAT:
//            return getVolumeDataBuffer(index2space,
//                                       data.asFloatBuffer().array(),
//                                       xMax, yMax, zMax, maxTime, maxI5);
//         case TYPE_INT:
//            return getVolumeDataBuffer(index2space,
//                                       data.asIntBuffer().array(),
//                                       xMax, yMax, zMax, maxTime, maxI5);
//         case TYPE_SHORT:
//            return getVolumeDataBuffer(index2space,
//                                       data.asShortBuffer().array(),
//                                       xMax, yMax, zMax, maxTime, maxI5);
//         case TYPE_USHORT:
//            return getVolumeDataBuffer(index2space,
//                                       data.asCharBuffer().array(),
//                                       xMax, yMax, zMax, maxTime, maxI5);
//         default:
//            throw new IllegalArgumentException(
//                "Please choose a type from ImageData");
//      }
//   }
}
