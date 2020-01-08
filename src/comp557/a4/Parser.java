package comp557.a4;

import java.util.Locale;
import java.util.Scanner;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A factory class to generate raytracer objects from XML definition. 
 */
public class Parser {

	/**
	 * Create a scene.
	 */
	public static Scene createScene(Node dataNode) {
		Scene scene = new Scene();
		Node ambientAttr = dataNode.getAttributes().getNamedItem("ambient");
        if( ambientAttr != null ) {
        	Scanner s = new Scanner( ambientAttr.getNodeValue());
            float x = s.nextFloat();
            float y = s.nextFloat();
            float z = s.nextFloat();
            scene.ambient.set(x, y, z);   
            s.close();
        }
        NodeList nodeList = dataNode.getChildNodes();
        for ( int i = 0; i < nodeList.getLength(); i++ ) {
            Node n = nodeList.item(i);
            // skip all text, just process the ELEMENT_NODEs
            if ( n.getNodeType() != Node.ELEMENT_NODE ) continue;
            String nodeName = n.getNodeName();
            if ( nodeName.equalsIgnoreCase( "material" ) ) {                
                Material material = Parser.createMaterial(n);
                Material.materialMap.put( material.name, material );
            } else if ( nodeName.equalsIgnoreCase( "light" ) ) {                
                Light light = Parser.createLight(n);
                scene.lights.put( light.name, light);
            } else if ( nodeName.equalsIgnoreCase( "render" ) ) {                
                scene.render = Parser.createRender(n);
            } else if ( nodeName.equalsIgnoreCase( "node" ) ) {
            	scene.surfaceList.add( Parser.createSceneNode(n) );
            } else if ( nodeName.equalsIgnoreCase( "plane" ) ) {
        		Plane plane = Parser.createPlane(n);
        		scene.surfaceList.add( plane );
            } else if ( nodeName.equalsIgnoreCase( "box" ) ) {
        		Box box = Parser.createBox(n);
        		scene.surfaceList.add( box );
            } else if ( nodeName.equalsIgnoreCase( "sphere" ) ) {
        		Sphere sphere = Parser.createSphere(n);
        		System.out.println(sphere.velocity);
        		scene.surfaceList.add( sphere );
        		// Add velocity to the class Intersectable from Spheres.
        		int size = scene.surfaceList.size();
        		Intersectable ir = scene.surfaceList.get(size - 1);
        		ir.velocity = sphere.velocity;
        		scene.surfaceList.set(size - 1, ir);
            } else if ( nodeName.equalsIgnoreCase( "metaballs")) {
            	Metaballs metaballs = Parser.createMetaballs( n );
            	scene.surfaceList.add( metaballs );
            } else if ( nodeName.equalsIgnoreCase( "quadric" ) ) {
            	Quadric quadric = Parser.createQuadric(n);
            	scene.surfaceList.add( quadric );
            } else if ( nodeName.equalsIgnoreCase( "mesh" ) ) {
            	Mesh mesh = Parser.createMesh(n);
            	scene.surfaceList.add( mesh );
            }else if ( nodeName.equalsIgnoreCase( "perlinnoise" ) ) {
            	PerlinNoise pn = Parser.createPerlinNoise(n);
            	scene.surfaceList.add( pn );
            }
        }
        return scene;
	}
	
