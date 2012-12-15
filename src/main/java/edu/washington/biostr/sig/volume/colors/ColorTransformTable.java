package edu.washington.biostr.sig.volume.colors;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

/**
 * Provide a lookup table for transforming ints that hold RGB to new ints that
 * hold RGB values.  It is by default sparse and unspecified values are unchanged.
 * @author Eider Moore
 * @version 1.1
 */
public class ColorTransformTable {

    private Int2IntMap transform;

    public final static int NO_COLOR = 1;
    /**
     * Create a blank table, unspecified values are unchanged.
     */
    public ColorTransformTable() {
        transform = new Int2IntOpenHashMap();
        transform.defaultReturnValue(NO_COLOR);
    }
    
    /**
     * Set the default return value.  "1" is used to denote that the original
     * value should be returned (-1 is important because it is opaque white and
     * 0 is completely transparent black.  Completely transparent, almost black
     * blue is probably less used than those other 2 candidates).
     * @param value
     */
    public void setDefault(int value) {
        transform.defaultReturnValue(value);
    }

    /**
     * Add an entry to the table
     * @param originalColor The original RGB color.
     * @param targetColor The new RGB color.
     */
    public void add(int originalColor, int targetColor) {
        transform.put(originalColor, targetColor);
    }

    /**
     * Look up originalColor and return the entry in the table or 
     * originalColor if non specified.
     * @param originalColor
     * @return The target color in the table or originalColor if non specified.
     */
    public int get(int originalColor) {
        int r = transform.get(originalColor);
        if (r == NO_COLOR) {
            return originalColor;
        } else {
            return r;
        }
    }
}
