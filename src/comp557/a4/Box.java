package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A simple box class. A box is defined by it's lower (@see min) and upper (@see max) corner. 
 */
public class Box extends Intersectable {

	public Point3d max;
	public Point3d min;
	
    /**
     * Default constructor. Creates a 2x2x2 box centered at (0,0,0)
     */
    public Box() {
    	super();
    	this.max = new Point3d( 1, 1, 1 );
    	this.min = new Point3d( -1, -1, -1 );
    }
    
    //I implemented this -- raffray
    public Box(Point3d max, Point3d min, Material material) {
    	this.max.set(max);
    	this.min.set(min);;
    	this.material = material;
    }
    
    @Override
	public void clone(Box obj) {
		this.max.set(obj.max);
		this.min.set(obj.min);
		this.material.clone(obj.material);	
	}
    

	@Override
	public void intersect(Ray ray, IntersectResult result) {
		// TODO: Objective 6: intersection of Ray with axis aligned box
		double temp = 0;
		
		double tmin = (min.x - ray.eyePoint.x) / ray.viewDirection.x; 
	    double tmax = (max.x - ray.eyePoint.x) / ray.viewDirection.x; 
	 
	    if (tmin > tmax) { //txlow = min(txmin, txmax), txhigh = max(txmin, txmax)
	    	temp = tmin;
	    	tmin = tmax;
	    	tmax = temp;
	    }
	 
	    double tymin = (min.y - ray.eyePoint.y) / ray.viewDirection.y; 
	    double tymax = (max.y - ray.eyePoint.y) / ray.viewDirection.y; 
	 
	    if (tymin > tymax) { //tylow = min(tymin, tymax), tyhigh = max(tymin, tymax)
	    	temp = tymin;
	    	tymin = tymax;
	    	tymax = temp;
	    }
	 
	    if ((tmin > tymax) || (tymin > tmax)) 
	        return; 
	 
	    if (tymin > tmin) 
	        tmin = tymin; //tmin = max(txlow, tylow)
	 
	    if (tymax < tmax) 
	        tmax = tymax;  //tmax = min(txhigh, tyhigh)
	 
	    double tzmin = (min.z - ray.eyePoint.z) / ray.viewDirection.z; 
	    double tzmax = (max.z - ray.eyePoint.z) / ray.viewDirection.z; 
	 
	    if (tzmin > tzmax) {
	    	temp = tzmin;
	    	tzmin = tzmax;
	    	tzmax = temp;
	    }
	 
	    if ((tmin > tzmax) || (tzmin > tmax)) 
	        return; 
	 
	    if (tzmin > tmin) 
	        tmin = tzmin; 
	 
	    if (tzmax < tmax) 
	        tmax = tzmax; 
	    
	    //now we have set tmin and tmax
	    Point3d pointOfIntersection = new Point3d();
	    double t = Math.min(tmin, tmax);
	    double t2 = Math.max(tmin, tmax);
	    
	    if(result.t > t && t > 1e-9) {
	  	
	    	result.t = t;
	    	result.tmax = t2;
	    	ray.getPoint(t, pointOfIntersection);
	    	result.p.set(pointOfIntersection);
	    	
	    	result.material = material;
	    	
	    	Vector3d x = new Vector3d();
	    	Vector3d y = new Vector3d();
	    	
	    	if(Math.abs(result.p.x - min.x) < 0.00001 || Math.abs(result.p.x - max.x) < 0.00001) {
	    		x.set(new Vector3d(0,max.y-min.y,0));
	    		y.set(new Vector3d(0,0,max.z-min.z));
	    	}else if(Math.abs(result.p.y - min.y) < 0.00001 || Math.abs(result.p.y - max.y) < 0.00001) {
	    		x.set(new Vector3d(0,0,max.z-min.z));
	    		y.set(new Vector3d(max.x-min.x,0,0));
	    	}else if(Math.abs(result.p.z - min.z) < 0.00001 || Math.abs(result.p.z - max.z) < 0.00001) {
	    		x.set(new Vector3d(max.x-min.x,0,0));
	    		y.set(new Vector3d(0,max.y-min.y,0));    
	    	}

	    	
	    	result.n.cross(x, y);
	    	result.n.normalize();

	    }
	}
	
	@Override
	public void intersect(Ray ray, IntersectResult result, double[] time) {}

}