	/**
	 * Create a scenegraph node.
	 */
	public static SceneNode createSceneNode(Node dataNode) {
		SceneNode sceneNode = new SceneNode();
        sceneNode.name = dataNode.getAttributes().getNamedItem("name").getNodeValue();		
		Node refAttr = dataNode.getAttributes().getNamedItem("ref");
		if ( refAttr != null ) {
			// add references to all child nodes and geometries
			//
			SceneNode other = SceneNode.nodeMap.get( refAttr.getNodeValue() );
			if ( other != null ) {
				for (Intersectable s : other.children) {
					sceneNode.children.add(s);
				}
			}
		} else {
	        // create geometries for this node.
			//
	        NodeList nodeList = dataNode.getChildNodes();
	        for ( int i = 0; i < nodeList.getLength(); i++ ) {
	            Node n = nodeList.item(i);
	            // skip all text, just process the ELEMENT_NODEs
	            if ( n.getNodeType() != Node.ELEMENT_NODE ) continue;
	            String nodeName = n.getNodeName();
	        	if ( nodeName.compareToIgnoreCase( "node") == 0 ) {
	                SceneNode childNode = Parser.createSceneNode(n) ;
	                sceneNode.children.add( childNode );
	            } else if ( nodeName.equalsIgnoreCase( "plane" ) ) {
	        		Plane plane = Parser.createPlane(n);
	        		sceneNode.children.add( plane );
	            } else if ( nodeName.equalsIgnoreCase( "box" ) ) {
	        		Box box = Parser.createBox(n);
	        		sceneNode.children.add( box );
	            } else if ( nodeName.equalsIgnoreCase( "sphere" ) ) {
	        		Sphere sphere = Parser.createSphere(n);
	        		System.out.println(sphere.velocity);
	        		sceneNode.children.add( sphere );
	        		// Add velocity to the class Intersectable from Spheres.
	        		int size = sceneNode.children.size();
	        		Intersectable ir = sceneNode.children.get(size - 1);
	        		ir.velocity = sphere.velocity;
	        		sceneNode.children.set(size - 1, ir);
	            } else if ( nodeName.equalsIgnoreCase( "metaballs")) {
	            	Metaballs metaballs = Parser.createMetaballs( n );
	            	sceneNode.children.add( metaballs );
	            } else if ( nodeName.equalsIgnoreCase( "mesh" ) ) {
	            	Mesh mesh = Parser.createMesh(n);
	            	sceneNode.children.add( mesh );
	            } else if ( nodeName.equalsIgnoreCase( "perlinnoise" ) ) {
	            	PerlinNoise pn = Parser.createPerlinNoise(n);
	            	sceneNode.children.add( pn );
	            }
	        }	        
            if ( !SceneNode.nodeMap.containsKey(sceneNode.name) ) {
            	SceneNode.nodeMap.put( sceneNode.name, sceneNode );
            } else {
            	System.err.println("Parser.createSceneNode(): node with name " + sceneNode.name + " already exists!");
            }	        
		}
		
		// Build the scene node transform.
		//
		sceneNode.M.setIdentity();        
		Node translationAttr = dataNode.getAttributes().getNamedItem("translation");
		if( translationAttr != null ) {
        	Scanner s = new Scanner( translationAttr.getNodeValue() );        	
        	s.useLocale(Locale.ENGLISH);
        	double x = s.nextDouble();
        	double y = s.nextDouble();
        	double z = s.nextDouble();
            s.close(); 
        	Vector3d t = new Vector3d(x,y,z);
        	Matrix4d T = new Matrix4d();
        	T.set(t);
        	sceneNode.M.mul(T);
		}		
		Node rotationAttr = dataNode.getAttributes().getNamedItem("rotation");
		if ( rotationAttr != null ) {
        	Scanner s = new Scanner( rotationAttr.getNodeValue() );
        	double degX = s.nextDouble();
        	double degY = s.nextDouble();
        	double degZ = s.nextDouble();
            s.close(); 
        	Matrix4d R = new Matrix4d();
        	R.rotX( Math.toRadians(degX) );
        	sceneNode.M.mul(R);
        	R.rotY( Math.toRadians(degY) );
        	sceneNode.M.mul(R);
        	R.rotZ( Math.toRadians(degZ) );
        	sceneNode.M.mul(R);
		}
		Node scaleAttr = dataNode.getAttributes().getNamedItem("scale");
		if ( scaleAttr != null ) {
            Scanner s = new Scanner( scaleAttr.getNodeValue() );   
            Matrix4d S = new Matrix4d();
            S.setIdentity();
            S.setElement(0,0,s.nextDouble());
            S.setElement(1,1,s.nextDouble());
            S.setElement(2,2,s.nextDouble());
            sceneNode.M.mul( S );
            s.close(); 
		}				
		// cache the inverse matrix, since we only need to compute it once!
		sceneNode.Minv.invert(sceneNode.M);		
		sceneNode.material = parseMaterial(dataNode, "material");					
		return sceneNode;	
	}
	
	/**
	 * Create a light.
	 */
	public static Light createLight(Node dataNode) {
		Light light = new Light();
        light.name = dataNode.getAttributes().getNamedItem("name").getNodeValue();        
        Node colorAttr = dataNode.getAttributes().getNamedItem("color");
        if ( colorAttr != null ) {
        	Scanner s = new Scanner( colorAttr.getNodeValue());
        	float r = s.nextFloat();
            float g = s.nextFloat();
            float b = s.nextFloat();
            float a = (s.hasNextFloat() ? s.nextFloat() : 0);
            light.color.set(r,g,b,a);   
            s.close();    	
        }
        Node fromAttr = dataNode.getAttributes().getNamedItem("from");
        if ( fromAttr != null ) {
        	Scanner s = new Scanner( fromAttr.getNodeValue());
            double x = s.nextDouble();
            double y = s.nextDouble();
            double z = s.nextDouble();
            light.from.set(x,y,z); 
            s.close();
        }
        Node powerAttr = dataNode.getAttributes().getNamedItem("power");
        if ( powerAttr != null ) {
        	light.power = Double.parseDouble( powerAttr.getNodeValue() );
        }
        Node typeAttr = dataNode.getAttributes().getNamedItem("type");
        if ( typeAttr != null ) {
        	light.type = typeAttr.getNodeValue();
        }        
		return light;
	}
	
	/**
	 * Create a camera.
	 */
	public static Camera createCamera(Node dataNode) {
		Camera camera = new Camera();			
		Node nameAttr = dataNode.getAttributes().getNamedItem("name");
		if ( nameAttr != null ) {
			camera.name = nameAttr.getNodeValue();
		}
        Node fromAttr = dataNode.getAttributes().getNamedItem("from");
        if ( fromAttr != null ) {
        	Scanner s = new Scanner( fromAttr.getNodeValue());
            double x = s.nextDouble();
            double y = s.nextDouble();
            double z = s.nextDouble();
            camera.from.set(x,y,z);     
            s.close();
        }
        Node toAttr = dataNode.getAttributes().getNamedItem("to");
        if ( toAttr != null ) {
        	Scanner s = new Scanner( toAttr.getNodeValue());
            double x = s.nextDouble();
            double y = s.nextDouble();
            double z = s.nextDouble();
            camera.to.set(x,y,z);     
            s.close();
        }
        Node upAttr = dataNode.getAttributes().getNamedItem("up");
        if ( upAttr != null ) {
        	Scanner s = new Scanner( upAttr.getNodeValue());
            double x = s.nextDouble();
            double y = s.nextDouble();
            double z = s.nextDouble();
            camera.up.set(x,y,z);
            s.close();
        }
        Node fovAttr = dataNode.getAttributes().getNamedItem("fovy");
        if ( fovAttr != null ) {
            camera.fovy = Double.parseDouble( fovAttr.getNodeValue() );       	
        }
        Node widthAttr = dataNode.getAttributes().getNamedItem("width");
        if ( widthAttr != null ) {
            camera.imageSize.width = Integer.parseInt( widthAttr.getNodeValue() );        	
        }
        Node heightAttr = dataNode.getAttributes().getNamedItem("height");
        if ( heightAttr != null ) {
            camera.imageSize.height = Integer.parseInt( heightAttr.getNodeValue() );        	
        }
        
		return camera;
	}
	
