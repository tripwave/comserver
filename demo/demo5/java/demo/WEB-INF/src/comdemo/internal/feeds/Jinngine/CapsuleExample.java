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

import jinngine.math.Matrix4;
import jinngine.math.Vector3;
import jinngine.physics.*;
import jinngine.physics.force.Force;
import jinngine.physics.force.GravityForce;
import jinngine.physics.solver.NonsmoothNonlinearConjugateGradient;

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
		scene.setTimestep(0.1);

		// add boxes to bound the world
		Body floor = new Body("floor", new Box(1500, 20, 1500));
		floor.setPosition(new Vector3(0, -30, 0));
		floor.setFixed(true);

		Body back = new Body("back", new Box(200, 200, 20));
		back.setPosition(new Vector3(0, 0, -55));
		back.setFixed(true);

		Body front = new Body("front", new Box(200, 200, 20));
		front.setPosition(new Vector3(0, 0, -7));
		front.setFixed(true);

		Body left = new Body("left", new Box(20, 200, 200));
		left.setPosition(new Vector3(-35, 0, 0));
		left.setFixed(true);

		Body right = new Body("right", new Box(20, 200, 200));
		right.setPosition(new Vector3(35, 0, 0));
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
					box.setAngularVelocity(0, 0, 2);
					GravityForce force = new GravityForce(box);
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
					scene.addForce(force);
					sphereBody.setAngularVelocity(0, 0, 8);
					data.put("gravity", force);
					bodies.put(id, sphereBody);

				}
				
				bodiesToAdd.clear();
			}
		}
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
						Vector3 bpos = bumped.getVelocity();
						Map<Object, Object> da = (Map<Object, Object>) bumps.get(id);
						int x = (Integer) da.get("x");
						int y = (Integer) da.get("y");
						int z = (Integer) da.get("z");

						bpos.add(x, y, z);
						bumped.setVelocity(x + bpos.x, y + bpos.y, z + bpos.z);
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
			Matrix4 mat = bod.getTransform();
			Vector3 rots = getEulerAngles(mat);

			b.put("rX", rots.x);
			b.put("rY", rots.y);
			b.put("rZ", rots.z);

			b.put("x", pos.x);
			b.put("y", pos.y);
			b.put("z", pos.z);
			b.put("id", bod.identifier);

			ret.add(b);
		}

		return ret;

	}

	public void addBump(String id, Map<Object, Object> data) {
		synchronized (bumps) {
			bumps.put(id, data);
		}
	}

	@SuppressWarnings("unchecked")
	private void doDestroy() {

		synchronized (bodiesToRemove) {

			Iterator<Object> keys = bodiesToRemove.keySet().iterator();

			while (keys.hasNext()) {
				String key = keys.next().toString();
				Map<Object, Object> data = (Map<Object, Object>) bodiesToRemove.get(key);
				scene.removeForce((Force) data.get("gravity"));
				Body body = (Body) data.get("body");
				scene.removeBody(body);
				bodies.remove(key);

			}
			bodiesToRemove.clear();
		}
	}
}
