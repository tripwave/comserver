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
import comdemo.internal.feeds.Jinngine.body.InteractiveBody;;

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
