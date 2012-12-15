package edu.washington.biostr.sig.volume;

import java.util.BitSet;

import javax.vecmath.Matrix4d;

/**
 * Support a bit (true/false) based volume array.<br>
 * The underlying structure is currently a BitSet.<br>
 * See the documentation for VolumeArray for more data.
 * @author Eider Moore
 * @version 1.0
 */
public class BitIndexedVolumeArray extends IndexedVolumeArray {

    BitSet bits;

    /**
     * 
     * @param maxX The maximum for the x dimension (fastest changing of the spacial indices)
     * @param maxY The maximum for the y dimension.
     * @param maxZ The maximum for the z dimension (slowest changing of the spacial indices).
     * @param maxTime The maximum for the time dimension.  Usually 1.
     * @param maxI5 The maximum for the 5th dimension.  Usually 1.
     * @param index2space The transform, usually turns indices to mm coordinates.  Must be invertable.
     * @param set The data
     */
    public BitIndexedVolumeArray(int maxX, int maxY, int maxZ, int maxTime, int maxI5, Matrix4d index2space, BitSet set) {
        super(maxX, maxY, maxZ, maxTime, maxI5, index2space);
        this.bits = set;
    }

    @Override
    public double getDouble(int index) {
        return getInt(index);
    }

    @Override
    public int getInt(int index) {
        return bits.get(index) ? 1 : 0;
    }

    @Override
    public DataType getNaturalType() {
        return DataType.TYPE_INT;
    }

    @Override
    public DataType getType() {
        return DataType.TYPE_BINARY;
    }

    @Override
    public void setData(int index, double value) {
        bits.set(index, value > 0);
    }

    @Override
    public void setData(int index, int value) {
        bits.set(index, value > 0);
    }
    
    @Override
    public Object getDataArray() {
    	return bits;
    }

}
