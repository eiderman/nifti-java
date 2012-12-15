package edu.washington.biostr.sig.volume.colors;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

// TODO: Write a getHashCode method
/**
 * <p>Create an efficient (time, not space) lookup array for translating values
 * from the volume into colors.  This precomputes values from ColorSchemes into
 * arrays.</p>
 * <p>Implementations should be immutable.</p>
 * @author Eider Moore
 * @version 1
 */
public class ColorLookupTable {

    /**
     * This stores an array of colors represented by int[]s.  It is used to look
     * up positive values.
     */
    protected int[] pcolors;
    /**
     * This stores an array of colors represented by int[]s.  It is used to look
     * up negative values.
     */
    protected int[] ncolors;
    /**
     * The multiplier is used to convert doubles to ints.  The current conversion
     * is (int) (double * multiplier).  This may change slightly with roundings.
     */
    protected float multiplier;
    private double min;
    private double max;
    AlphaComposite composite = AlphaComposite.Src;

    /**
     * Create a scheme for values between min and max.
     * @param min The minimum supported value.
     * @param max The maximum supported value.
     */
    public ColorLookupTable(double min, double max) {
        this(1000, min, max);
    }

    /**
     * Create a scheme for values between min and max.
     * @param min The minimum supported value.
     * @param max The maximum supported value.
     */
    public ColorLookupTable(int min, int max) {
        this(1, min, max);
    }

    /**
     * Create a scheme for values betwenn min and max.  
     * The multiplier is used to convert floating points into indices.  Large values
     * result in higher resolution (note that most schemes only have a few 
     * thousand colors), but it takes more space to store and more time to 
     * precompute.
     * @param multiplier The multiplier is used to convert floating points into indices.
     * @param min The minimum supported value.
     * @param max The maximum supported value.
     */
    public ColorLookupTable(float multiplier, double min, double max) {
        if (multiplier <= 0) {
            throw new IllegalArgumentException("Illegal multiplier (<= 0) " + multiplier);
        }
        this.multiplier = multiplier;
        double highIndex = (multiplier * max) + 1;
        if (highIndex > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Illegal multiplier (too big) " + multiplier + " " + max);
        }
        pcolors = new int[(int) highIndex];
        if (min < 0) {
            double lowIndex = (multiplier * -min) + 1;
            if (lowIndex > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Illegal multiplier (too big) " + multiplier);
            }

            ncolors = new int[(int) lowIndex];
        } else {
            ncolors = new int[0];
        }
        this.min = min;
        this.max = max;
    }

    /**
     * Look up the given value and return an ARGB integer.
     * @param value The value from the volume.
     * @return An ARGB value.
     */
    public int getColor(double value) {
        if (value > max) {
            value = max;
        }
        if (value < min) {
            value = min;
        }
        if (value >= 0) {
            return pcolors[(int) (multiplier * value)];
        } else {
            return ncolors[(int) (multiplier * -value)];
        }
    }
    
    public void setColor(double value, int color) {
        if (value > max) {
            value = max;
        }
        if (value < min) {
            value = min;
        }
        if (value >= 0) {
            pcolors[(int) (multiplier * value)] = color;
        } else {
            ncolors[(int) (multiplier * -value)] = color;
        }
    }

    /**
     * Use the scheme to fill the values from low to high.  We assume that the
     * range for this in values for the scheme is from [0, 1]
     * @param positive Whether or not this is positive or negative
     * @param low a value between [0, high]
     * @param high a value between [low, color.length)
     * @param scheme
     */
    protected void fill(boolean positive, int low, int high, ColorScheme scheme) {
        double increment = 1.0 / (high - low);
        if (positive && (high > pcolors.length)) {
            high = pcolors.length;
        } else if (!positive && (high > ncolors.length)) {
            high = ncolors.length;
        }
        if (high <= low) {
            return;
        }
        double value = 0;
        for (int i = low; i < high; i++) {
            if (positive) {
                pcolors[i] = scheme.getARGB(value);
            } else {
                ncolors[i] = scheme.getARGB(value);
            }
            value += increment;
        }
    }

    /**
     * Finish up the arrays by filling all values from low to high with the
     * defaultValue.
     * @param positive Whether or not this is positive or negative
     * @param low a value between [0, high]
     * @param high a value between [low, color.length)
     * @param defaultValue
     */
    protected void finish(boolean positive, int low, int high,
            int defaultValue) {
        if (positive && (high > pcolors.length)) {
            high = pcolors.length;
        } else if (!positive && (high > ncolors.length)) {
            high = ncolors.length;
        }
        for (int i = low; i < high; i++) {
            if (positive) {
                pcolors[i] = defaultValue;
            } else {
                ncolors[i] = defaultValue;
            }
        }
    }
    private boolean positiveSet = false;
    private boolean negativeSet = false;

