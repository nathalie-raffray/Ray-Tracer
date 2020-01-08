package comp557.a4;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;


public class Quadric extends Intersectable {
    
	/**
	 * Radius of the sphere.
	 */
	public Matrix4d Q = new Matrix4d();
	public Matrix3d A = new Matrix3d();
	public Vector3d B = new Vector3d();
	public double C;
	
	/**
	 * The second material, e.g., for front and back?
	 */
	Material material2 = null;
	
	public Quadric() {
	
	}
	
	@Override
	public void intersect(Ray ray, IntersectResult result) {
		
	}
	
	@Override
	public void intersect(Ray ray, IntersectResult result, double[] time) {}
	
}
