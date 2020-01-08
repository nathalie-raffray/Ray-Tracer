package comp557.a4;

import java.util.ArrayList;
import java.util.*; 

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Simple scene loader based on XML file format.
 */
public class Scene {
    
    /** List of surfaces in the scene */
    public List<Intersectable> surfaceList = new ArrayList<Intersectable>();
	
	/** All scene lights */
	public Map<String,Light> lights = new HashMap<String,Light>();

    /** Contains information about how to render the scene */
    public Render render;
    
    /** The ambient light colour */
    public Color3f ambient = new Color3f();
    
    /** The depth of recursion for reflection, Fresnel and refraction computations */
    public int depthMax = 27;
    

    /** 
     * Default constructor.
     */
    public Scene() {
    	this.render = new Render();
    }
    
    /**
     * renders the scene
     */
    public void render(boolean showPanel) {
 
        Camera cam = render.camera; 
        int w = cam.imageSize.width;
        int h = cam.imageSize.height;
        
        double[]offset = {0, 0};
        double[] motionTime = {0, 0, 0};
    	
        
        Iterator it = lights.entrySet().iterator();
        List<String> lightNames = new ArrayList<String>();
        List<Light> lightObjs = new ArrayList<Light>();
		while (it.hasNext()) { 
            Map.Entry mapElement = (Map.Entry)it.next(); 
            lightNames.add((String)mapElement.getKey());
            lightObjs.add((Light)mapElement.getValue());
        } 
		System.out.println(lightNames.toString());
		System.out.println(lightObjs.toString());
		
        
        render.init(w, h, showPanel);
        
        int samples = render.samples;
        double[][] offsets = calculateOffset(samples); //need to change this to render.samples
        
     // motion blur matrix
        double[] motion = new double[samples];
        calculateMotionBlur(motion, samples);
        

        boolean printR = false;
        for ( int j = 0; j < h && !render.isDone(); j++ ) {
            for ( int i = 0; i < w && !render.isDone(); i++ ) {
            	Vector3d finalLightTotal = new Vector3d();
            	
                // TODO: Objective 1: generate a ray (use the generateRay method)
            	for(int k = 0; k<offsets.length; k++) {
            		Ray ray = new Ray();
                	generateRay(i, j, offsets[k], cam, ray);

                	boolean flag = false;
                	motionTime[0] = motion[k];

                	IntersectResult ir = new IntersectResult();
                	
                	for(Intersectable obj: surfaceList) {
                		
                		if ((obj.material != null) && (obj.velocity > 0)) obj.intersect(ray, ir, motionTime);
                		else if ((obj.material == null)) obj.intersect(ray, ir, motionTime);
                		else obj.intersect(ray, ir);

                	}
                	Vector3d finalLight = new Vector3d();
                	finalLight.set(render.bgcolor);
                	//System.out.println(ir.t);

                	//Vector3d color = new Vector3d();
                	if(ir.t != Double.POSITIVE_INFINITY) {
//                		flag = true;
//            			Vector3d v = new Vector3d(); //viewing vector = (eye - intersect point)
//                 		Vector3d l = new Vector3d();
//                 		Vector3d bisector = new Vector3d();
//                 		
//                 		finalLight.x = 0;
//            			finalLight.y = 0;
//            			finalLight.z = 0;
//            			
//            			//Color3f colorLight = computeFresnelReflection(lightObjs, cam.from, ray, ir, 500, 0);
//            			
//            			for(Light lite: lightObjs) {
//            				
//            				Ray shadowRay = new Ray();
//            				IntersectResult shadowIR = new IntersectResult();
//            				//SceneNode sn = new SceneNode();
//            				if(inShadow(ir, lite, shadowIR, shadowRay)) {
//            					//System.out.println("hiii");
//                        		continue;
//            				}
//
//                    		v.sub(cam.from, ir.p);
//                    		l.sub(lite.from, ir.p);
//                    		bisector.add(v, l);
//                    		
//                    		v.normalize();
//                    		l.normalize();
//                    		bisector.normalize();
//                    		ir.n.normalize();
//                    		
//                    		//DIFFUSE LIGHTING
//                    		Vector3d diffuse = new Vector3d();
//                    		diffuse.x = ir.material.diffuse.x * lite.power * lite.color.x * Math.max(0, l.dot(ir.n));
//                    		diffuse.y = ir.material.diffuse.y * lite.power * lite.color.y * Math.max(0, l.dot(ir.n));
//                    		diffuse.z = ir.material.diffuse.z * lite.power * lite.color.z * Math.max(0, l.dot(ir.n));
//                    	
//                    		
//                    		//Blinn Phong LIGHTING
//                    		Vector3d blinnPhong = new Vector3d();
//           
//                    		blinnPhong.x = ir.material.specular.x * lite.power * lite.color.x * Math.pow(Math.max(0,  bisector.dot(ir.n)), ir.material.shinyness);
//                    		blinnPhong.y = ir.material.specular.y * lite.power * lite.color.y * Math.pow(Math.max(0,  bisector.dot(ir.n)), ir.material.shinyness);
//                    		blinnPhong.z = ir.material.specular.z * lite.power * lite.color.z * Math.pow(Math.max(0,  bisector.dot(ir.n)), ir.material.shinyness);
//                    		
//                    		
//                    		//Ambient LIGHTING
//                    		//Vector3d ambient = new Vector3d();
//                    		
//                    		
//                    		finalLight.x += diffuse.x + blinnPhong.x;
//                    		finalLight.y += diffuse.y + blinnPhong.y;
//                    		finalLight.z += diffuse.z + blinnPhong.z;
//                	
//                	}
//       	
//                	finalLight.x += ambient.x* ir.material.diffuse.x;
//            		finalLight.y += ambient.y* ir.material.diffuse.y;
//            		finalLight.z += ambient.z* ir.material.diffuse.z;
            		
            		finalLight = new Vector3d();
            		finalLight.set(ambient);
            		finalLight.x = finalLight.x * ir.material.diffuse.x;
            		finalLight.y = finalLight.y * ir.material.diffuse.y;
            		finalLight.z = finalLight.z * ir.material.diffuse.z;

            		// Compute Lambertian and Blinn-Phong Shading
            		Color3f colorLight = new Color3f();
            		//System.out.println("Pixels = " + i + ", " + j);
            		colorLight = computeFresnelReflection(lightObjs, cam.from, ray, ir, 500, 0);
            		finalLight.x = finalLight.x + colorLight.x;
            		finalLight.y = finalLight.y + colorLight.y;
            		finalLight.z = finalLight.z + colorLight.z;

                    
                }
            	if(ir.t == Double.POSITIVE_INFINITY) {
                	finalLight.x = render.bgcolor.x;
        			finalLight.y = render.bgcolor.y;
        			finalLight.z = render.bgcolor.z;
                }
            	
            	finalLightTotal.add(finalLight);
            		
            		
            }
            	

            finalLightTotal.x/=offsets.length;
            finalLightTotal.y/=offsets.length;
            finalLightTotal.z/=offsets.length;


        	Color3f c = new Color3f(finalLightTotal);
        	int r = (int)Math.min(255,(255*c.x));
            int g = (int)Math.min(255,(255*c.y));
            int b = (int)Math.min(255,(255*c.z));
            int a = 255;
            int argb = (a<<24 | r<<16 | g<<8 | b);    
            
            // update the render image
            render.setPixel(i, j, argb);
        }
        
        }
        // save the final render image
        render.save();
        
        // wait for render viewer to close
        render.waitDone();
        
        
        
    }
    
    
    public void calculateMotionBlur(double[] motion, int mSamples) {
		int count = -1;
		for (int i = 0; i < mSamples; i++)
		{
			// Divide into N samples along 0 to 1 space
			count++;
			double result = 0;
			double d = 0;
			d = 1.0 / (double)mSamples;
			result = i * d;
			motion[count] = result;
		}
	}    
	
    
    
