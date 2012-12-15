package edu.washington.biostr.sig.volume;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * A very simple encapsulation of a plane with some useful equations.
 * @author eider
 *
 */
public class Plane {
	private Vector3f normal;
	private float D;
	
	public Plane(Vector3f normal, float d) {
		super();
		this.normal = normal;
		D = d;
	}

	public float getD() {
		return D;
	}
	
	public Vector3f getNormal() {
		return normal;
	}
	
	public void setNormal(Vector3f normal) {
		this.normal = normal;
	}
	
	public void setD(float d) {
		D = d;
	}
	
	/**
	 * For a ray-plane intersection the point is given by <code>pOnPlane=origin + ray * t</code>.  This method computes t.
	 * @param origin
	 * @param ray
     * @param nDOTdir normal.dot(ray), cannot be 0 (unchecked).
	 * @return t a scalar from <code>pOnPlane=origin + ray * t</code>
	 */
    public float computeT(Point3f origin, Vector3f ray, float nDOTdir) {
    	float t = -(normal.x * origin.x + normal.y * origin.y + normal.z * origin.z - D) / (nDOTdir);
    	return t;
    }
    
    /**
     * Compute the intersection based on the ray/line and t.  Notice that this is static because
     * only the computation of t depends on the plane.
     * @param origin
     * @param ray
     * @param t from computeT
     * @return The point of intersection or null if there is none.
     */
    public static Point3f computeIntersectionPoint(Point3f origin, Vector3f ray, float t) {
		Point3f p = new Point3f(ray);
		p.scale(t);
		p.add(origin);
		return p;
    }
    
    /**
     * Compute the intersection, return null if the ray is in front of this plane or parallel.<br>
     * @param origin
     * @param ray
     * @return The point of intersection or null if there is none.
     */
    public Point3f computeIntersectionWithRay(Point3f origin, Vector3f ray) {
    	float nDOTdir = ray.dot(normal);
    	if (nDOTdir == 0) { // parallel || origin inside box
    		return null;
    	} else {
    		float t = computeT(origin, ray, nDOTdir);
    		if (t < 0)
    			return null;
    		return computeIntersectionPoint(origin, ray, t);
    	}
    }
    
    /**
     * Compute the intersection, return null if the ray is in front of this plane or parallel.<br>
     * @param origin
     * @param ray
     * @return The point of intersection or null if there is none.
     */
    public Point3f computeIntersectionWithLine(Point3f origin, Vector3f dir) {
    	float nDOTdir = dir.dot(normal);
    	if (nDOTdir == 0) { // parallel || origin inside box
    		return null;
    	} else {
    		float t = computeT(origin, dir, nDOTdir);
    		return computeIntersectionPoint(origin, dir, t);
    	}
    }
}
