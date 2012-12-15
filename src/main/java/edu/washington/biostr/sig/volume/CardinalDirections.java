package edu.washington.biostr.sig.volume;

/**
 * The main directions in an MRI.  This has mappings for other ways of thinking
 * about the data, but the 4 main ones are: CORONAL, SAGITTAL, TRANSVERSE, OBLIQUE.
 * @author eider
 *
 */
public enum CardinalDirections {
	CORONAL, SAGITTAL, TRANSVERSE, OBLIQUE;
	
	public static CardinalDirections XZ = SAGITTAL;
	public static CardinalDirections XY = TRANSVERSE;
	public static CardinalDirections YZ = CORONAL;
	public static CardinalDirections NORMAL_X = CORONAL;
	public static CardinalDirections NORMAL_Y = SAGITTAL;
	public static CardinalDirections NORMAL_Z = TRANSVERSE;
	public static CardinalDirections HORIZONTAL = TRANSVERSE;
	public static CardinalDirections PARASAGITTAL = SAGITTAL;
}