	/**
	 * Create a material.
	 */
	public static Material createMaterial(Node dataNode) {
		Material material;
		Node refAttr = dataNode.getAttributes().getNamedItem("ref");
		if( refAttr  != null ) {
			material = Material.materialMap.get( refAttr.getNodeValue() );
		} else {
			material = new Material();			
	    	Node nameAttr = dataNode.getAttributes().getNamedItem("name");
	    	if ( nameAttr != null ) {
	    		material.name = nameAttr.getNodeValue();
	    	}
	    	Node diffuseAttr = dataNode.getAttributes().getNamedItem("diffuse");
	    	if ( diffuseAttr != null ) {
	        	Scanner s = new Scanner( diffuseAttr.getNodeValue() );
	            float r = s.nextFloat();
	            float g = s.nextFloat();
	            float b = s.nextFloat();
	            float a = (s.hasNextFloat() ? s.nextFloat() : 1);
	            material.diffuse.set(r,g,b,a);
	            s.close();
	    	}
	    	Node specularAttr = dataNode.getAttributes().getNamedItem("specular");
	    	if ( specularAttr != null ) {
	        	Scanner s = new Scanner( specularAttr.getNodeValue());
	            float r = s.nextFloat();
	            float g = s.nextFloat();
	            float b = s.nextFloat();
	            float a = (s.hasNextFloat() ? s.nextFloat() : 1);
	            material.specular.set(r,g,b,a);   
	            s.close();
	    	}
	    	Node hardnessAttr = dataNode.getAttributes().getNamedItem("hardness");
	    	if ( hardnessAttr != null ) {
	    		material.shinyness = Float.parseFloat( hardnessAttr.getNodeValue() );
	    	}
	    	Node reflectiveAttr = dataNode.getAttributes().getNamedItem("reflective");
	    	if ( reflectiveAttr != null ) {
	    		material.reflective = Float.parseFloat( reflectiveAttr.getNodeValue() );
	    	}
	    	Node refIndexAttr = dataNode.getAttributes().getNamedItem("refIndex");
	    	if ( refIndexAttr != null ) {
	    		material.refIndex = Float.parseFloat( refIndexAttr.getNodeValue() );
	    	}
		}
		return material;
	}

	/**
	 * Create a renderer.
	 */
	public static Render createRender(Node dataNode) {
		Render render = new Render();
		Node outputAttr = dataNode.getAttributes().getNamedItem("output");
		if ( outputAttr != null ) {
			render.output = outputAttr.getNodeValue();
		}
		Node bgcolorAttr = dataNode.getAttributes().getNamedItem("bgcolor");
		if ( bgcolorAttr != null ) {
        	Scanner s = new Scanner( bgcolorAttr.getNodeValue());
            float r = s.nextFloat();
            float g = s.nextFloat();
            float b = s.nextFloat();
			render.bgcolor.set(r,g,b);
			s.close();
		}		
		Node samplesAttr = dataNode.getAttributes().getNamedItem("samples");
		if ( samplesAttr != null ) {
        	Scanner s = new Scanner( samplesAttr.getNodeValue());
            render.samples = s.nextInt(); 
			s.close();
		}
    	NodeList nodeList = dataNode.getChildNodes();
    	for (int i = 0; i < nodeList.getLength(); i++) {
    		Node n = nodeList.item(i);
            // skip all text, just process the ELEMENT_NODEs
            if ( n.getNodeType() != Node.ELEMENT_NODE ) continue;
    		String name = n.getNodeName();
			if ( name.equalsIgnoreCase("camera") ) {
				render.camera = Parser.createCamera(n);
    		}
    	}	
		return render;
	}
	
	/**
	 * Create a plane.
	 */
	public static Plane createPlane( Node dataNode ) {
		Plane plane = new Plane();	
		plane.material = parseMaterial(dataNode, "material");	
		plane.material2 = parseMaterial(dataNode, "material2");
		return plane;
	}
	
	
	public static PerlinNoise createPerlinNoise(Node dataNode) {
		//PerlinNoise pn = new PerlinNoise();
		System.out.println("hi");
		Node shapeAttrib = dataNode.getAttributes().getNamedItem("shape");
		String shape = shapeAttrib.getNodeValue();
		PerlinNoise pn = new PerlinNoise();
		
		if(shape.equalsIgnoreCase("box")) {
			Box b = createBox(dataNode);
			pn = new PerlinNoise(b);	
		}else if(shape.equalsIgnoreCase("sphere")) {
			Sphere s = createSphere(dataNode);
			pn = new PerlinNoise(s);
		}else {
			System.out.println("ERROR: You have not correctly written XML for Perlin Noise.");
		}
		
		//pn.material = parseMaterial(dataNode, "material");
		if(pn.material == null) System.out.println("nullmaterial");
		System.out.println(pn.toString());
		return pn;
	}
	
