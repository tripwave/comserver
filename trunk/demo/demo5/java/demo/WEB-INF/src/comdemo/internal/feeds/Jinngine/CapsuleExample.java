package comdemo.internal.feeds.Jinngine;

/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jinngine.collision.SAP2;
import jinngine.geometry.Box;
import jinngine.geometry.Sphere;
import jinngine.math.Matrix3;
import jinngine.math.Matrix4;
import jinngine.math.Quaternion;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.DefaultDeactivationPolicy;
import jinngine.physics.DefaultScene;
import jinngine.physics.Scene;
import jinngine.physics.force.Force;
import jinngine.physics.force.GravityForce;
import jinngine.physics.solver.NonsmoothNonlinearConjugateGradient;

import com.thebitstream.comserver.nodes.IConnectionNode;
import comdemo.internal.feeds.Jinngine.body.InteractiveBody;

public class CapsuleExample {
	private final Scene scene;

	private Map<String, Body> bodies;

	private Map<Object, Object> bodiesToRemove;

	private Map<Object, Object> bodiesToAdd;

	private Map<Object, Object> bumps;

	public Map<String, Body> getBodies() {
		return bodies;
	}

	public void setBodies(Map<String, Body> bodies) {
		this.bodies = bodies;
	}

	public CapsuleExample() {

		bodies = new HashMap<String, Body>();
		bodiesToRemove = new HashMap<Object, Object>();
		bodiesToAdd = new HashMap<Object, Object>();
		bumps = new HashMap<Object, Object>();

		// start jinngine 
		scene = new DefaultScene(new SAP2(), new NonsmoothNonlinearConjugateGradient(44),
				new DefaultDeactivationPolicy());
		scene.setTimestep(.08);

		// add boxes to bound the world
		Body floor = new Body("floor", new Box(1500, 20, 1500));
		floor.setPosition(new Vector3(0, -30, 0));
		floor.setFixed(true);

		Body back = new Body("back", new Box(200, 200, 20));
		back.setPosition(new Vector3(0, 0,0));
		back.setFixed(true);

		Body front = new Body("front", new Box(200, 200, 20));
		front.setPosition(new Vector3(0, 0, 50));
		front.setFixed(true);

		Body left = new Body("left", new Box(20, 200, 200));
		left.setPosition(new Vector3(-25, 0, 0));
		left.setFixed(true);

		Body right = new Body("right", new Box(20, 200, 200));
		right.setPosition(new Vector3(25, 0, 0));
		right.setFixed(true);

		// add all to scene
		scene.addBody(floor);
		scene.addBody(back);
		scene.addBody(front);
		scene.addBody(left);
		scene.addBody(right);

	}

	public void removeBody(String id, Map<Object, Object> data) {

		synchronized (bodiesToRemove) {
			bodiesToRemove.put(id, data);
		}

	}

	public void createBody(String id, Map<Object, Object> data) {
		synchronized (bodiesToAdd) {
			bodiesToAdd.put(id, data);
		}
	}

