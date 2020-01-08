package comp557.a4;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class Mesh extends Intersectable {
	
	/** Static map storing all meshes by name */
	public static Map<String,Mesh> meshMap = new HashMap<String,Mesh>();
	
	/**  Name for this mesh, to allow re-use of a polygon soup across Mesh objects */
	public String name = "";
	
	/**
	 * The polygon soup.
	 */
	public PolygonSoup soup;

	public Mesh() {
		super();
		this.soup = null;
	}
	
	public Vector3d subtract(Point3d p1, Point3d p2) {
		return new Vector3d(p1.x - p2.x, p1.y - p2.y, p1.z - p2.z);
	}
		
	@Override
	public void intersect(Ray ray, IntersectResult result) {
		
		// TODO: Objective 7: ray triangle intersection for meshes
		//Vector3d origin, dir, v0, v1, v2;
		
		
		for(int i = 0; i<this.soup.faceList.size(); i++) {
			int[] f = soup.faceList.get(i);
			
			
			Point3d v0 = new Point3d(soup.vertexList.get(f[0]).p);
			Point3d v1 = new Point3d(soup.vertexList.get(f[1]).p);
			Point3d v2 = new Point3d(soup.vertexList.get(f[2]).p);
			
			Vector3d v0v1 = subtract(v1, v0);
			Vector3d v0v2 = subtract(v2, v0);
			
			Vector3d pvec = new Vector3d();
			pvec.cross(ray.viewDirection, v0v2); 
			
			double det = pvec.dot(v0v1); 
			
//		    // ray and triangle are parallel if det is close to 0
		    if (Math.abs(det) < 1e-9) continue; 
		    
		    double invDet =  1.0 / det; 
		    
		    Vector3d tvec = subtract(ray.eyePoint, v0);
		    double u = tvec.dot(pvec) * invDet; 
		    if (u < 0 || u > 1) continue; 
		    
		    Vector3d qvec = new Vector3d();
		    qvec.cross(tvec, v0v1);
		    		
		    double v = ray.viewDirection.dot(qvec) * invDet; 
		    if (v < 0 || u + v > 1) continue; 

		    double t = v0v2.dot(qvec) * invDet; 
		    
		    //if(t < 1e-9 || t > result.t) continue;
		    
		    if(t < result.t && t>1e-9) {
		    	result.t = t;
			    
			    ray.getPoint(t, result.p);
			    
			    result.n.cross(v0v1, v0v2);
			    result.n.normalize();
			    result.material = material;
		    }
		    
		    
		    
			 
			    //return true; 
		    		
		}
		
		//this.soup.faceList
		
		
		
		
		

		
	}
	
	@Override
	public void intersect(Ray ray, IntersectResult result, double[] time) {
		this.intersect(ray, result);
	}

}
