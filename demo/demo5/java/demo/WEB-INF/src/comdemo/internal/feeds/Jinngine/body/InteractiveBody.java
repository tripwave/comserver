package comdemo.internal.feeds.Jinngine.body;

import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.force.GravityForce;
import jinngine.physics.force.ImpulseForce;

public class InteractiveBody {
	public String id;
	public Body body;
	public GravityForce gravity ;
	public ImpulseForce bump;

	public InteractiveBody(String name,Body shape,GravityForce grav){
		id=name;
		body=shape;
		gravity=grav;
		bump=new ImpulseForce(body,new Vector3(),new Vector3(),0);	
	}
}