    public double[][] calculateOffset(int samples) {
    	int nx = 0;
    	int ny = 0;

    	double[]offset = new double[2];
    	if(samples==1) {
    		offset[0] = offset[1] = 0.5;
    		nx = ny = 1;
    	}else if(checkPerfectSquare(samples)) {
    		nx = ny = (int)(Math.sqrt(samples));
    		offset[0] = 1.0/(nx*2);
        	offset[1] = 1.0/(ny*2);
        	//System.out.println(offset[0]+", "+offset[1]);
    	}else {
    		nx = (int)Math.sqrt(samples * 2);
    		ny = (int)Math.sqrt(samples / 2);
    		offset[0] = (nx == 1)? 0.5: 1.0/(nx*2); 
    		//essentially (int)Math.sqrt(samples * 2) == 1 is the number of samples in x direction, so if the number
    		//of samples is 1 then the offset[0] should be 0
    		//but if the number of samples is 2 then offset should be 1/4 = 0.25 
    		offset[1] = (ny == 1)? 0.5: 1.0/(ny*2);
    	}
    	
    	double stepX, stepY;
        stepX = (offset[0] == 0)? 1: offset[0]*2;
        stepY = (offset[1] == 0)? 1: offset[1]*2;
        
        double[][]offsets = new double[nx*ny][2];
        
        for(int k = 0; k<nx; k++) { //this will only not work when offset[0] = 0
    		
    		for(int l = 0; l<ny; l++) {
    			offsets[k*ny+l][0] = -0.5 + offset[0] + stepX*k;
    			offsets[k*ny+l][1] = -0.5 + offset[1] + stepY*l;
    		}
    		
    	}
        
    	return offsets;

    }
    
