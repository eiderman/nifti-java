package edu.washington.biostr.sig.volume;

import java.awt.image.BufferedImage;

import javax.vecmath.Matrix4d;

/**
 * This class takes a matrix (extractor) that extracts an image by:
 * <ol>
 * <li>Applying it to point (0,0,0) to find the center of the new image</li>
 * <li>Applying it to Vector(width,0,0) to find the right vector</li>
 * <li>Applying it to Vector(0,height,0) to find the down vector</li>
 * <li>Extracting the image</li>
 * </ol>
 * @author eider
 */
public class ImageExtractor {
	private Matrix4d extractor; 
	private float widthmm;
	private float heightmm;
	private double resolution = 1; 
	
	public ImageExtractor() {
		
	}
	
	public ImageExtractor(Matrix4d extractor, float widthmm, float heightmm) {
		this.extractor = extractor;
		this.widthmm = widthmm;
		this.heightmm = heightmm;
	}

	public Matrix4d getExtractor() {
		return extractor;
	}

	public void setExtractor(Matrix4d extractor) {
		this.extractor = extractor;
	}

	public float getWidthmm() {
		return widthmm;
	}

	public void setWidthmm(float widthmm) {
		this.widthmm = widthmm;
	}

	public float getHeightmm() {
		return heightmm;
	}

	public void setHeightmm(float heightmm) {
		this.heightmm = heightmm;
	}
	
	public void setResolution(double resolution) {
		this.resolution = resolution;
	}
	
	public double getResolution() {
		return resolution;
	}
	
	private void check() {
		if (extractor == null) {
			throw new IllegalStateException("ImageExtractor cannot be used unless extractor is set!");
		}
		if (widthmm == 0 || heightmm == 0) {
			throw new IllegalStateException("Width and Heigh must be non zero (" + widthmm +", " + heightmm + ")");
		}
	}
}