    /**
     * Set the positive values for this using the specified color scheme with
     * the given threshold and window.  threshold + window should be less than
     * the maximum value for the image.
     * @param scheme The coloring scheme to use
     * @param threshold The value to use as 0 (values < threshold are treated as 0)
     * @param window The width of the colors(values > window + threshold are treated as 1)
     * @throws IllegalStateException if this has been set before.
     */
    public void setPositive(ColorScheme scheme, double threshold, double window) {
        if (positiveSet) {
            throw new IllegalStateException();
        }

        set(true, scheme, threshold, window);
        positiveSet = true;
        this.composite = scheme.getAlphaComposite();
    }

    /**
     * Set the negative values for this using the specified color scheme with
     * the given threshold and window.  threshold + window should be less than
     * the maximum value for the image.
     * @param scheme The coloring scheme to use
     * @param threshold The value to use as 0 (values > threshold are treated as 0)
     * @param window The width of the colors(values < window + threshold are treated as 1)
     * @throws IllegalStateException if this has been set before.
     */
    public void setNegative(ColorScheme scheme, double threshold, double window) {
        if (negativeSet) {
            throw new IllegalStateException();
        }

        set(false, scheme, Math.abs(threshold), Math.abs(window));
        negativeSet = true;
    }

    private void set(boolean positive, ColorScheme scheme, double threshold,
            double window) {
        int low = (int) (threshold * multiplier);
        int high = (int) (window * multiplier);
        if (high < low) {
            high = low;
        }
        fill(positive, low, high, scheme);
        int length;
        if (positive) {
            length = pcolors.length;
        } else {
            length = ncolors.length;
        }
        finish(positive, 0, low, 0);
        finish(positive, high, length, scheme.getARGB(1));
    }

    /**
     * Get the compositing rule for blending this color with an existing image.
     * @return the compositing rule.
     */
    public AlphaComposite getAlphaComposite() {
        return composite;
    }

    /**
     * Dispatch to equals(ColorLookupTable) or return false.  Note hashCode is 
     * undefined...
     * @param o
     * @return true if the color tables are equal.
     */
    public boolean equals(Object o) {
        if (o instanceof ColorLookupTable) {
            return this.equals((ColorLookupTable) o);
        }
        return false;
    }

    /**
     * Compares two ColorLookupTables based on the requirement that all of each tables
     * instance variables satisfy each of their equals conditions.
     * @param other The other ColorLookupTable for comparison
     * @return true if the equals condition is met, false otherwise
     */
    public boolean equals(ColorLookupTable other) {
        if (other == null) {
            return false;
        }

        if (!this.composite.equals(other.composite)) {
            return false;
        }

        if (this.max != other.max) {
            return false;
        }

        if (this.min != other.min) {
            return false;
        }

        if (this.multiplier != other.multiplier) {
            return false;
        }

        if (this.negativeSet != other.negativeSet) {
            return false;
        }

        if (this.positiveSet != other.positiveSet) {
            return false;
        }

        if ((this.pcolors != null) && (other.pcolors != null)) {
            if (this.pcolors.length != other.pcolors.length) {
                return false;
            }

            for (int i = 0; i < this.pcolors.length; i++) {
                if (this.pcolors[i] != other.pcolors[i]) {
                    return false;
                }
            }
        } else if ((this.pcolors == null) && (other.pcolors != null)) {
            return false;
        } else if ((this.pcolors != null) && (other.pcolors == null)) {
            return false;
        }

        if ((this.ncolors != null) && (other.ncolors != null)) {
            if (this.ncolors.length != other.ncolors.length) {
                return false;
            }

            for (int i = 0; i < this.ncolors.length; i++) {
                if (this.ncolors[i] != other.ncolors[i]) {
                    return false;
                }
            }
        } else if ((this.ncolors == null) && (other.ncolors != null)) {
            return false;
        } else if ((this.ncolors != null) && (other.ncolors == null)) {
            return false;
        }

        return true;
    }

    /**
     * Get the maximum supported value.
     * @return The maximum supported value.
     */
    public double getMax() {
        return max;
    }

    /**
     * Get the minimum supported value.
     * @return The minimum supported value.
     */
    public double getMin() {
        return min;
    }

    /**
     * Make a legend that can be used to express the range that this table
     * can display.
     * @param positive whether this legend is for positive values.
     * @param horizontal Whether this legend is oriented left to right or top to bottom
     * @param width The width in pixels
     * @param height The height in pixels
     * @return A new BufferedImage with an evenly spaced legend.
     */
    public BufferedImage generateLegend(boolean positive, boolean horizontal, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        double value = 0;
        double step;
        int size = horizontal ? width : height;
        if (positive) {
            step = max / size;
        } else {
            step = min / size;
        }
        if (horizontal) {
            for (int i = 0; i < size; i++) {
                g2d.setColor(new Color(getColor(value)));
                value += step;
                if (horizontal) {
                    g2d.drawLine(i, 0, i, height);
                } else {
                    g2d.drawLine(0, i, width, i);
                }
            }
        }
        return img;
    }
}
