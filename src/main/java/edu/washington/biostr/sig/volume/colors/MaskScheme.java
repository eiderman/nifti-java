package edu.washington.biostr.sig.volume.colors;

import java.awt.AlphaComposite;

/**
 * Return 0,0,0,1 for anything below the threshold (non transparent black, this
 * will black out anything it is applied over) and 0,0,0,0 for anything at or
 * above the threshold (basically a completely transparent nothing, this should
 * not change anything that it is applied over).  Use ColorLookupTable for 
 * thresholding.
 * @author Eider Moore
 * @version 1
 */
public class MaskScheme
        extends ColorScheme {

    static final int IN = (255 << 24);
    static final int OUT = 0;

    private MaskScheme() {
    }
    static MaskScheme scheme = new MaskScheme();

    /**
     * Get the MaskScheme (all of them are the same).
     * @return the MaskScheme
     */
    public static MaskScheme getMaskScheme() {
        return scheme;
    }

    public int[] getRGB(double value) {
        if (value > 0) {
            return new int[]{0, 0, 0, 0};
        } else {
            return new int[]{0, 0, 0, 255};
        }
    }

    public int getARGB(double value) {
        if (value > 0) {
            return IN;
        }
        return OUT;
    }

    public boolean equals(Object parm1) {
        if (parm1 instanceof MaskScheme) {
            return true;
        }
        return false;
    }

    /**
     * @return SrcIn
     */
    public AlphaComposite getAlphaComposite() {
        return AlphaComposite.DstIn;
    }

    public int hashCode() {
        return 1203231;
    }

    public String toString() {
        return "mask";
    }
}
