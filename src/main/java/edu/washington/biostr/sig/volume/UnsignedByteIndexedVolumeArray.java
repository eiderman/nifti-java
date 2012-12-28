package edu.washington.biostr.sig.volume;

import javax.vecmath.Matrix4d;

/**
 * Support an unsigned 8 bit integer based volume array.<br>
 * See the documentation for IndexedVolumeArray for more data.
 * @author Eider Moore
 * @version 1.0
 */
public class UnsignedByteIndexedVolumeArray extends ByteIndexedVolumeArray {

    /**
     * @param maxX The maximum for the x dimension (fastest changing of the spacial indices)
     * @param maxY The maximum for the y dimension.
     * @param maxZ The maximum for the z dimension (slowest changing of the spacial indices).
     * @param maxTime The maximum for the time dimension.  Usually 1.
     * @param maxI5 The maximum for the 5th dimension.  Usually 1.
     * @param index2space The transform, usually turns indices to mm coordinates.  Must be invertable.
     * @param array The data
     */
    public UnsignedByteIndexedVolumeArray(int maxX, int maxY, int maxZ, int maxTime, int maxI5, Matrix4d index2space, byte[] array) {
        super(maxX, maxY, maxZ, maxTime, maxI5, index2space, array);
    }

    @Override
    public int getInt(int index) {
        int v = super.getInt(index);
        if (v < 0) {
            v = 256 + v;
        }
        return v;
    }

    @Override
    public double getDouble(int index) {
        int v = super.getInt(index);
        if (v < 0) {
            v = 256 + v;
        }
        return v;
    }

    @Override
    public DataType getType() {
        return DataType.TYPE_UBYTE;
    }
    
    @Override
    public void setData(int index, int value) {
        getDataArray()[index] = (byte) (value & 0xff);
    }
}
