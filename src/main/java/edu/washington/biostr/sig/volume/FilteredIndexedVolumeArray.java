package edu.washington.biostr.sig.volume;

/**
 * Dynamically filter a volume array by applying the given function to it as
 * data is required. This is very space efficient, but may use extra CPU since
 * nothing is precomputed.
 * @author Eider Moore
 * @version 1
 */
class FilteredIndexedVolumeArray extends IndexedVolumeArray {

    IndexedVolumeArray array;
    VolumeFunction filter;

    /**
     * Create basic mapping from the source array to the desired filtered array.
     * @param array The source
     * @param filter The function to apply at every point.
     */
    public FilteredIndexedVolumeArray(IndexedVolumeArray array, VolumeFunction filter) {
        super(array.getMaxX(), array.getMaxY(), array.getMaxZ(),
                array.getMaxTime(), array.getMaxI5(), array.getIndex2Space());
        this.filter = filter;
        this.array = array;
    }

    @Override
    public double getDouble(int index) {
        return filter.filter(array.getDouble(index));
    }

    @Override
    public int getInt(int index) {
        return (int) Math.round(getDouble(index));
    }

    @Override
    public void setData(int index, double value) {
        throw new UnsupportedOperationException("Filtered volumes are immutable!");
    }

    @Override
    public void setData(int index, int value) {
        throw new UnsupportedOperationException("Filtered volumes are immutable!");
    }

    @Override
    public DataType getNaturalType() {
        return array.getNaturalType();
    }

    @Override
    public DataType getType() {
        return array.getType();
    }
    
    @Override
    public Object getDataArray() {
    	return array.getDataArray();
    }
}
