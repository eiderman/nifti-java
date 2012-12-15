package edu.washington.biostr.sig.volume.colors;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.util.Arrays;

/**
 * An AdvancedColorScheme allows the user to create a scheme that flows from one
 * color to another (and maybe to another, etc) by doing a piecewise 
 * interpolation.<br>
 * The most convenient way to use it is by calling the getWarm() or getCold()
 * methods.<br>
 * @author Eider Moore
 * @version 1.0
 */
public class AdvancedColorScheme
        extends ColorScheme {

    Color[] color;
    double[] start;
    double[] end;
    private transient AlphaComposite composite;
    int compositeValue;
    int[] rgb;
    String name;

    /**
     * Create a simple grayscale color scheme.
     */
    public AdvancedColorScheme() {
        compositeValue = AlphaComposite.SRC_OVER;
        rgb = new int[4];
        color = new Color[1];
        color[0] = Color.WHITE;
        start = new double[1];
        start[0] = 0;
        end = new double[1];
        end[0] = 1;
    }

    /**
     * Interpolate across the given number of colors (first from black to color 1,
     * then from from color 1 to color 2, etc.)
     * @param numColors The number of colors to interpolate across.
     */
    public AdvancedColorScheme(int numColors) {
        compositeValue = AlphaComposite.SRC_OVER;
        rgb = new int[4];
        color = new Color[numColors];
        color[0] = Color.WHITE;
        start = new double[numColors];
        start[0] = 0;
        end = new double[numColors];
        end[0] = 1;
        for (int i = 1; i < numColors; i++) {
            color[i] = Color.BLACK;
            start[i] = 0;
            end[i] = 1;
        }
    }

    /**
     * Use one of AlphaComposite's values to mix this in with any existing colors
     * on a palatte based on the rule and alpha level.
     * @param composite The composite value from AlphaComposite
     */
    public void setComposite(int composite) {
        compositeValue = composite;
    }

    /**
     * Add a color that will be blended with the existing color.
     * @param i The color index.
     * @param color The color.
     * @param start The starting value to blend this (between 0 and 1)
     * @param end The ending value to blend this (between 0 and 1)
     */
    public void setColor(int i, Color color, double start, double end) {
        this.color[i] = color;
        this.start[i] = start;
        this.end[i] = end;
    }

    public int[] getRGB(double value) {
        rgb = new int[4];
        boolean negative = false;
        if (value < 0) {
            negative = true;
        }
        rgb[0] = 0;
        rgb[1] = 0;
        rgb[2] = 0;
        rgb[3] = 0;
        if (Math.abs(value) == 0) {
            return rgb;
        } else {
            double range = 1;
            value = (Math.abs(value)) / range;
            if (negative) {
                value = -value;
            }
        }

        double percent;
        if (color == null) {
            return rgb;
        }
        for (int i = 0; i < color.length; i++) {
            if (negative) {
                if ((start[i] < 0) && (value < start[i])) {
                    if (value < end[i]) {
                        rgb[0] += color[i].getRed();
                        rgb[1] += color[i].getGreen();
                        rgb[2] += color[i].getBlue();
                    } else {
                        percent = (value - start[i]) / (end[i] - start[i]);
                        rgb[0] += color[i].getRed() * percent;
                        rgb[1] += color[i].getGreen() * percent;
                        rgb[2] += color[i].getBlue() * percent;
                    }
                }
            } else {
                if ((start[i] >= 0) && (value > start[i])) {
                    if (value > end[i]) {
                        rgb[0] += color[i].getRed();
                        rgb[1] += color[i].getGreen();
                        rgb[2] += color[i].getBlue();
                    } else {
                        percent = (value - start[i]) / (end[i] - start[i]);
                        rgb[0] += color[i].getRed() * percent;
                        rgb[1] += color[i].getGreen() * percent;
                        rgb[2] += color[i].getBlue() * percent;
                    }
                }
            }
        }

        for (int i = 0; i < rgb.length; i++) {
            if (rgb[i] >= 255) {
                rgb[i] = 255;
            }
            if (rgb[i] > 0) {
                rgb[3] = (alpha);
            }
        }
        return rgb;
    }

    public String toString() {
        return name;
    }

    public boolean equals(Object o) {
        if (o instanceof AdvancedColorScheme) {
            AdvancedColorScheme s = (AdvancedColorScheme) o;
            return (this == s) ||
                    ((alpha == s.alpha) &&
                    (compositeValue == s.compositeValue) &&
                    Arrays.equals(color, s.color) &&
                    Arrays.equals(start, s.start) &&
                    Arrays.equals(end, s.end));
        }
        return false;
    }

    public AlphaComposite getComposite() {
        if (composite == null) {
            composite = AlphaComposite.getInstance(compositeValue);
        }
        return composite;
    }

    /**
     * Generate a color scheme that flows from black to red to yellow to white
     * (it has a warm feel and a large level of granularity).
     * @param alpha The level of opacity
     * @return A scheme that generates warm colors.
     */
    public static AdvancedColorScheme getWarm(double alpha) {
        AdvancedColorScheme scheme = new AdvancedColorScheme(3);
        scheme.setAlpha(alpha);
        scheme.setColor(0, Color.RED, 0, .33);
        scheme.setColor(1, Color.GREEN, .33, .66);
        scheme.setColor(2, Color.BLUE, .66, 1);
        scheme.name = "warm";
        return scheme;
    }

    /**
     * Generate a color scheme that flows from black to blue to cyan to white
     * (it has a cold feel and a large level of granularity).
     * @param alpha The level of opacity
     * @return A scheme that generates cold colors.
     */
    public static AdvancedColorScheme getCold(double alpha) {
        AdvancedColorScheme scheme = new AdvancedColorScheme(3);
        scheme.setAlpha(alpha);
        scheme.setColor(0, Color.BLUE, 0, .33);
        scheme.setColor(1, Color.GREEN, .33, .66);
        scheme.setColor(2, Color.RED, .66, 1);
        scheme.name = "cold";
        return scheme;
    }

    /**
     * Generate a color scheme for a single color.
     * @param color The desired color
     * @param name The name of this scheme
     * @return A color scheme for a single color.
     */
    public static AdvancedColorScheme getColor(Color color, String name) {
        AdvancedColorScheme scheme = new AdvancedColorScheme(3);
        scheme.setAlpha(.5);
        scheme.setColor(0, color, 0, 1);
        scheme.name = name;
        return scheme;
    }

    public int hashCode() {
        long code = 17;
        for (int i = 0; i < color.length; i++) {
            code = code + color[i].hashCode() * 37;
        }
        for (int i = 0; i < start.length; i++) {
            code = code + Double.doubleToLongBits(start[i]) * 37;
        }
        return (int) code;
    }
}
