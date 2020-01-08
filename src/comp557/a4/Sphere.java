package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A simple sphere class.
 */
public class Sphere extends Intersectable {

	/** Radius of the sphere. */
	public double radius = 1;

	/** Location of the sphere center. */
	public Point3d center = new Point3d(0, 0, 0);

	/** This is used for motion blur. */
	public double velocity = 0;
	
	
	/**
	 * Default constructor
	 */
	public Sphere() {
		super();
	}

	/**
	 * Creates a sphere with the request radius and center.
	 * 
	 * @param radius
	 * @param center
	 * @param material
	 */
	public Sphere(double radius, Point3d center, Material material) {
		super();
		this.radius = radius;
		this.center = center;
		this.material = material;
	}
	
	@Override
	public void clone(Sphere obj) {
		this.radius = obj.radius;
		this.center.set(obj.center);
		this.material.clone(obj.material);	
	}

	@Override
	public void intersect(Ray ray, IntersectResult result) {

//    	/** The normal at the intersection */ 
//    	public Vector3d n = new Vector3d();
//    	
//    	/** Intersection position */
//    	public Point3d p = new Point3d();
//    	
//    	/** The material of the intersection */
//    	public Material material = null;
//    		
//    	/** Parameter on the ray giving the position of the intersection */
//    	public double t = Double.POSITIVE_INFINITY; 
		// TODO: Objective 2: intersection of ray with sphere
		double t1 = 0;
		double t2 = 0;
		double t = Double.POSITIVE_INFINITY;
		double tmax = 0;
		//System.out.println("enter");
		boolean intersectAtT1 = false;
		boolean intersectAtT2 = false;

		Vector3d p = new Vector3d(ray.eyePoint.x, ray.eyePoint.y, ray.eyePoint.z);
		p.sub(this.center);
		Vector3d d = ray.viewDirection;

		
//		  t1 = ( -d.dot(p) + Math.sqrt( Math.pow(d.dot(p), 2) - (d.dot(d) *
//		  (p.dot(p)-Math.pow(radius, 2)))) ) / (d.dot(d));
//		  
//		  t2 = ( -d.dot(p) - Math.sqrt( Math.pow(d.dot(p), 2) - (d.dot(d) *
//		 (p.dot(p)-Math.pow(radius, 2)))) ) / (d.dot(d));
//		 
		
		double dis = (d.dot(p) * d.dot(p)) - ((d.dot(d)) * (p.dot(p) - Math.pow(radius, 2)));
    	
		//System.out.println(dis);
		
    	// Check for the value of the Discriminant
    	if (dis < 0)
    		return; // Discriminant is negative, no Real Roots exist
    	dis = Math.sqrt(dis);
    	
    	t1 = (- d.dot(p) + dis) / d.dot(d);
    	t2 = (- d.dot(p) - dis) / d.dot(d);

//		if (Double.isNaN(t1) && Double.isNaN(t2)) {
//			// then no intersection
//			//System.out.println("Hello");
//			return;
//		} 
    	//System.out.println("exit");
    	
		if (t1 <= t2) {
			t = t1;// then two point intersection at t1 and t2	
			tmax = t2;
		}
		else {
			t = t2;
			tmax = t1;
		}

		if(t < result.t && t>1e-9) {
			result.t = t;
			result.tmax = tmax;
			ray.getPoint(t, result.p);
			result.n.x = (result.p.x - center.x);
			result.n.y = (result.p.y - center.y);
			result.n.z = (result.p.z - center.z);
			result.n.normalize();

			result.material = material;
		}


	}
	
	 @Override
	    public void intersect( Ray ray, IntersectResult result, double[] time ) {
	    
	        // TODO: Objective 2: intersection of ray with sphere
	    	double t1, t2;
	    	double t = Double.POSITIVE_INFINITY;
	    	
	    	Point3d timeCenter = new Point3d(this.center);
	    	timeCenter.x += time[0] * this.velocity;
	    	timeCenter.y += time[1] * this.velocity;
	    	timeCenter.z += time[2] * this.velocity;
	    	
	    	Vector3d p = new Vector3d();
	    	Vector3d d = new Vector3d();
	    	p.x = ray.eyePoint.x;
	    	p.y = ray.eyePoint.y;
	    	p.z = ray.eyePoint.z;
	    	p.sub(timeCenter);

	    	d = ray.viewDirection;
	    	
	    	// Equation:- (p + td).(p + td) - radius = 0
	    	// p.p + 2t(d.p) + (d)t.t - radius = 0
	    	
	    	double dis = (d.dot(p) * d.dot(p)) - ((d.dot(d)) * (p.dot(p) - (radius * radius)));
	    	
	    	// Check for the value of the Discriminant
	    	if (dis < 0)
	    		return; // Discriminant is negative, no Real Roots exist
	    	dis = Math.sqrt(dis);
	    	
	    	t1 = (- d.dot(p) + dis) / d.dot(d);
	    	t2 = (- d.dot(p) - dis) / d.dot(d);
	    	
	    	if (t1 <= t2 && t1 > 0)
	    	{
	    		t = t1;
	    	}
	    	else if (t2 <= t1 && t2 > 0)
	    	{
	    		t = t2;
	    	}	
	    	
	    	// Update the Result, if
	    	if (t < result.t && t > 1e-9)
	    	{
	    		// t
	    		result.t = t;
	    		// p
	    		Point3d point = new Point3d();
	    		ray.getPoint(t, point);
	    		result.p = point;
	    		
	    		// Material
	    		result.material = material;
	    	
	    		// Normals
	    		result.n.x = (result.p.x - timeCenter.x) / radius;
	    		result.n.y = (result.p.y - timeCenter.y) / radius;
	    		result.n.z = (result.p.z - timeCenter.z) / radius;
	    	
	    		// Normalize
	    		result.n.normalize();
	    	}
	    }

}