	public static Metaballs createMetaballs( Node dataNode ) {
		Metaballs mb = new Metaballs();
		
		Node threshAttr = dataNode.getAttributes().getNamedItem("threshold");
		if ( threshAttr != null ) {
			mb.thresh = Double.parseDouble( threshAttr.getNodeValue() );
		}
		Node epsAttr = dataNode.getAttributes().getNamedItem("eps");
		if ( epsAttr != null ) {
			mb.eps = Double.parseDouble( epsAttr.getNodeValue() );
		}
		
        NodeList nodeList = dataNode.getChildNodes();
        for ( int i = 0; i < nodeList.getLength(); i++ ) {
            Node n = nodeList.item(i);
            // skip all text, just process the ELEMENT_NODEs
            if ( n.getNodeType() != Node.ELEMENT_NODE ) continue;
            String nodeName = n.getNodeName();
        	
            if ( nodeName.equalsIgnoreCase( "sphere" ) ) {
        		Sphere sphere = Parser.createSphere(n);
        		mb.spheres.add( sphere );
            }
        }
        return mb;
	}
	/**
	 * Create a sphere object.
	 */
	public static Sphere createSphere(Node dataNode) {
		Sphere sphere = new Sphere();
		Node centerAttr = dataNode.getAttributes().getNamedItem("center");
		if ( centerAttr != null ) {
            Scanner s = new Scanner( centerAttr.getNodeValue() );
            double x = s.nextDouble();
            double y = s.nextDouble();
            double z = s.nextDouble();
            sphere.center = new Point3d(x, y, z);
            s.close();
		}
		Node radiusAttr = dataNode.getAttributes().getNamedItem("radius");
		if ( radiusAttr != null ) {
			sphere.radius = Double.parseDouble( radiusAttr.getNodeValue() );
		}
		Node velocityAttr = dataNode.getAttributes().getNamedItem("velocity");
		if ( velocityAttr != null ) {
			sphere.velocity = Double.parseDouble( velocityAttr.getNodeValue() );
		}
		sphere.material = parseMaterial(dataNode, "material");	
    	return sphere;
	}
	
	public static Quadric createQuadric(Node dataNode) {
		Quadric quadric = new Quadric();
		Node attr = dataNode.getAttributes().getNamedItem("Q");
		if ( attr != null ) {
			String coeffs = attr.getNodeValue();
            Scanner s = new Scanner( coeffs );
            for ( int i = 0; i < 4; i++ ) {
            	for ( int j = 0; j < 4; j++ ) {
            		quadric.Q.setElement( i, j, s.nextDouble() );
            	}
            }
            s.close();
            quadric.Q.getRotationScale(quadric.A);
            quadric.B.set( quadric.Q.m03, quadric.Q.m13, quadric.Q.m23 );
            quadric.C = quadric.Q.m33;
		} 
		quadric.material = parseMaterial(dataNode, "material");	
		quadric.material2 = parseMaterial(dataNode, "material2");
		return quadric;
	}
	
	/**
	 * Create a box object.
	 */
	public static Box createBox(Node dataNode) {
		Box box = new Box();
		Node minAttr = dataNode.getAttributes().getNamedItem("min");
		if ( minAttr != null ) {
            Scanner s = new Scanner( minAttr.getNodeValue() );
            double x = s.nextDouble();
            double y = s.nextDouble();
            double z = s.nextDouble();
            box.min = new Point3d(x, y, z);
            s.close();
		}
		Node maxAttr = dataNode.getAttributes().getNamedItem("max");
		if ( maxAttr != null ) {
            Scanner s = new Scanner( maxAttr.getNodeValue() );
            double x = s.nextDouble();
            double y = s.nextDouble();
            double z = s.nextDouble();
            box.max = new Point3d(x, y, z);
            s.close();
		}		
		box.material = parseMaterial(dataNode, "material");
    	return box;
	}	
	
	/**
	 * Create a mesh object.
	 */
	public static Mesh createMesh(Node dataNode) {
        Mesh mesh = new Mesh();
        mesh.name = dataNode.getAttributes().getNamedItem("name").getNodeValue();
        Node filenameAttr = dataNode.getAttributes().getNamedItem("filename");
        if ( filenameAttr != null ) {
        	mesh.soup = new PolygonSoup( filenameAttr.getNodeValue() );
        	if ( !Mesh.meshMap.containsKey(mesh.name) )
        		Mesh.meshMap.put(mesh.name, mesh);
        } else {
			String instance = dataNode.getAttributes().getNamedItem("ref").getNodeValue();
			Mesh other = Mesh.meshMap.get(instance);
			if ( other != null ) {
				mesh.soup = other.soup;
			}
        }
        mesh.material = parseMaterial(dataNode, "material");
    	return mesh;    	
	}

	/**
	 * Utility method to parse a material tag.
	 */
	private static Material parseMaterial(Node dataNode, String tagName) {
		Material material = null;
    	NodeList nodeList = dataNode.getChildNodes();
    	for (int i = 0; i < nodeList.getLength(); i++) {
    		Node n = nodeList.item(i);
            // skip all text, just process the ELEMENT_NODEs
            if ( n.getNodeType() != Node.ELEMENT_NODE ) continue;
    		String name = n.getNodeName();
			if ( name.equalsIgnoreCase(tagName) ) {
    			Node refNode = n.getAttributes().getNamedItem("ref");
    			if( refNode != null ) { 
					material = Material.materialMap.get( refNode.getNodeValue() );
    			} else {
    				material = Parser.createMaterial(n);
    			}
    		}
    	}
    	return material;
	}		
}