	@SuppressWarnings("unchecked")
	private void doCreate() {
		synchronized (bodiesToAdd) {
			Iterator<Object> keys = bodiesToAdd.keySet().iterator();
			while (keys.hasNext()) {
				String id = keys.next().toString();
				Map<Object, Object> data = (Map<Object, Object>) bodiesToAdd.get(id);
				String geoType = (String) data.get("geometry");
				double x = ((Double) data.get("x"));
				double y = ((Double) data.get("y"));
				double z = ((Double) data.get("z"));

				if (geoType.equals("box")) {

					double sX = ((Integer) data.get("sX"));
					double sY = ((Integer) data.get("sY"));
					double sZ = ((Integer) data.get("sZ"));
					Box boxgeometry = new Box(sX, sY, sZ);
					Body box = new Body(id, boxgeometry);
					box.setPosition(new Vector3(x, y, z));
					scene.addBody(box);
					data.put("body", box);
					box.setAngularVelocity(0, 0, 0);
					GravityForce force = new GravityForce(box);
					InteractiveBody iBody=new InteractiveBody(id,box, force);
					data.put("interactive", iBody);
					scene.addForce(iBody.bump);
					scene.addForce(force);
					scene.addForce(force);
					data.put("gravity", force);
					bodies.put(id, box);

				} else if (geoType.equals("sphere")) {
					double rad = ((Integer) data.get("radius"));
					Sphere sphere = new Sphere(rad);
					Body sphereBody = new Body(id, sphere);
					sphereBody.setPosition(new Vector3(x, y, z));
					scene.addBody(sphereBody);
					data.put("body", sphereBody);
					
					GravityForce force = new GravityForce(sphereBody);
					InteractiveBody iBody=new InteractiveBody(id,sphereBody, force);
					scene.addForce(iBody.bump);
					scene.addForce(force);
					sphereBody.setAngularVelocity(1, 0, 0);
					data.put("interactive", iBody);
					data.put("gravity", force);
					bodies.put(id, sphereBody);
				}
				
				bodiesToAdd.clear();
			}
		}
	}
	public final void quaternionToEuler(final Quaternion q1, final Vector3 euler) {
		double heading, attitude, bank;
		
		double test = q1.v.x * q1.v.y + q1.v.z * q1.s;
		
	//	System.out.println("z:"+q1.v.z+" y:"+q1.v.y+" x:"+ q1.v.x+" s:"+q1.s);
		
		boolean t1=( ( q1.v.z > 0 ) );
		boolean t2=( ( q1.s > 0 ) );
		
		if (test > 0.5 - 1e-7) { // singularity at north pole
			System.out.println("c 1");
			heading = 2 * Math.atan2(q1.v.x, q1.s);
			attitude = Math.PI / 2;
			bank = 0;
		} else if (test < -0.5 + 1e-7) { // singularity at south pole
			System.out.println("c 2");
			heading = -2 * Math.atan2(q1.v.x, q1.s);
			attitude = - Math.PI / 2;
			bank = 0;
		} else {
			
			double sqx = q1.v.x * q1.v.x;
			double sqy = q1.v.y * q1.v.y;
			double sqz = q1.v.z * q1.v.z;
			
			heading = Math.atan2(2 * q1.v.y * q1.s - 2 * q1.v.x * q1.v.z, 1 - 2 * sqy - 2 * sqz);
			
			attitude = Math.asin(2 * test);
			
			bank = Math.atan2(2 * q1.v.x * q1.s - 2 * q1.v.y * q1.v.z, 1 - 2 * sqx - 2 * sqz);
			
			
		
		}
		

		
		bank=bank  *  180 / Math.PI;
		heading=heading * 180 / Math.PI;
		attitude=attitude * 180 / Math.PI;


			System.out.println((test < 0)+" "+t1 +"  "+t2+":"+attitude );
			//attitude=360-attitude;



		
		
		// return values
		euler.x = bank ;
		euler.y = heading ;
		euler.z = attitude ;
		
	}
	/**
	 *  This is wrong...
	 * @param mat
	 * @return
	 */
	public Vector3 getEulerAngles(Matrix4 mat) {

		double lAngleY = Math.asin(mat.a13);
		double lCos = Math.cos(lAngleY);

		lAngleY *= 180 / Math.PI;

		double lTrx = 0;
		double lTry = 0;
		double lAngleX = 0;
		double lAngleZ = 0;

		lTrx = mat.a33 / lCos;
		lTry = mat.a23 / lCos;
		lAngleX = Math.atan2(lTry, lTrx);

		lTrx = mat.a11 / lCos;
		lTry = -mat.a12 / lCos;
		lAngleZ = Math.atan2(lTry, lTrx);

		lAngleX *= 180 / Math.PI;
		lAngleZ *= 180 / Math.PI;

		if (lAngleX < 0)
			lAngleX += 360;
		if (lAngleY < 0)
			lAngleY += 360;
		if (lAngleZ < 0)
			lAngleZ += 360;
		if (lAngleX > 360)
			lAngleX -= 360;
		if (lAngleY > 360)
			lAngleY -= 360;
		if (lAngleZ > 360)
			lAngleZ -= 360;

		return new Vector3(lAngleX, lAngleY, lAngleZ);
	}
	/** this conversion uses conventions as described on page:
	*   http://www.euclideanspace.com/maths/geometry/rotations/euler/index.htm
	*   Coordinate System: right hand
	*   Positive angle: right hand
	*   Order of euler angles: heading first, then attitude, then bank
	*   matrix row column ordering:
	*   [m00 m01 m02]
	*   [m10 m11 m12]
	*   [m20 m21 m22]
	*   
	*   [m11 m12 m13]
	*   [m21 m22 m23]
	*   [m31 m32 m33]*/
	