    private static boolean checkPerfectSquare(double x)  
    { 

	// finding the square root of given number 
    	double sq = Math.sqrt(x); 
    	return ((sq - Math.floor(sq)) == 0); 
    } 

    
    /**
     * Generate a ray through pixel (i,j).
     * 
     * @param i The pixel row.
     * @param j The pixel column.
     * @param offset The offset from the center of the pixel, in the range [-0.5,+0.5] for each coordinate. 
     * @param cam The camera.
     * @param ray Contains the generated ray.
     */
	public static void generateRay(final int i, final int j, final double[] offset, final Camera cam, Ray ray) {

			//System.out.println(offset[0]+", "+offset[1]);
		// TODO: Objective 1: generate rays given the provided parameters
				// Define u,v,w.
				Vector3d u = new Vector3d();
				Vector3d v = new Vector3d();
				Vector3d dw = new Vector3d();
				
				dw.x = -(cam.to.x - cam.from.x);
				dw.y = -(cam.to.y - cam.from.y);
				dw.z = -(cam.to.z - cam.from.z);
				double d = dw.length();
				
				dw.normalize();
				
				u.cross(cam.up, dw);
				u.normalize();
				
				v.cross(u, dw);
				v.normalize();
				
				// Top, Left, Bottom and Right Calculations.
				double t = d * Math.tan((cam.fovy/ 2) * Math.PI / 180);
				double b = -t;
				double width = cam.imageSize.width;
		        double height = cam.imageSize.height;
		        double r = t * (width / height);
		        double l = -r;
		          
		        double us = l + (r - l) * (i + offset[0]) / width;
		        double vs = b + (t - b) * (j + offset[1]) / height;
		        
		        Vector3d s = new Vector3d();
		        s.x = cam.from.x + us * u.x + vs * v.x - d * dw.x;
		        s.y = cam.from.y + us * u.y + vs * v.y - d * dw.y;
		        s.z = cam.from.z + us * u.z + vs * v.z - d * dw.z;
		        
		        Vector3d dv = new Vector3d();
		        dv.sub(s, cam.from);
		        dv.normalize();
		        
		        ray.set(cam.from, dv);

	}
	

	
	public Color3f computeFresnelReflection(List <Light> lightObjs, Point3d camPos, Ray ray, IntersectResult ir, double ior, int depth) {
	Vector3d v = new Vector3d(); //viewing vector = (eye - intersect point)
	Vector3d l = new Vector3d();
	Vector3d bisector = new Vector3d();
	
	Color3f finalLight = new Color3f();
	IntersectResult closestIr = new IntersectResult();
	boolean test = false;
	if (depth == 0)
	{
		closestIr = ir;
	}
	
	if (depth >= depthMax) {
		finalLight.set(render.bgcolor);
		return finalLight;
	}
	
	if (depth != 0)
	{
		//System.out.println("In Function");
    	for (Intersectable obj: surfaceList)
    	{
    		obj.intersect(ray, closestIr);
    		
    	}
    	// If the Reflected Ray has no intersections, return a constant color
    	if (closestIr.t == Double.POSITIVE_INFINITY)
    	{
    		//System.out.println("Come here");
    		Color3f defaultColor = new Color3f(render.bgcolor);
    		return defaultColor;
    	}
    	//ir = closestIr;
		//System.out.println("Done with IR");
	}
	
	// Reflection, Fresnel Reflection and Refraction
	if (closestIr.material.reflective == 1)
	{
		Color3f refractionColor = new Color3f();
		Color3f reflectionColor = new Color3f();
		
		//System.out.println("Hello");
		
        // compute fresnel
        double kr = 0; 
        double biasCons = 0.0001; 
        ior = closestIr.material.refIndex;
        kr = fresnel(ray.viewDirection, closestIr.n, ior);
        //System.out.println("kr = " + kr);
        boolean outside = ray.viewDirection.dot(closestIr.n) < 0; 
        Vector3d bias = new Vector3d(closestIr.n);
        bias.scale(biasCons); 
        // compute refraction if it is not a case of total internal reflection
        if (kr < 1 && ior < 500) { 
            Vector3d refractionDirection = new Vector3d();
            refractionDirection = refract(ray.viewDirection, closestIr.n, ior);
            refractionDirection.normalize(); 
            Vector3d refractionRayOrig = new Vector3d();
            if (outside) 
            	refractionRayOrig.sub(closestIr.p, bias);
            else
            	refractionRayOrig.add(closestIr.p, bias); 
            Ray refractionRay = new Ray();
            refractionRay.eyePoint.set(refractionRayOrig);
            refractionRay.viewDirection.set(refractionDirection);
            
            refractionColor = computeFresnelReflection(lightObjs, camPos, refractionRay, closestIr, ior, depth + 1); 
        } 

        Vector3d reflectionDirection = new Vector3d();
        reflectionDirection = reflect(ray.viewDirection, closestIr.n);
        reflectionDirection.normalize(); 
       
        Vector3d reflectionRayOrig = new Vector3d(closestIr.p);
        if (outside) 
        	reflectionRayOrig.add(closestIr.p, bias);
        else
        	reflectionRayOrig.sub(closestIr.p, bias);  
        Ray reflectionRay = new Ray();
        reflectionRay.eyePoint.set(reflectionRayOrig);
        reflectionRay.viewDirection.set(reflectionDirection);
        reflectionColor = computeFresnelReflection(lightObjs, camPos, reflectionRay, closestIr, ior, depth + 1); 

        if (ior < 500)
        {// mix the two
        reflectionColor.scale((float)kr);
        refractionColor.scale((float)(1 - kr)); 
        finalLight.x = finalLight.x + reflectionColor.x + refractionColor.x;
        finalLight.y = finalLight.y + reflectionColor.y + refractionColor.y;
        finalLight.z = finalLight.z + reflectionColor.z + refractionColor.z;
        }
        else
        {
        	reflectionColor.scale((float)kr);
        	finalLight.x = finalLight.x + reflectionColor.x;
            finalLight.y = finalLight.y + reflectionColor.y;
            finalLight.z = finalLight.z + reflectionColor.z;
            
        }
        

        
        return finalLight; 
	}
	else
	{

	for (int li = 0; li < lightObjs.size(); li++)
	{
		Light light = new Light();
		light = lightObjs.get(li);
		
		boolean inShadow = false;
		Ray shadowRay = new Ray();
		IntersectResult shadowIR = new IntersectResult();
		
		double intensity = 0;
		intensity = softShadow(closestIr, light, shadowIR, shadowRay);
		inShadow = false;
		Vector3d lightFrom = new Vector3d(light.from);
		double power = light.power;

		v.sub(camPos, closestIr.p);
		v.normalize();
		l.sub(lightFrom, closestIr.p);
		l.normalize();

		bisector.add(v, l);
		bisector.normalize();

		// Lambertian Shading
		Vector3d diffuse = new Vector3d();

		diffuse.x = closestIr.material.diffuse.x * light.color.x * intensity * power * Math.max(0.0, l.dot(closestIr.n));
		diffuse.y = closestIr.material.diffuse.y * light.color.y * intensity * power * Math.max(0.0, l.dot(closestIr.n));
		diffuse.z = closestIr.material.diffuse.z * light.color.z * intensity * power * Math.max(0.0, l.dot(closestIr.n));

		// Blinn-Phong Shading
		Vector3d blinnPhong = new Vector3d();

		if (closestIr.material.specular.x < 0)
		{
			closestIr.material.specular.negate();
		}
		
		blinnPhong.x = closestIr.material.specular.x * light.color.x * intensity * power * Math.pow(Math.max(0.0,  bisector.dot(closestIr.n)), closestIr.material.shinyness);
		blinnPhong.y = closestIr.material.specular.y * light.color.y * intensity * power * Math.pow(Math.max(0.0,  bisector.dot(closestIr.n)), closestIr.material.shinyness);
		blinnPhong.z = closestIr.material.specular.z * light.color.z * intensity * power * Math.pow(Math.max(0.0,  bisector.dot(closestIr.n)), closestIr.material.shinyness);

		finalLight.x = finalLight.x + (float)(diffuse.x + blinnPhong.x); 
		finalLight.y = finalLight.y + (float)(diffuse.y + blinnPhong.y); 
		finalLight.z = finalLight.z + (float)(diffuse.z + blinnPhong.z);
	}
		return finalLight;
	}
	
	//return finalLight;
	
}    

// Compute reflection direction
public Vector3d reflect(Vector3d I, Vector3d N) 
{ 
	Vector3d reflectCoeff = new Vector3d(N);
	double coeff = I.dot(N);
	reflectCoeff.scale(coeff);
	reflectCoeff.scale(2);
	reflectCoeff.sub(I);
	reflectCoeff.negate();
    return reflectCoeff; 
} 

// Compute refraction direction
public Vector3d refract(Vector3d ii, Vector3d nn, double ior) 
{ 
    double cosi = Math.max(-1, Math.min(1, ii.dot(nn))); 
    double etai = 1, etat = ior; 
    Vector3d n = new Vector3d(nn); 
    if (cosi < 0) 
    { 
    	cosi = -cosi; 
    } 
    else 
    { 
    	double temp = etai;
    	etai = etat;
    	etat = temp;
    	n.negate(); 
    } 
    
    double eta = etai / etat; 
    double k = 1 - eta * eta * (1 - cosi * cosi); 
    if (k < 0)
    {
    	Vector3d t = new Vector3d(0, 0, 0);
    	return t;
    }
    else
    {
    	Vector3d refract = new Vector3d(ii);
    	ii.scale(eta);
    	n.scale(eta * cosi - Math.sqrt(k));
    	ii.add(n);
    	return refract;
    }
} 

// Evaluate Fresnel equation (ration of reflected light for a given incident direction and surface normal) 
public double fresnel(Vector3d ii, Vector3d nn, double ior) 
{ 
	double kr = 0;
	double cosi = Math.max(-1, Math.min(1, ii.dot(nn))); 
    double etai = 1, etat = ior; 
    if (cosi > 0) 
    { 
    	double temp = etai;
    	etai = etat;
    	etat = temp; 
    }
    
    double sint = etai / (etat * Math.sqrt(Math.max(0.0, 1 - cosi * cosi)));
    
    if (sint >= 1) 
    { 
        kr = 1.0; 
    } 
    else 
    { 
        double cost = Math.sqrt(Math.max(0.f, 1 - sint * sint)); 
        cosi = Math.abs(cosi); 
        double Rs = ((etat * cosi) - (etai * cost)) / ((etat * cosi) + (etai * cost)); 
        double Rp = ((etai * cosi) - (etat * cost)) / ((etai * cosi) + (etat * cost)); 
        kr = (Rs * Rs + Rp * Rp) / 2.0; 
    }

    
    return kr;   
} 
		


