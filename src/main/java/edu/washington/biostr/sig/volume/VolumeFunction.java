package edu.washington.biostr.sig.volume;

/**
 * VolumeFunctions can be mapped to a volume to convert every voxel by using
 * the filter method.
 * @author Eider Moore
 * @version 1
 */
public interface VolumeFunction {

    public double filter(double value);
}
