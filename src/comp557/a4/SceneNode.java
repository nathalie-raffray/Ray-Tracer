package comp557.a4;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4d;

import comp557.a4.IntersectResult;
import comp557.a4.Intersectable;
import comp557.a4.Ray;

/**
 * The scene is constructed from a hierarchy of nodes, where each node
 * contains a transform, a material definition, some amount of geometry, 
 * and some number of children nodes.  Each node has a unique name so that
 * it can be instanced elsewhere in the hierarchy (provided it does not 
 * make loops. 
 * 
 * Note that if the material (inherited from Intersectable) for a scene 
 * node is non-null, it should override the material of any child.
 * 
 */
public class SceneNode extends Intersectable {
	
	/** Static map for accessing scene nodes by name, to perform instancing */
	public static Map<String,SceneNode> nodeMap = new HashMap<String,SceneNode>();
	
    public String name;
   
    /** Matrix transform for this node */
    public Matrix4d M;
    
    /** Inverse matrix transform for this node */
    public Matrix4d Minv;
    
    /** Child nodes */
    public List<Intersectable> children;
    
    /**
     * Default constructor.
     * Note that all nodes must have a unique name, so that they can used as an instance later on.
     */
    public SceneNode() {
    	super();
    	this.name = "";
    	this.M = new Matrix4d();
    	this.Minv = new Matrix4d();
    	this.children = new LinkedList<Intersectable>();
    }
    
    private IntersectResult tmpResult = new IntersectResult();
    
    private Ray tmpRay = new Ray();
    
    @Override
    public void intersect(Ray ray, IntersectResult result) {
    	tmpRay.eyePoint.set(ray.eyePoint);
    	tmpRay.viewDirection.set(ray.viewDirection);
    	Minv.transform(tmpRay.eyePoint);
    	Minv.transform(tmpRay.viewDirection);    	
    	tmpResult.t = Double.POSITIVE_INFINITY;
    	tmpResult.n.set(0, 0, 1);
    	result.t = Double.POSITIVE_INFINITY;
        for ( Intersectable s : children ) {
            s.intersect( tmpRay, tmpResult );
            
            if(result.t > tmpResult.t && tmpResult.t > 1e-9 && tmpResult.t != Double.POSITIVE_INFINITY) {
            	result.t = tmpResult.t;
            	result.n.set(tmpResult.n);
            	M.transform(result.n);
            	result.n.normalize();
            	result.p.set(tmpResult.p);
            	M.transform(result.p);
            	result.material = tmpResult.material;
            }
            
        }

    }
    
	@Override
  public void intersect( Ray ray, IntersectResult result, double[] time ) {
  	tmpRay.eyePoint.set(ray.eyePoint);
  	tmpRay.viewDirection.set(ray.viewDirection);
  	Minv.transform(tmpRay.eyePoint);
  	Minv.transform(tmpRay.viewDirection);    	
  	tmpResult.t = Double.POSITIVE_INFINITY;
  	tmpResult.n.set(0, 1, 0);
  	int count = 0;
      for ( Intersectable s : children ) {
      	if ((s.material == null)) s.intersect(tmpRay, tmpResult, time);
      	else if ((s.material != null) && s.velocity > 0) s.intersect(tmpRay, tmpResult, time);
      	else s.intersect( tmpRay, tmpResult );

          count++;
          if ( tmpResult.t > 1e-9 && tmpResult.t != Double.POSITIVE_INFINITY && tmpResult.t < result.t ) {
      		// TODO: do something useful here!
      		result.t = tmpResult.t;
      		M.transform(tmpResult.p);
      		result.p.set(tmpResult.p);
      		M.transform(tmpResult.n);
      		tmpResult.n.normalize();
      		result.n.set(tmpResult.n);
      		if (tmpResult.material != null)
      		{
      			result.material = tmpResult.material;
      		}
      	}
      }
	}
}