	/**
	 * Shoot a shadow ray in the scene and get the result.
	 * 
            	int r = (int)(255*c.x);
	 * @param result Intersection result from raytracing. 
	 * @param light The light to check for visibility.
	 * @param root The scene node.
	 * @param shadowResult Contains the result of a shadow ray test.
	 * @param shadowRay Contains the shadow ray used to test for visibility.
	 * 
	 * @return True if a point is in shadow, false otherwise. 
	 */
	public boolean inShadow(final IntersectResult result, final Light light, IntersectResult shadowResult, Ray shadowRay) {
		
		// TODO: Objective 5: check for shdows and use it in your lighting computation
		shadowRay.eyePoint = result.p;
		shadowRay.viewDirection = new Vector3d(light.from.x - result.p.x, light.from.y - result.p.y, light.from.z - result.p.z);
		
		shadowRay.viewDirection.normalize();

		Vector3d ass = new Vector3d(1, 1, 1);
		ass.scale(0.0000001);
		shadowRay.eyePoint.add(ass);
		
		for(Intersectable ir: surfaceList) {
			
    		ir.intersect(shadowRay, shadowResult);
    		
    		if(shadowResult.t != Double.POSITIVE_INFINITY && shadowResult.t > 1e-9) {
    			return true;
    		}		
		}
		return false;

		
	}

