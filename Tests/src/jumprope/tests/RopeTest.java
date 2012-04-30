package jumprope.tests;

import javax.vecmath.*;

import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;

import processing.core.*;

import com.bulletphysics.collision.broadphase.*;
import com.bulletphysics.collision.dispatch.*;
import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.dynamics.*;
import com.bulletphysics.dynamics.constraintsolver.*;
import com.bulletphysics.linearmath.*;

public class RopeTest extends PApplet {

	public static final int GROUND_W = 800;
	public static final int GROUND_H = -400;
	public static Vector4f plane = new Vector4f(0.0f, 1.0f, 0.0f, GROUND_H);
	public static Vector4f light = new Vector4f(0.0f, 300.0f, -100.0f, 0);
	
	CollisionDispatcher collisionDispatcher;
	BroadphaseInterface broadphaseInterface;
	ConstraintSolver constraintSolver;
	CollisionConfiguration collisionConf;

	RigidBody groundRigidBody;
	private RigidBody sphereBody1;
	private RigidBody sphereBody2;

	int maxProxies = 1024;
	Vector3f worldAabbMin = new Vector3f(-10000, -10000, -10000);
	Vector3f worldAabbMax = new Vector3f(10000, 10000, 10000);
	DynamicsWorld world;
	
	OscP5 osc;
	NetAddress addr;
	static final String serverIP = "127.0.0.1";
	static final int serverPort = 50000;
	static final String clientIP = "127.0.0.1";
	static final int clientPort = 50001;
	//static final float wiimoteWidth = 512;
	//static final float wiimoteHeight = 384;
	static final float wiimoteWidth = 1024;
	static final float wiimoteHeight = 768;

	public static void main(String args[]) {
		PApplet.main(new String[] { "jumprope.tests.RopeTest" });
	}

	@Override
	public void setup() {
		size(1024, 768, OPENGL);
//		camera(width / 2.0f, height / 2.0f, (height / 2.0f) / tan(PI * 60.0f / 360.0f),
		camera(0.0f, 500, -800,
			0.0f, 0.0f, 0.0f,
			0f, -1f, 0f);
		background(200, 0, 0);

		stroke(0, 0, 255);
//		strokeWeight(3);
		smooth();

		collisionConf = new DefaultCollisionConfiguration();
		broadphaseInterface = new AxisSweep3(worldAabbMin, worldAabbMax, maxProxies);
		collisionDispatcher = new CollisionDispatcher(collisionConf);
		constraintSolver = new SequentialImpulseConstraintSolver();

		world = new DiscreteDynamicsWorld(collisionDispatcher, broadphaseInterface,
			constraintSolver, collisionConf);
		world.setGravity(new Vector3f(0, -1000, 0));

		// ADD STATIC GROUND
		CollisionShape groundShape = new StaticPlaneShape(new Vector3f(plane.x, plane.y, plane.z), GROUND_H);
		Transform tf = new Transform();
		tf.origin.set(new Vector3f(0, -1, 0));
		tf.setRotation(new Quat4f(0, 0, 0, 1));
		DefaultMotionState groundMotionState = new DefaultMotionState(tf);
		RigidBodyConstructionInfo groundCI = new RigidBodyConstructionInfo(0, groundMotionState,
			groundShape, new Vector3f(0, 0, 0));
		RigidBody groundRigidBody = new RigidBody(groundCI);
		world.addRigidBody(groundRigidBody);

		sphereBody1 = addCollisionSphere(new Vector3f(0, 500, 0));
//		sphereBody2 = addCollisionSphere(new Vector3f(0, 600, 0));
		
		rope = new Rope(this, world);
		
//		RagDoll ragDoll = new RagDoll(world, new Vector3f(0f, 0f, 10f), 5f);
		osc = new OscP5(this, clientPort);
		addr = new NetAddress(serverIP, serverPort);
	}

	private RigidBody addCollisionSphere(Vector3f position) {
		CollisionShape fallShape = new SphereShape(50);
		Transform tf = new Transform();
		tf.origin.set(position);
		tf.setRotation(new Quat4f(0, 0, 0, 1));
		DefaultMotionState fallMotionState = new DefaultMotionState(tf);
		float myFallMass = 1;
		Vector3f myFallInertia = new Vector3f(0, 0, 0);
		fallShape.calculateLocalInertia(myFallMass, myFallInertia);
		RigidBodyConstructionInfo fallRigidBodyCI = new RigidBodyConstructionInfo(myFallMass,
			fallMotionState, fallShape, myFallInertia);
		RigidBody body = new RigidBody(fallRigidBodyCI);
		world.addRigidBody(body);
		return body;
	}

	@Override
	public void draw() {
		update();
		
		lights();
//		ambientLight(128, 128, 128);
		directionalLight(128, 128, 128, 0, -1, 0.5f);
		lightSpecular(128, 128, 128);
		background(0x0);
		stroke(255);
		fill(255, 255, 255);

		drawGround();

//		drawBox();

		drawSphere(mousePosToWorldPos());

		world.stepSimulation(1.0f / 60.0f, 8);

		rope.draw();
		drawSphere(sphereBody1);
//		drawSphere(sphereBody2);
	}
	
	private void update() {
		if (mousePressed) rope.setPosition(mousePosToWorldPos());
		else rope.clearPosition();
	}

	private Vector3f mousePosToWorldPos() {
		return new Vector3f(mouseX-width*0.5f, -mouseY+width*0.5f, -300);
	}
	
	private void oscEvent(OscMessage msg) {
		if (msg.checkAddrPattern("/wiimote") && msg.checkTypetag("ff")) {
			float x = msg.get(0).floatValue();
			float y = msg.get(1).floatValue();
			x = (x - 0.5f) * wiimoteWidth;
			y = (y - 0.5f) * wiimoteHeight;
			rope.setPosition(new Vector3f(-x, -y, -300));
		}
	}
	
	// vibrates wiimote for millis milliseconds
	private void vibrateWiimote(int millis) {
		OscMessage msg = new OscMessage("/jumprope");
		msg.add(millis);
		osc.send(msg, addr);
	}
	
	private void drawSphere(RigidBody body) {
		Transform tf = new Transform();
		tf = body.getMotionState().getWorldTransform(tf);

		pushMatrix();
		translate(tf.origin.x, tf.origin.y, tf.origin.z);
		PMatrix trans = getMatrix();
		Quat4f rotQuat = new Quat4f();
		tf.getRotation(rotQuat);
		PMatrix3D rotMatrix = new PMatrix3D();
		MatrixUtils.setRotation(rotMatrix, rotQuat);
		trans.apply(rotMatrix);
		setMatrix(trans);
		sphereDetail(30);
		sphere(50);
		popMatrix();
	}

	private void drawSphere(Vector3f pos) {
//		stroke(255);
		fill(148, 222, 52);
		noStroke();
		pushMatrix();
		translate(pos.x, pos.y, pos.z);
		sphere(10);
		popMatrix();
	}

	private void drawBox() {
		fill(255, 255, 255);
		
		// noStroke();
		pushMatrix();
		translate(400, 400, 0);
		// rotateY(1.25f);
		// rotateX(-0.4f);
		box(100);
		popMatrix();
	}

	private Rope rope;
	
	private void drawGround() {
		beginShape();
		stroke(0x0);
		fill(78, 164, 204);
		vertex(-GROUND_W, GROUND_H, -400);
		vertex(GROUND_W, GROUND_H, -400);
		vertex(GROUND_W, GROUND_H, 400);
		vertex(-GROUND_W, GROUND_H, 400);
		endShape(CLOSE);
	}
}
