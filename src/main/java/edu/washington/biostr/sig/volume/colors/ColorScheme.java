package edu.washington.biostr.sig.volume.colors;

import java.awt.AlphaComposite;

/**
 * <p>This produces colors for values from 0 to 1.  Implementors should
 * override getRGB() or getARGB().  getRGB is provided merely for 
 * implementation convenience.</p>
 * @author Eider Moore
 * @version 1
 */
public abstract class ColorScheme {

    protected int alpha;

    /**
     * Create a ColorScheme with alpha = 1.0
     */
    public ColorScheme() {
        alpha = 255;
    }

    /**
     * Return true if this supports alpha, false if it is completely opaque.Ï
     * @return true if this supports alpha
     */
    public boolean supportsAlpha() {
        return alpha < 255;
    }

    /**
     * Set the alpha value from 0 to 1.
     * @param alpha
     */
    public void setAlpha(double alpha) {
        this.alpha = (int) (255 * alpha);
    }

    public double getAlpha() {
        return alpha / 255;
    }

    /**
     * Get an RGBA value as 4 ints {R, G, B, A}
     * @param value
     * @return A 4 int RGBA value.
     */
    protected int[] getRGB(double value) {
        return new int[4];
    }

    /**
     * Get the RGBA value as 1 int.<br>
     * By default this calls getRGB and converts the 
     * 4 ints into 1.
     * @param value
     * @return An integer that holds an ARGB value.
     */
    public int getARGB(double value) {
        int[] values = getRGB(value);
        int result = (values[3] & 255) << 24;
        result = result | ((values[0] & 255) << 16);
        result = result | ((values[1] & 255) << 8);
        result = result | ((values[2] & 255));
        return result;
    }

    /**
     * Get the alpha composite for this scheme.
     * @return by default SrcOver.
     */
    public AlphaComposite getAlphaComposite() {
        return AlphaComposite.SrcOver;
    }
}
