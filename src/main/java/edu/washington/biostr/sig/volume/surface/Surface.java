package edu.washington.biostr.sig.volume.surface;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * Encapsulate information about a surface.  The surface is indexed by
 * the faces array and each point/normal consists of 3 floats.
 * @author Eider Moore
 * @version 1
 */
public class Surface {

    int[] faces;
    float[] points;
    float[] normals;
    float[] colors;

    public Surface(int[] faces, float[] points, float[] normals) {
        this(faces, points, normals, null);
    }

    public Surface(int[] faces, float[] points, float[] normals, float[] colors) {
        this.faces = faces;
        this.points = points;
        this.normals = normals;
        this.colors = colors;
    }

    public float[] getColors() {
        return colors;
    }

    public Vector3f getNormal(int vertex, Vector3f rv) {
        if (rv == null) {
            rv = new Vector3f();
        }
        int offset = vertex * 3;
        rv.set(normals[offset], normals[offset + 1], normals[offset + 2]);
        return rv;
    }

    public void setColors(float[] colors) {
        this.colors = colors;
    }

    public int[] getFaces() {
        return faces;
    }

    public float[] getNormals() {
        return normals;
    }

    public float[] getPoints() {
        return points;
    }

    /**
     * If you know that the faces are wound counter-clockwise, this will
     * fix them.
     */
    public void rewindFaces() {
        for (int i = 0; i < faces.length; i += 3) {
            int t = faces[i];
            faces[i] = faces[i + 2];
            faces[i + 2] = t;
        }
    }

    /** 
     * If normals do not exist, then use a very simple algorithm to
     * generate the normals.  This algorithm makes no attempt to sort 
     * out creases.
     */
    public void makeNormals() {
        if (normals == null) {
            normals = new float[points.length];
            Vector3f v1 = new Vector3f();
            Vector3f v2 = new Vector3f();
            Vector3f n = new Vector3f();
            for (int i = 0; i < faces.length; i += 3) {
                int i1 = faces[i] * 3;
                int i2 = faces[i + 1] * 3;
                int i3 = faces[i + 2] * 3;
                v1.set(points[i1] - points[i2],
                        points[i1 + 1] - points[i2 + 1],
                        points[i1 + 2] - points[i2 + 2]);
                v2.set(points[i1] - points[i3],
                        points[i1 + 1] - points[i3 + 1],
                        points[i1 + 2] - points[i3 + 2]);
                n.cross(v1, v2);
                // now add this to the current values
                normals[i1] += n.x;
                normals[i1 + 1] += n.y;
                normals[i1 + 2] += n.z;
                normals[i2] += n.x;
                normals[i2 + 1] += n.y;
                normals[i2 + 2] += n.z;
                normals[i3] += n.x;
                normals[i3 + 1] += n.y;
                normals[i3 + 2] += n.z;
            }

            // now normalize
            for (int i = 0; i < normals.length; i += 3) {
                double len = normals[i] * normals[i] +
                        normals[i + 1] * normals[i + 1] +
                        normals[i + 2] * normals[i + 2];
                len = Math.sqrt(len);
                normals[i] /= len;
                normals[i + 1] /= len;
                normals[i + 2] /= len;
            }
        }
    }

    public int[] getFace(int faceIndex, int[] rv) {
        if (rv == null) {
            rv = new int[3];
        }
        int offset = faceIndex * 3;
        for (int i = 0; i < 3; i++) {
            rv[i] = faces[i + offset];
        }
        return rv;
    }

    public Point3f getVertex(int vertexIndex, Point3f rv) {
        if (rv == null) {
            rv = new Point3f();
        }
        int offset = vertexIndex * 3;
        rv.set(points[offset], points[offset + 1], points[offset + 2]);
        return rv;

    }

    public void setNormals(float[] normals) {
        this.normals = normals;
    }
    
    /**
     * Smooth this by a factor of lambda, going for depth number of neighbors. 
     * @param lambda
     * @param depth
     */
    public void smooth(double lambda, int depth) {
    	// neighbor index
    	IntSet[] vertexIndex = new IntSet[points.length/3];
    	for (int i = 0; i < vertexIndex.length; i++) {
    		vertexIndex[i] = new IntArraySet(5);
    	}
    	for (int i = 0; i < faces.length; i+=3) {
    		int f1 = faces[i];
    		int f2 = faces[i+1];
    		int f3 = faces[i+2];
    		vertexIndex[f1].add(f2);
    		vertexIndex[f1].add(f3);
    		vertexIndex[f2].add(f1);
    		vertexIndex[f2].add(f3);
    		vertexIndex[f3].add(f1);
    		vertexIndex[f3].add(f2);
    	}
    	for (int c = 0; c < depth; c++) {
	    	double avgDist = 0;
	    	double avgNewDist = 0;
	    	float[] newPoints = new float[points.length];
	    	for (int i = 0; i < points.length/3; i++) {
	    		float x = points[i * 3];
	    		float y = points[i * 3 + 1];
	    		float z = points[i * 3 + 2];
	    		avgDist += Math.sqrt(x * x + y * y + z * z);
	    		double size = 1;
	    		for (int j : vertexIndex[i]) {
	    			x += points[j * 3] * lambda;
	    			y += points[j * 3 + 1] * lambda;
	    			z += points[j * 3 + 2] * lambda;
	    			size += lambda;
	    		}
	    		x /= size;
	    		y /= size;
	    		z /= size;
	    		//try and keep it the same size.
	    		avgNewDist += Math.sqrt(x * x + y * y + z * z);
	    		newPoints[i * 3] = x;
	    		newPoints[i * 3 + 1] = y;
	    		newPoints[i * 3 + 2] = z;
	    	}
	    	float factor = (float) (avgDist / avgNewDist);
	    	for (int i = 0; i < points.length; i++){
	    		newPoints[i] *= factor;
	    	}
	    	points = newPoints;
	    	normals = null;
    	}
    }

	public void validate() {
		if (faces.length % 3 != 0 || faces.length == 0) {
			throw new IllegalArgumentException("Wrong face count: " + faces.length);
		}
		if (normals != null && points.length != normals.length) {
			throw new IllegalArgumentException();
		}
		for (int f : faces) {
			if (f >= points.length || f < 0) {
				throw new IllegalArgumentException("f is outside of range: [" + f + ";" + points.length + "]");
			}
		}
	}
}
