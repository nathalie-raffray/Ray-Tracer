package comp557.a4;

import java.util.ArrayList;
import java.util.*; 

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import javax.vecmath.Color4f;

//Perlin Noise based off Ken Perlin's paper and https://flafla2.github.io/2014/08/09/perlinnoise.html

public class PerlinNoise extends Intersectable{
	
	String shape; //shape will either be a cube or a sphere

	//public Box b; 
	//public Sphere s;
	
	public Material m1, m2, m3; //etc
	
	public Intersectable obj;
	
	private static int[] permutation = { 151,160,137,91,90,15,                 // Hash lookup table as defined by Ken Perlin.  This is a randomly
		    131,13,201,95,96,53,194,233,7,225,140,36,103,30,69,142,8,99,37,240,21,10,23,    // arranged array of all numbers from 0-255 inclusive.
		    190, 6,148,247,120,234,75,0,26,197,62,94,252,219,203,117,35,11,32,57,177,33,
		    88,237,149,56,87,174,20,125,136,171,168, 68,175,74,165,71,134,139,48,27,166,
		    77,146,158,231,83,111,229,122,60,211,133,230,220,105,92,41,55,46,245,40,244,
		    102,143,54, 65,25,63,161, 1,216,80,73,209,76,132,187,208, 89,18,169,200,196,
		    135,130,116,188,159,86,164,100,109,198,173,186, 3,64,52,217,226,250,124,123,
		    5,202,38,147,118,126,255,82,85,212,207,206,59,227,47,16,58,17,182,189,28,42,
		    223,183,170,213,119,248,152, 2,44,154,163, 70,221,153,101,155,167, 43,172,9,
		    129,22,39,253, 19,98,108,110,79,113,224,232,178,185, 112,104,218,246,97,228,
		    251,34,242,193,238,210,144,12,191,179,162,241, 81,51,145,235,249,14,239,107,
		    49,192,214, 31,181,199,106,157,184, 84,204,176,115,121,50,45,127, 4,150,254,
		    138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180
		};
	
	private int[] p;                                                    // Doubled permutation to avoid overflow
	
	
	PerlinNoise(Box b){
		fillP();
		shape = "box";
		obj = new Box();
		obj.clone(b);
		this.material.clone(b.material);
	}
	PerlinNoise(Sphere s){
		fillP();
		shape = "sphere";
		obj = new Sphere();
		obj.clone(s);
		this.material.clone(s.material);
	}
	
	PerlinNoise(){
		fillP();
		shape = "unknown";
	}
	
	
	private void fillP() {//only called once per perlin noise. izz good
		p = new int[512];
		for(int i = 0; i<512; i++) {
			p[i] = permutation[i%256];
		}
	}
	
	public String toString() {
		if(shape.equalsIgnoreCase("box")) {
			//return "shape: "+this.shape + "| box min: " + this.obj.min + "| material diffuse: " + this.material.diffuse;
		}
		return "shape: "+this.shape + "| material diffuse: " + this.material.diffuse;
	}

	
	@Override
	public void intersect(Ray ray, IntersectResult result) {
		//IntersectResult attempt = new IntersectResult();
		double tInitial = result.t;

		IntersectResult ir = new IntersectResult(result);
		
		if(shape.equals("sphere") || shape.equals("box")) {
			obj.intersect(ray, ir);
		}
		
		else {
			System.out.println("Unknown Perlin Noise shape.");
			return;
		}
		
		if(Math.abs(result.t - ir.t) < 0.000001 || ir.t == Double.POSITIVE_INFINITY) { //in this case, there was no intersection worth noting. 
			return; 
		}
		
		//Perlin Noise
		double noise = perlin(result.p.x, result.p.y, result.p.z);
		
		//System.out.println(noise);
		//if(result.material.diffuse == null) System.out.print("null");
		//System.out.println(result.material.diffuse);
		//result.material.diffuse = new Color4f(1, 1, 1, 1);
		//result.material.diffuse.scale((float)noise);
		
		if(noise > 0.6) {
			//System.out.println(tInitial);
			//System.out.println(result.t);
			return;
		}
		else {
//			result.t = ir.t;
//			result.p.set(ir.p);
//			result.n.set(ir.n);
//			result.material = ir.material;

			
			double averageT = (ir.t + ir.tmax)/2;
			result.t = lerp(noise, ir.t, ir.tmax);
			ray.getPoint(result.t, result.p);
			//System.out.println("t: "+ir.t+"| tmax: "+ir.tmax+" |result.t: "+result.t);
			result.material = ir.material;
			if(result.t <= averageT) result.n = ir.n;
			else {
				ir.n.scale(-1);
				ir.n.normalize();
				result.n = ir.n;
			}
			
			//result.p.scale(noise);
			
			//System.out.println(noise);
			//result.material.diffuse.set(new Color4f((float) (result.material.diffuse.x * noise), result.material.diffuse.y, result.material.diffuse.z, 1f));
			//result.material.diffuse.x = (float) (result.material.diffuse.x * (float)noise);
			//System.out.println("noise: "+noise+"| diffuse: "+result.material.diffuse.x);
			
		}
		
		//problem is that i want to keep both t values for perlin noise
		//then i can use a t value that is in between minT and maxT, possible interpolate with noise to get it


	
	}
	