	public final Vector3 rotate(Matrix3  m) {
		double heading, attitude, bank;
		Vector3 ret= new Vector3();
		// Assuming the angles are in radians.
		if (m.a21 > 0.998) { // singularity at north pole
			heading = Math.atan2(m.a13,m.a33);
			attitude = Math.PI/2;
			bank = 0;
			
			
		}else
		if (m.a32 < -0.998) { // singularity at south pole
			heading = Math.atan2(m.a13,m.a33);
			attitude = -Math.PI/2;
			bank = 0;
			
			
		}else{
		heading = Math.atan2(-m.a31,m.a11);
		bank = Math.atan2(-m.a23,m.a22);
		attitude = Math.asin(m.a21);
		}
		ret.y=heading;
		ret.x=attitude;
		ret.z=bank;
		return ret;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Map<Object, Object>> tick() {

		List<Map<Object, Object>> ret = new ArrayList<Map<Object, Object>>();

		doCreate();
		doDestroy();
		synchronized (bumps) {

			if (bumps.size() > 0) {

				Iterator<Object> bumpKeys = bumps.keySet().iterator();

				while (bumpKeys.hasNext()) {
					String id = bumpKeys.next().toString();
					Body bumped = (Body) bodies.get(id);

					if (bumped != null) {
						
						Map<Object, Object> da = (Map<Object, Object>) bumps.get(id);
						int x = (Integer) da.get("x");
						int y = (Integer) da.get("y");
						int z = (Integer) da.get("z");
						int m = (Integer) da.get("mag");
						InteractiveBody iBody = (InteractiveBody) da.get("interactive");
						iBody.bump.setDirection(new Vector3(x,y,z));
						iBody.bump.setMagnitude( m );
					}
				}
			}
			bumps.clear();
		}
		
		scene.tick();
		
		Iterator<String> keys = bodies.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			Map<Object, Object> b = new HashMap<Object, Object>();
			Body bod = (Body) bodies.get(key);
			Vector3 pos = bod.getPosition();
			Quaternion q =bod.state.orientation;
			b.put("qX", q.v.x);
			b.put("qY", q.v.y);
			b.put("qZ", q.v.z);
			b.put("qW", q.s);
			b.put("x", pos.x);
			b.put("y", pos.y);
			b.put("z", pos.z);
			b.put("id", bod.identifier);
			ret.add(b);
		}

		return ret;
	}

	public void addBump(IConnectionNode id, Map<Object, Object> data) {
		synchronized (bumps) {
			data.put("interactive", id.getNodeData().get("interactive"));
			bumps.put(id.getNodeId(), data);
		}
	}

	@SuppressWarnings("unchecked")
	private void doDestroy() {

		synchronized (bodiesToRemove) {

			Iterator<Object> keys = bodiesToRemove.keySet().iterator();

			while (keys.hasNext()) {
				String key = keys.next().toString();
				Map<Object, Object> data = (Map<Object, Object>) bodiesToRemove.get(key);
				InteractiveBody iBody= (InteractiveBody) data.get("interactive");
				Body body = (Body) data.get("body");
				scene.removeForce((Force) data.get("gravity"));
				scene.removeForce(iBody.bump);
				scene.removeBody(body);
				bodies.remove(key);
			}
			bodiesToRemove.clear();
		}
	}
}
