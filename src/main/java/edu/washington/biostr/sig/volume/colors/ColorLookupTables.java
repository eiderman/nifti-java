package edu.washington.biostr.sig.volume.colors;

import edu.washington.biostr.sig.volume.DataType;
import edu.washington.biostr.sig.volume.VolumeArray;

/**
 * Provide a convenience method for generating a ColorLookupTable.
 * @author Eider Moore
 * @version 1
 */
public class ColorLookupTables {

    /**
     * I assume that the maximum resolution that all but the most advanced
     * color schemes will have is 1000 values (with a single color, its 256.
     * With warm or cold, its around 766).
     */
    public static final int NUM_VALUES = 1000;

    /**
     * Generate a color lookup table.
     * @param positive The color scheme for positive values.
     * @param max The maximum value
     * @param posThreshold The lower bound on positive numbers
     * @param posWindow The upper bound on positive numbers
     * @param negative The colorscheme to use for negative values
     * @param min The minimum value
     * @param negThreshold The lower bound on negative numbers
     * @param negWindow The upper bound on negative numbers
     * @param alpha The level of transparency to use.
     * @param integer If the data is stored as floats or integers, false will always work but will consume more memory.
     * @return A new table with the above settings.
     */
    public static ColorLookupTable getTable(
            ColorScheme positive, double max, double posThreshold, double posWindow,
            ColorScheme negative, double min, double negThreshold, double negWindow,
            double alpha, boolean integer) {
        positive.setAlpha(alpha);
        float mult;
        if (Math.abs(negWindow) < Math.abs(posWindow / 100)) {
            mult = integer ? 1f : (float) (NUM_VALUES / (posWindow - posThreshold));
        } else if (Math.abs(posWindow) < Math.abs(negWindow / 100)) {
            mult = integer ? 1f : (float) (NUM_VALUES / (negWindow - negThreshold));
        } else {
            mult = integer ? 1f : (float) Math.max(max <= 0 ? 0 : NUM_VALUES / (posWindow - posThreshold),
                    min >= 0 ? 0 : Math.abs(NUM_VALUES / (negWindow - negThreshold)));
        }
        ColorLookupTable table = new ColorLookupTable(mult, min, max);
        if (negative != null) {
            negative.setAlpha(alpha);
            table.setNegative(negative, negThreshold, negWindow);
        }
        table.setPositive(positive, posThreshold, posWindow);
        return table;
    }
    
    public static ColorLookupTable getTable(ColorScheme positive, ColorScheme negative, VolumeArray array) {
    	return ColorLookupTables.getTable(
                positive, array.getImageMax(), 0, array.getImageMax(),
                negative, array.getImageMin(), 0, array.getImageMin(),
                1, array.getNaturalType().equals(DataType.TYPE_INT));    	
    }
}
