package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Class for a plane at y=0.
 * 
 * This surface can have two materials.  If both are defined, a 1x1 tile checker 
 * board pattern should be generated on the plane using the two materials.
 */
public class Plane extends Intersectable {
    
	/** The second material, if non-null is used to produce a checker board pattern. */
	Material material2;
	
	/** The plane normal is the y direction */
	public static final Vector3d n = new Vector3d( 0, 1, 0 );
    
    /**
     * Default constructor
     */
    public Plane() {
    	super();
    }

        
    @Override
    public void intersect( Ray ray, IntersectResult result ) {
    
        // TODO: Objective 4: intersection of ray with plane

    	if(ray.viewDirection.y == 0) return; //if the ray is on the plane
    	if (ray.viewDirection.dot(this.n) == 0) return; // Undefined

    	
    	Vector3d eyePoint = new Vector3d(ray.eyePoint.x, ray.eyePoint.y, ray.eyePoint.z);
	
		//double t = v.dot(n)/ray.viewDirection.dot(n);
    	double t = (-eyePoint.dot(this.n)) / (ray.viewDirection.dot(this.n));
    	if (t > result.t || t < 0) return;
		if(t<0) return;
		if(Math.abs(ray.viewDirection.dot(n)) < 0.0000000000001) return;
    	
    	Point3d intersectionPos = new Point3d();
    
    	if(t < result.t && t>1e-9) {
    		ray.getPoint(t, intersectionPos);
    		
        	result.p.x = intersectionPos.x;
        	result.p.y = intersectionPos.y;
        	result.p.z = intersectionPos.z;
        	
        	result.t = t;

        	result.n.x = n.x;
        	result.n.y = n.y;
        	result.n.z = n.z;
        	
          	double x = Math.ceil(result.p.x);
        	double z = Math.ceil(result.p.z);
        	Material material = new Material();
        	
        	// Even/even or odd/odd is material 1, else material 2 (if two materials)
        	if ((x % 2 == 0) == (z % 2 == 0)) material = this.material;
        	else if (this.material2 == null) material = this.material;
        	else material = this.material2;
        	
        	result.material = material;

        	
    	
    	}
    	
    	
    	
    }
    	

    	

    
    @Override
	public void intersect(Ray ray, IntersectResult result, double[] time) {}
    
}
