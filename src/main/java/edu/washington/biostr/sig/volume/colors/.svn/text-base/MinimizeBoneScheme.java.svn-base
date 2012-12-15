package edu.washington.biostr.sig.volume.colors;

import java.awt.Color;

/**
 * This is an attempt to provide a grayscale that becomes bonelike near 1.<br>
 * In MRI images, the brightest structures are bone and so it is helpful to 
 * reduce the brightness of it and make it distinct from the now brighter brain.
 * @author Eider Moore
 * @version 1
 */
public class MinimizeBoneScheme
        extends ColorScheme {

    Color baseColor;
    Color dropTo;

    public MinimizeBoneScheme() {
        baseColor = Color.white;
        dropTo = new Color(191, 184, 162);
    }

    public int[] getRGB(double value) {
        int[] rgb = new int[4];
        if (value < .95) {
            value /= .95;
            value = Math.abs(value);
            rgb[0] = (int) Math.round(value * baseColor.getRed());
            rgb[1] = (int) Math.round(value * baseColor.getGreen());
            rgb[2] = (int) Math.round(value * baseColor.getBlue());
            if (value > 0) {
                rgb[3] = alpha;
            } else {
                rgb[3] = 0;
            }
        } else {
            value = (value - .95) / .05;
            double drop = value *
                    (255 - Math.max(Math.max(dropTo.getRed(), dropTo.getBlue()),
                    dropTo.getGreen()));

            rgb[0] = (int) Math.round(dropTo.getRed() + drop);
            rgb[1] = (int) Math.round(dropTo.getGreen() + drop);
            rgb[2] = (int) Math.round(dropTo.getBlue() + drop);
        /*         rgb[0] = (int) Math.round(baseColor.getRed() - value * (255- dropTo.getRed()));
        rgb[1] = (int) Math.round(baseColor.getGreen() - value * (255- dropTo.getGreen()));
        rgb[2] = (int) Math.round(baseColor.getBlue() - value * (255- dropTo.getBlue()));*/
        }
        return rgb;
    }
}