	public double softShadow(final IntersectResult result, final Light light, IntersectResult shadowResult, Ray shadowRay) {
				
				double inten = 0; //0 would be no shadow, 1 is full intensity
				boolean inShadow = false;
				Random rand = new Random();
				
				if (light.type.equals("area")) {  		
					int numSamples = (int) Math.min(light.areaLightSamples, light.areaHeight * light.areaWidth);
					
					// Loop variables
					Vector3d l = new Vector3d();
					double fullLength = 0;
					
					Point3d lightFrom = new Point3d();
					
					Vector3d err = new Vector3d();
					
					for (int j = 0; j < numSamples; j++) {
						// l = normalize(light.pos - point)
						lightFrom.set(light.from);
						lightFrom.x += rand.nextInt((int) light.areaWidth + 1);
						lightFrom.y += rand.nextInt((int) light.areaHeight + 1);
						l.set(lightFrom);
						l.sub(result.p);
						fullLength = l.length();
						l.normalize();
					
						//this is to avoid roundingerror
						err.set(l);
						err.scale(0.00000000001);
					
						shadowRay.eyePoint.set(result.p);
						shadowRay.eyePoint.add(err);
						shadowRay.viewDirection.set(l);
						
						inShadow = false;
						shadowResult = new IntersectResult();
						for (int i = 0; i < surfaceList.size(); i++) {
							surfaceList.get(i).intersect(shadowRay, shadowResult);
							if (shadowResult.t == Double.POSITIVE_INFINITY) continue;
				    		if (shadowResult.t <= fullLength) {
				    			inShadow = true;
				    			break;
				    		}
						}
						if (!inShadow) inten += 1.0;
					}
					
			    	return inten / (double) numSamples;
				}				
				else { //this computes the normal shadow
					Vector3d l = new Vector3d(light.from);
					l.sub(result.p);
			
					double len = l.length();
					l.normalize();
					
			
					Vector3d error = new Vector3d(l);
					error.scale(0.00000000001);
					
				
					shadowRay.eyePoint.set(result.p);
					shadowRay.eyePoint.add(error);
					shadowRay.viewDirection.set(l);

			    	for (int i = 0; i < surfaceList.size(); i++) {
			    		surfaceList.get(i).intersect(shadowRay, shadowResult);
			    		if (shadowResult.t == Double.POSITIVE_INFINITY) continue;
			    		if (shadowResult.t <= len) {
			    			inShadow = true;
			    			break;
			    		}
			    	}
			    	if (!inShadow) inten += 1.0;
			    	return inten;
				}
			}
		
