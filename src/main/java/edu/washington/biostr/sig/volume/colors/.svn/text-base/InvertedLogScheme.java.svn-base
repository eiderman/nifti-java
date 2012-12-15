package edu.washington.biostr.sig.volume.colors;

/**
 * This provides a wrapper for a color scheme.  Instead of generating increasing
 * values from 0 to 1, it instead generates values that start at maximum
 * brightness and decrease exponentially.  Its primary purpose is to support
 * Z-Scores with arbitrary color schemes.
 * @author Eider Moore
 * @version 1
 */
public class InvertedLogScheme
        extends ColorScheme {

    ColorScheme scheme;
    double base;

    public InvertedLogScheme(ColorScheme scheme, double base) {
        this.scheme = scheme;
        this.base = base;
    }

    public int[] getRGB(double value) {
        if (value < 1) {
            value = 1 - value;
            value = Math.pow(base, value);
            value = value / base;
        } else {
            value = 0;
        }
        return scheme.getRGB(value);
    }
}
