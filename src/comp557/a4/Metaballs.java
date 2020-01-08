package comp557.a4;

import java.util.ArrayList;

public class Metaballs extends Intersectable {
	
	ArrayList<Sphere> spheres = new ArrayList<Sphere>();

	double thresh = 0.2;

	double eps = 0.01;
	
	@Override
	public void intersect(Ray ray, IntersectResult result) {
		
	}
	
	@Override
	public void intersect(Ray ray, IntersectResult result, double[] time) {}
}