		// This inShadow function will return an intensity instead of a boolean
//		
//					double intensity = 0; // 0 -> 1, Complete Shadow -> No Shadow
//					boolean inShadow = false;
//					Random rand = new Random();
//					
//					if (light.type.equals("area")) { // Area Lights			
//						// Cap maximum samples!
//						int numSamples = (int) Math.min(light.areaLightSamples, light.areaHeight * light.areaWidth);
//						
//						// Loop variables
//						Vector3d l = new Vector3d();
//						double fullLength = 0;
//						Vector3d error = new Vector3d();
//						Point3d lightFrom = new Point3d();
//						
//						for (int j = 0; j < numSamples; j++) {
//							// l = normalize(light.pos - point)
//							lightFrom.set(light.from);
//							lightFrom.x += rand.nextInt((int) light.areaWidth + 1);
//							lightFrom.y += rand.nextInt((int) light.areaHeight + 1);
//							l.set(lightFrom);
//							l.sub(result.p);
//							fullLength = l.length();
//							l.normalize();
//						
//							// Start a small distance away from surface to avoid shadow rounding errors
//							error.set(l);
//							error.scale(0.00000000001);
//						
//							// shadowRay = (point, light.pos - point)
//							shadowRay.eyePoint.set(result.p);
//							shadowRay.eyePoint.add(error);
//							shadowRay.viewDirection.set(l);
//							
//							inShadow = false;
//							shadowResult = new IntersectResult();
//							for (int i = 0; i < surfaceList.size(); i++) {
//								surfaceList.get(i).intersect(shadowRay, shadowResult);
//								if (shadowResult.t == Double.POSITIVE_INFINITY) continue;
//					    		if (shadowResult.t <= fullLength) {
//					    			inShadow = true;
//					    			break;
//					    		}
//							}
//							if (!inShadow) intensity += 1.0;
//						}
//						//System.out.printf("intensity %f samples %s \n", intensity, numSamples);
//				    	return intensity / (double) numSamples;
//					}
//					
//					else { // Normal Point Light
//						// l = normalize(light.pos - point)
//						Vector3d l = new Vector3d(light.from);
//						l.sub(result.p);
//						double fullLength = l.length();
//						l.normalize();
//						
//						// Start a small distance away from surface to avoid shadow rounding errors
//						Vector3d error = new Vector3d(l);
//						error.scale(0.00000000001);
//						
//						// shadowRay = (point, light.pos - point)
//						shadowRay.eyePoint.set(result.p);
//						shadowRay.eyePoint.add(error);
//						shadowRay.viewDirection.set(l);
//
//				    	for (int i = 0; i < surfaceList.size(); i++) {
//				    		surfaceList.get(i).intersect(shadowRay, shadowResult);
//				    		if (shadowResult.t == Double.POSITIVE_INFINITY) continue;
//				    		if (shadowResult.t <= fullLength) {
//				    			inShadow = true;
//				    			break;
//				    		}
//				    	}
//				    	if (!inShadow) intensity += 1.0;
//				    	return intensity;
//					}
//	}
	
	
}
