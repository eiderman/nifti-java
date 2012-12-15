package edu.washington.biostr.sig.volume;

import edu.washington.biostr.sig.nifti.AnalyzeNiftiSpmHeader;

/**
 * these correspond to the datatype values in analyze 7.5 as much as possible
 * @author Eider Moore
 * @version 1.0
 */
public enum DataType {

    TYPE_BINARY(AnalyzeNiftiSpmHeader.DT_BINARY, 1),
    TYPE_BYTE(AnalyzeNiftiSpmHeader.DT_INT8, 8),
    TYPE_UBYTE(AnalyzeNiftiSpmHeader.DT_UINT8, 8),
    TYPE_SHORT(AnalyzeNiftiSpmHeader.DT_INT16, 16),
    TYPE_USHORT(AnalyzeNiftiSpmHeader.DT_UINT16, 16),
    TYPE_INT(AnalyzeNiftiSpmHeader.DT_INT32, 32),
    TYPE_RGB(AnalyzeNiftiSpmHeader.DT_RGB24, 32),
    // this doesn't match up, we just needed it.
    TYPE_LONG(AnalyzeNiftiSpmHeader.DT_INT64, 64),
    TYPE_FLOAT(AnalyzeNiftiSpmHeader.DT_FLOAT32, 32),
    TYPE_DOUBLE(AnalyzeNiftiSpmHeader.DT_FLOAT64, 64),
    /**
     * This is a custom type that is signified by the existance of an atlas.xml file.
     */
    TYPE_ATLAS(-1, -1);
    private short value;
    private int bitsPerEntry;

    DataType(int value, int bitsPerEntry) {
        this.value = (short) value;
        this.bitsPerEntry = bitsPerEntry;
    }

    /**
     * The value in Analyze and NIFTI
     * @return The value used to represent this in the header file.
     */
    public short getValue() {
        return value;
    }

    /**
     * Find the value of the data from an Analyze or NIFTI file.
     * @param value The value in the analyze header
     * @return The data type
     */
    public static DataType valueOf(int value) {
        for (DataType type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException(value + " does not correspond to any DataType.");
    }

    /**
     * 
     * @return The number of bits needed to encode 1 unit of data.
     */
    public int getBitsPerEntry() {
        return bitsPerEntry;
    }
}