	public double perlin(double x, double y, double z) {
		
		//calculate the min corner of the integer unit cube surrounding x,y,z, and then & 255
		//this will be used as a key to hash through the p[] array to get a pseudo random number 
		//& 255 operation will assure that the number is positive and between 1 and 255 
		int xUnit = (int) Math.floor(x) & 255;
		int yUnit = (int) Math.floor(y) & 255;
		int zUnit = (int) Math.floor(z) & 255;
		
		//xDiff is the difference between x and x0 (x0 is the x value of the min corner of unit cube)
		//xDiff are values between 0 and 1
		double xDiff = x-Math.floor(x);
		double yDiff = y-Math.floor(y);
		double zDiff = z-Math.floor(z);
		
		//fade so that we have a smoother transition between gradients
		double sx = fade(xDiff);
	    double sy = fade(yDiff);
	    double sz = fade(zDiff);

	    
	    //these correspond to the eight corners of unit cube
	    //Lowercase letter corresponds to min coordinate, uppercase letter corresponds to max coordinate
	    //ie. xyz corresponds to the min left corner, XYZ corresponds to max right corner
	    //each corner has a pseudorandom value between 1 and 255, which will be used to calculate a random unit gradient attached to them
	    int xyz, xYz, xyZ, xYZ, Xyz, XYz, XyZ, XYZ;
	    xyz = p[p[p[xUnit]  +  yUnit]+    zUnit ];
	    xYz = p[p[p[xUnit]  +yUnit+1]+    zUnit ];
	    xyZ = p[p[p[xUnit]  +  yUnit]+ zUnit + 1];
	    xYZ = p[p[p[xUnit]  +yUnit+1]+ zUnit + 1];
	    Xyz = p[p[p[xUnit+1]+  yUnit]+    zUnit ];
	    XYz = p[p[p[xUnit+1]+yUnit+1]+    zUnit ];
	    XyZ = p[p[p[xUnit+1]+  yUnit]+ zUnit + 1];
	    XYZ = p[p[p[xUnit+1]+yUnit+1]+ zUnit + 1];
	    
	    double s, t, u, v;
	    double aX, bX, cY, dY, p;
	    
	    s = computeGradient(xyz, xDiff, yDiff, zDiff);
	    t = computeGradient(Xyz, xDiff-1, yDiff, zDiff);
	    u = computeGradient(xYz, xDiff, yDiff-1, zDiff);
	    v = computeGradient(XYz, xDiff-1, yDiff-1, zDiff);
	    
	    aX = lerp(sx, s, t);
	    bX = lerp(sx, u, v);
	    
	    cY = lerp(sy, aX, bX);
	    
	    s = computeGradient(xyZ, xDiff, yDiff, zDiff-1);
	    t = computeGradient(XyZ, xDiff-1, yDiff, zDiff-1);
	    u = computeGradient(xYZ, xDiff, yDiff-1, zDiff-1);
	    v = computeGradient(XYZ, xDiff-1, yDiff-1, zDiff-1);
	    
	    aX = lerp(sx, s, t);
	    bX = lerp(sx, u, v);
	    
	    dY = lerp(sy, aX, bX);
	    
	    p = lerp(sz, cY, dY); //our noise value calculated for (x, y, z)!
	    
	    return (p+1)/2;  // For convenience we bind the result to 0 - 1 (theoretical min/max before is [-1, 1])
    
		
	}

	public double lerp(double alpha, double y0, double y1) {
		return alpha * y1 + (1 - alpha) * y0;
	}
	
	/*this is to compute the gradient ie. dot product between random unit vector at a given corner
	(a, b, c) and the difference between our initial point, (xi, yi, zi), and a given corner point, (xc, yc, zc)
	in this function: (x, y, z) = (xi - xc, yi - yc, zi - zc)
	
	therefore computeGradient returns (a, b, c) dot (x, y, z)
	
	the random unit vectors to choose from are:
	(1,1,0),(-1,1,0),(1,-1,0),(-1,-1,0),
	(1,0,1),(-1,0,1),(1,0,-1),(-1,0,-1),
	(0,1,1),(0,-1,1),(0,1,-1),(0,-1,-1) 
	
	* hash determines which unit vector is chosen to dot with (x, y, z)
	*/
	public double computeGradient(int hash, double x, double y, double z)
	{
	    switch(hash & 0xF) //hash is a number between 1 and 255. 
	    {
	        case 0x0: return  x + y;
	        case 0x1: return -x + y;
	        case 0x2: return  x - y;
	        case 0x3: return -x - y;
	        case 0x4: return  x + z;
	        case 0x5: return -x + z;
	        case 0x6: return  x - z;
	        case 0x7: return -x - z;
	        case 0x8: return  y + z;
	        case 0x9: return -y + z;
	        case 0xA: return  y - z;
	        case 0xB: return -y - z;
	        case 0xC: return  y + x;
	        case 0xD: return -y + z;
	        case 0xE: return  y - x;
	        case 0xF: return -y - z;
	        default: return 0; // never happens
	    }
	}
	
	public static double fade(double t) {
        // Fade function as defined by Ken Perlin.  This eases coordinate values
        // so that they will ease towards integral values.  This ends up smoothing
        // the final output.
		return t * t * t * (t * (t * 6 - 15) + 10);         // 6t^5 - 15t^4 + 10t^3
	}
	
	@Override
	public void intersect(Ray ray, IntersectResult result, double[] time) {}
	

		

}