//package comp557.a4;
//
//import java.util.Locale;
//import java.util.Scanner;
//
//import javax.vecmath.Matrix4d;
//import javax.vecmath.Point3d;
//import javax.vecmath.Vector3d;
//
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//
///**
// * A factory class to generate raytracer objects from XML definition. 
// */
//public class Parser {
//
//	/**
//	 * Create a scene.
//	 */
//	public static Scene createScene(Node dataNode) {
//		Scene scene = new Scene();
//		Node ambientAttr = dataNode.getAttributes().getNamedItem("ambient");
//        if( ambientAttr != null ) {
//        	Scanner s = new Scanner( ambientAttr.getNodeValue());
//            float x = s.nextFloat();
//            float y = s.nextFloat();
//            float z = s.nextFloat();
//            scene.ambient.set(x, y, z);   
//            s.close();
//        }
//        NodeList nodeList = dataNode.getChildNodes();
//        for ( int i = 0; i < nodeList.getLength(); i++ ) {
//            Node n = nodeList.item(i);
//            // skip all text, just process the ELEMENT_NODEs
//            if ( n.getNodeType() != Node.ELEMENT_NODE ) continue;
//            String nodeName = n.getNodeName();
//            if ( nodeName.equalsIgnoreCase( "material" ) ) {                
//                Material material = Parser.createMaterial(n);
//                Material.materialMap.put( material.name, material );
//            } else if ( nodeName.equalsIgnoreCase( "light" ) ) {                
//                Light light = Parser.createLight(n);
//                scene.lights.put( light.name, light);
//            } else if ( nodeName.equalsIgnoreCase( "render" ) ) {                
//                scene.render = Parser.createRender(n);
//            } else if ( nodeName.equalsIgnoreCase( "node" ) ) {
//            	scene.surfaceList.add( Parser.createSceneNode(n) );
//            } else if ( nodeName.equalsIgnoreCase( "plane" ) ) {
//        		Plane plane = Parser.createPlane(n);
//        		scene.surfaceList.add( plane );
//            } else if ( nodeName.equalsIgnoreCase( "box" ) ) {
//        		Box box = Parser.createBox(n);
//        		scene.surfaceList.add( box );
//            } else if ( nodeName.equalsIgnoreCase( "sphere" ) ) {
//        		Sphere sphere = Parser.createSphere(n);
//        		scene.surfaceList.add( sphere );
//            } else if ( nodeName.equalsIgnoreCase( "metaballs")) {
//            	Metaballs metaballs = Parser.createMetaballs( n );
//            	scene.surfaceList.add( metaballs );
//            } else if ( nodeName.equalsIgnoreCase( "quadric" ) ) {
//            	Quadric quadric = Parser.createQuadric(n);
//            	scene.surfaceList.add( quadric );
//            } else if ( nodeName.equalsIgnoreCase( "mesh" ) ) {
//            	Mesh mesh = Parser.createMesh(n);
//            	scene.surfaceList.add( mesh );
//            }
//        }
//        return scene;
//	}
//	
//	/**
//	 * Create a scenegraph node.
//	 */
//	public static SceneNode createSceneNode(Node dataNode) {
//		SceneNode sceneNode = new SceneNode();
//        sceneNode.name = dataNode.getAttributes().getNamedItem("name").getNodeValue();		
//		Node refAttr = dataNode.getAttributes().getNamedItem("ref");
//		if ( refAttr != null ) {
//			// add references to all child nodes and geometries
//			//
//			SceneNode other = SceneNode.nodeMap.get( refAttr.getNodeValue() );
//			if ( other != null ) {
//				for (Intersectable s : other.children) {
//					sceneNode.children.add(s);
//				}
//			}
//		} else {
//	        // create geometries for this node.
//			//
//	        NodeList nodeList = dataNode.getChildNodes();
//	        for ( int i = 0; i < nodeList.getLength(); i++ ) {
//	            Node n = nodeList.item(i);
//	            // skip all text, just process the ELEMENT_NODEs
//	            if ( n.getNodeType() != Node.ELEMENT_NODE ) continue;
//	            String nodeName = n.getNodeName();
//	        	if ( nodeName.compareToIgnoreCase( "node") == 0 ) {
//	                SceneNode childNode = Parser.createSceneNode(n) ;
//	                sceneNode.children.add( childNode );
//	            } else if ( nodeName.equalsIgnoreCase( "plane" ) ) {
//	        		Plane plane = Parser.createPlane(n);
//	        		sceneNode.children.add( plane );
//	            } else if ( nodeName.equalsIgnoreCase( "box" ) ) {
//	        		Box box = Parser.createBox(n);
//	        		sceneNode.children.add( box );
//	            } else if ( nodeName.equalsIgnoreCase( "sphere" ) ) {
//	        		Sphere sphere = Parser.createSphere(n);
//	        		sceneNode.children.add( sphere );
//	            } else if ( nodeName.equalsIgnoreCase( "metaballs")) {
//	            	Metaballs metaballs = Parser.createMetaballs( n );
//	            	sceneNode.children.add( metaballs );
//	            } else if ( nodeName.equalsIgnoreCase( "mesh" ) ) {
//	            	Mesh mesh = Parser.createMesh(n);
//	            	sceneNode.children.add( mesh );
//	            } else if ( nodeName.equalsIgnoreCase( "perlinnoise" ) ) {
//	            	PerlinNoise pn = Parser.createPerlinNoise(n);
//	            	sceneNode.children.add( pn );
//	            }
//	        }	        
//            if ( !SceneNode.nodeMap.containsKey(sceneNode.name) ) {
//            	SceneNode.nodeMap.put( sceneNode.name, sceneNode );
//            } else {
//            	System.err.println("Parser.createSceneNode(): node with name " + sceneNode.name + " already exists!");
//            }	        
//		}
//		
//		// Build the scene node transform.
//		//
//		sceneNode.M.setIdentity();        
//		Node translationAttr = dataNode.getAttributes().getNamedItem("translation");
//		if( translationAttr != null ) {
//        	Scanner s = new Scanner( translationAttr.getNodeValue() );        	
//        	s.useLocale(Locale.ENGLISH);
//        	double x = s.nextDouble();
//        	double y = s.nextDouble();
//        	double z = s.nextDouble();
//            s.close(); 
//        	Vector3d t = new Vector3d(x,y,z);
//        	Matrix4d T = new Matrix4d();
//        	T.set(t);
//        	sceneNode.M.mul(T);
//		}		
//		Node rotationAttr = dataNode.getAttributes().getNamedItem("rotation");
//		if ( rotationAttr != null ) {
//        	Scanner s = new Scanner( rotationAttr.getNodeValue() );
//        	double degX = s.nextDouble();
//        	double degY = s.nextDouble();
//        	double degZ = s.nextDouble();
//            s.close(); 
//        	Matrix4d R = new Matrix4d();
//        	R.rotX( Math.toRadians(degX) );
//        	sceneNode.M.mul(R);
//        	R.rotY( Math.toRadians(degY) );
//        	sceneNode.M.mul(R);
//        	R.rotZ( Math.toRadians(degZ) );
//        	sceneNode.M.mul(R);
//		}
//		Node scaleAttr = dataNode.getAttributes().getNamedItem("scale");
//		if ( scaleAttr != null ) {
//            Scanner s = new Scanner( scaleAttr.getNodeValue() );   
//            Matrix4d S = new Matrix4d();
//            S.setIdentity();
//            S.setElement(0,0,s.nextDouble());
//            S.setElement(1,1,s.nextDouble());
//            S.setElement(2,2,s.nextDouble());
//            sceneNode.M.mul( S );
//            s.close(); 
//		}				
//		// cache the inverse matrix, since we only need to compute it once!
//		sceneNode.Minv.invert(sceneNode.M);		
//		sceneNode.material = parseMaterial(dataNode, "material");					
//		return sceneNode;	
//	}
//	
//	/**
//	 * Create a light.
//	 */
//	public static Light createLight(Node dataNode) {
//		Light light = new Light();
//        light.name = dataNode.getAttributes().getNamedItem("name").getNodeValue();        
//        Node colorAttr = dataNode.getAttributes().getNamedItem("color");
//        if ( colorAttr != null ) {
//        	Scanner s = new Scanner( colorAttr.getNodeValue());
//        	float r = s.nextFloat();
//            float g = s.nextFloat();
//            float b = s.nextFloat();
//            float a = (s.hasNextFloat() ? s.nextFloat() : 0);
//            light.color.set(r,g,b,a);   
//            s.close();    	
//        }
//        Node fromAttr = dataNode.getAttributes().getNamedItem("from");
//        if ( fromAttr != null ) {
//        	Scanner s = new Scanner( fromAttr.getNodeValue());
//            double x = s.nextDouble();
//            double y = s.nextDouble();
//            double z = s.nextDouble();
//            light.from.set(x,y,z); 
//            s.close();
//        }
//        Node powerAttr = dataNode.getAttributes().getNamedItem("power");
//        if ( powerAttr != null ) {
//        	light.power = Double.parseDouble( powerAttr.getNodeValue() );
//        }
//        Node typeAttr = dataNode.getAttributes().getNamedItem("type");
//        if ( typeAttr != null ) {
//        	light.type = typeAttr.getNodeValue();
//        }        
//		return light;
//	}
//	
//	/**
//	 * Create a camera.
//	 */
//	public static Camera createCamera(Node dataNode) {
//		Camera camera = new Camera();			
//		Node nameAttr = dataNode.getAttributes().getNamedItem("name");
//		if ( nameAttr != null ) {
//			camera.name = nameAttr.getNodeValue();
//		}
//        Node fromAttr = dataNode.getAttributes().getNamedItem("from");
//        if ( fromAttr != null ) {
//        	Scanner s = new Scanner( fromAttr.getNodeValue());
//            double x = s.nextDouble();
//            double y = s.nextDouble();
//            double z = s.nextDouble();
//            camera.from.set(x,y,z);     
//            s.close();
//        }
//        Node toAttr = dataNode.getAttributes().getNamedItem("to");
//        if ( toAttr != null ) {
//        	Scanner s = new Scanner( toAttr.getNodeValue());
//            double x = s.nextDouble();
//            double y = s.nextDouble();
//            double z = s.nextDouble();
//            camera.to.set(x,y,z);     
//            s.close();
//        }
//        Node upAttr = dataNode.getAttributes().getNamedItem("up");
//        if ( upAttr != null ) {
//        	Scanner s = new Scanner( upAttr.getNodeValue());
//            double x = s.nextDouble();
//            double y = s.nextDouble();
//            double z = s.nextDouble();
//            camera.up.set(x,y,z);
//            s.close();
//        }
//        Node fovAttr = dataNode.getAttributes().getNamedItem("fovy");
//        if ( fovAttr != null ) {
//            camera.fovy = Double.parseDouble( fovAttr.getNodeValue() );       	
//        }
//        Node widthAttr = dataNode.getAttributes().getNamedItem("width");
//        if ( widthAttr != null ) {
//            camera.imageSize.width = Integer.parseInt( widthAttr.getNodeValue() );        	
//        }
//        Node heightAttr = dataNode.getAttributes().getNamedItem("height");
//        if ( heightAttr != null ) {
//            camera.imageSize.height = Integer.parseInt( heightAttr.getNodeValue() );        	
//        }
//        
//		return camera;
//	}
//	
//	/**
//	 * Create a material.
//	 */
//	public static Material createMaterial(Node dataNode) {
//		Material material;
//		Node refAttr = dataNode.getAttributes().getNamedItem("ref");
//		if( refAttr  != null ) {
//			material = Material.materialMap.get( refAttr.getNodeValue() );
//		} else {
//			material = new Material();			
//	    	Node nameAttr = dataNode.getAttributes().getNamedItem("name");
//	    	if ( nameAttr != null ) {
//	    		material.name = nameAttr.getNodeValue();
//	    	}
//	    	Node diffuseAttr = dataNode.getAttributes().getNamedItem("diffuse");
//	    	if ( diffuseAttr != null ) {
//	        	Scanner s = new Scanner( diffuseAttr.getNodeValue() );
//	            float r = s.nextFloat();
//	            float g = s.nextFloat();
//	            float b = s.nextFloat();
//	            float a = (s.hasNextFloat() ? s.nextFloat() : 1);
//	            material.diffuse.set(r,g,b,a);
//	            s.close();
//	    	}
//	    	Node specularAttr = dataNode.getAttributes().getNamedItem("specular");
//	    	if ( specularAttr != null ) {
//	        	Scanner s = new Scanner( specularAttr.getNodeValue());
//	            float r = s.nextFloat();
//	            float g = s.nextFloat();
//	            float b = s.nextFloat();
//	            float a = (s.hasNextFloat() ? s.nextFloat() : 1);
//	            material.specular.set(r,g,b,a);   
//	            s.close();
//	    	}
//	    	Node hardnessAttr = dataNode.getAttributes().getNamedItem("hardness");
//	    	if ( hardnessAttr != null ) {
//	    		material.shinyness = Float.parseFloat( hardnessAttr.getNodeValue() );
//	    	}
//		}
//		return material;
//	}
//
//	/**
//	 * Create a renderer.
//	 */
//	public static Render createRender(Node dataNode) {
//		Render render = new Render();
//		Node outputAttr = dataNode.getAttributes().getNamedItem("output");
//		if ( outputAttr != null ) {
//			render.output = outputAttr.getNodeValue();
//		}
//		Node bgcolorAttr = dataNode.getAttributes().getNamedItem("bgcolor");
//		if ( bgcolorAttr != null ) {
//        	Scanner s = new Scanner( bgcolorAttr.getNodeValue());
//            float r = s.nextFloat();
//            float g = s.nextFloat();
//            float b = s.nextFloat();
//			render.bgcolor.set(r,g,b);
//			s.close();
//		}		
//		Node samplesAttr = dataNode.getAttributes().getNamedItem("samples");
//		if ( samplesAttr != null ) {
//        	Scanner s = new Scanner( samplesAttr.getNodeValue());
//            render.samples = s.nextInt(); 
//			s.close();
//		}
//    	NodeList nodeList = dataNode.getChildNodes();
//    	for (int i = 0; i < nodeList.getLength(); i++) {
//    		Node n = nodeList.item(i);
//            // skip all text, just process the ELEMENT_NODEs
//            if ( n.getNodeType() != Node.ELEMENT_NODE ) continue;
//    		String name = n.getNodeName();
//			if ( name.equalsIgnoreCase("camera") ) {
//				render.camera = Parser.createCamera(n);
//    		}
//    	}	
//		return render;
//	}
//	
//	/**
//	 * Create a plane.
//	 */
//	public static Plane createPlane( Node dataNode ) {
//		Plane plane = new Plane();	
//		plane.material = parseMaterial(dataNode, "material");	
//		plane.material2 = parseMaterial(dataNode, "material2");
//		return plane;
//	}
//	
//	
//	public static PerlinNoise createPerlinNoise(Node dataNode) {
//		//PerlinNoise pn = new PerlinNoise();
//		System.out.println("hi");
//		Node shapeAttrib = dataNode.getAttributes().getNamedItem("shape");
//		String shape = shapeAttrib.getNodeValue();
//		PerlinNoise pn = new PerlinNoise();
//		
//		if(shape.equalsIgnoreCase("box")) {
//			Box b = createBox(dataNode);
//			pn = new PerlinNoise(b);	
//		}else if(shape.equalsIgnoreCase("sphere")) {
//			Sphere s = createSphere(dataNode);
//			pn = new PerlinNoise(s);
//		}else {
//			System.out.println("ERROR: You have not correctly written XML for Perlin Noise.");
//		}
//		
//		//pn.material = parseMaterial(dataNode, "material");
//		if(pn.material == null) System.out.println("nullmaterial");
//		System.out.println(pn.toString());
//		return pn;
//	}
//	
//	public static Metaballs createMetaballs( Node dataNode ) {
//		Metaballs mb = new Metaballs();
//		
//		Node threshAttr = dataNode.getAttributes().getNamedItem("threshold");
//		if ( threshAttr != null ) {
//			mb.thresh = Double.parseDouble( threshAttr.getNodeValue() );
//		}
//		Node epsAttr = dataNode.getAttributes().getNamedItem("eps");
//		if ( epsAttr != null ) {
//			mb.eps = Double.parseDouble( epsAttr.getNodeValue() );
//		}
//		
//        NodeList nodeList = dataNode.getChildNodes();
//        for ( int i = 0; i < nodeList.getLength(); i++ ) {
//            Node n = nodeList.item(i);
//            // skip all text, just process the ELEMENT_NODEs
//            if ( n.getNodeType() != Node.ELEMENT_NODE ) continue;
//            String nodeName = n.getNodeName();
//        	
//            if ( nodeName.equalsIgnoreCase( "sphere" ) ) {
//        		Sphere sphere = Parser.createSphere(n);
//        		mb.spheres.add( sphere );
//            }
//        }
//        return mb;
//	}
//	/**
//	 * Create a sphere object.
//	 */
//	public static Sphere createSphere(Node dataNode) {
//		Sphere sphere = new Sphere();
//		Node centerAttr = dataNode.getAttributes().getNamedItem("center");
//		if ( centerAttr != null ) {
//            Scanner s = new Scanner( centerAttr.getNodeValue() );
//            double x = s.nextDouble();
//            double y = s.nextDouble();
//            double z = s.nextDouble();
//            sphere.center = new Point3d(x, y, z);
//            s.close();
//		}
//		Node radiusAttr = dataNode.getAttributes().getNamedItem("radius");
//		if ( radiusAttr != null ) {
//			sphere.radius = Double.parseDouble( radiusAttr.getNodeValue() );
//		}
//		sphere.material = parseMaterial(dataNode, "material");	
//    	return sphere;
//	}
//	
//	public static Quadric createQuadric(Node dataNode) {
//		Quadric quadric = new Quadric();
//		Node attr = dataNode.getAttributes().getNamedItem("Q");
//		if ( attr != null ) {
//			String coeffs = attr.getNodeValue();
//            Scanner s = new Scanner( coeffs );
//            for ( int i = 0; i < 4; i++ ) {
//            	for ( int j = 0; j < 4; j++ ) {
//            		quadric.Q.setElement( i, j, s.nextDouble() );
//            	}
//            }
//            s.close();
//            quadric.Q.getRotationScale(quadric.A);
//            quadric.B.set( quadric.Q.m03, quadric.Q.m13, quadric.Q.m23 );
//            quadric.C = quadric.Q.m33;
//		} 
//		quadric.material = parseMaterial(dataNode, "material");	
//		quadric.material2 = parseMaterial(dataNode, "material2");
//		return quadric;
//	}
//	
//	/**
//	 * Create a box object.
//	 */
//	public static Box createBox(Node dataNode) {
//		Box box = new Box();
//		Node minAttr = dataNode.getAttributes().getNamedItem("min");
//		if ( minAttr != null ) {
//            Scanner s = new Scanner( minAttr.getNodeValue() );
//            double x = s.nextDouble();
//            double y = s.nextDouble();
//            double z = s.nextDouble();
//            box.min = new Point3d(x, y, z);
//            s.close();
//		}
//		Node maxAttr = dataNode.getAttributes().getNamedItem("max");
//		if ( maxAttr != null ) {
//            Scanner s = new Scanner( maxAttr.getNodeValue() );
//            double x = s.nextDouble();
//            double y = s.nextDouble();
//            double z = s.nextDouble();
//            box.max = new Point3d(x, y, z);
//            s.close();
//		}		
//		box.material = parseMaterial(dataNode, "material");
//    	return box;
//	}	
//	
//	/**
//	 * Create a mesh object.
//	 */
//	public static Mesh createMesh(Node dataNode) {
//        Mesh mesh = new Mesh();
//        mesh.name = dataNode.getAttributes().getNamedItem("name").getNodeValue();
//        Node filenameAttr = dataNode.getAttributes().getNamedItem("filename");
//        if ( filenameAttr != null ) {
//        	mesh.soup = new PolygonSoup( filenameAttr.getNodeValue() );
//        	if ( !Mesh.meshMap.containsKey(mesh.name) )
//        		Mesh.meshMap.put(mesh.name, mesh);
//        } else {
//			String instance = dataNode.getAttributes().getNamedItem("ref").getNodeValue();
//			Mesh other = Mesh.meshMap.get(instance);
//			if ( other != null ) {
//				mesh.soup = other.soup;
//			}
//        }
//        mesh.material = parseMaterial(dataNode, "material");
//    	return mesh;    	
//	}
//
//	/**
//	 * Utility method to parse a material tag.
//	 */
//	private static Material parseMaterial(Node dataNode, String tagName) {
//		Material material = null;
//    	NodeList nodeList = dataNode.getChildNodes();
//    	for (int i = 0; i < nodeList.getLength(); i++) {
//    		Node n = nodeList.item(i);
//            // skip all text, just process the ELEMENT_NODEs
//            if ( n.getNodeType() != Node.ELEMENT_NODE ) continue;
//    		String name = n.getNodeName();
//			if ( name.equalsIgnoreCase(tagName) ) {
//    			Node refNode = n.getAttributes().getNamedItem("ref");
//    			if( refNode != null ) { 
//					material = Material.materialMap.get( refNode.getNodeValue() );
//    			} else {
//    				material = Parser.createMaterial(n);
//    			}
//    		}
//    	}
//    	return material;
//	}		
//}