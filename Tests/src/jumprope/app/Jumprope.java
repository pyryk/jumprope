package jumprope.app;

import java.awt.Font;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import jumprope.tests.MatrixUtils;
import jumprope.tests.Rope;

import lll.Loc.Loc;
import lll.wrj4P5.Wrj4P5;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.collision.shapes.StaticPlaneShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;

import SimpleOpenNI.SimpleOpenNI;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix;
import processing.core.PMatrix3D;
import processing.core.PVector;

@SuppressWarnings("serial")
public class Jumprope extends PApplet {
	
	public static final int GROUND_W = 800;
	public static final int GROUND_H = -400;
	public static Vector4f plane = new Vector4f(0.0f, 1.0f, 0.0f, GROUND_H);
	public static Vector4f light = new Vector4f(0.0f, 300.0f, -100.0f, 0);
	
	private KinectTracker kinect;
	private GameModel gameModel;
	private Rope rope;
	private int maxProxies = 1024;
	private Vector3f worldAabbMin = new Vector3f(-10000, -10000, -10000);
	private Vector3f worldAabbMax = new Vector3f(10000, 10000, 10000);
	public DynamicsWorld world;
	
	static final float wiimoteWidth = 1024;
	static final float wiimoteHeight = 768;
	
	private RigidBody sphereBody1;
	
	Wrj4P5 wii;
	
	public static final PVector CENTER = new PVector(500,500, 0);
	
	public void setup() {

		size(1024, 768, OPENGL);
		//size(width, height, P3D);
		frameRate = 30;
		//smooth();
		
		camera(0.0f, 500, -800,
				0.0f, 0.0f, 0.0f,
				0f, -1f, 0f);
			background(200, 0, 0);
		
		kinect = new KinectTracker(this);
		

		background(200, 0, 0);
		
		addWorld();
		addGround();
		
		sphereBody1 = addCollisionSphere(new Vector3f(0, 500, 0));
		
		rope = new Rope(this, world);
		gameModel = new GameModel(rope);
		
		setupWiimote();
	}

	private void setupWiimote() {
		wii = new Wrj4P5(this);
		wii.connect(Wrj4P5.IR);
	}

	private void addGround() {
		CollisionShape groundShape = new StaticPlaneShape(new Vector3f(plane.x, plane.y, plane.z), GROUND_H);
		Transform tf = new Transform();
		tf.origin.set(new Vector3f(0, -1, 0));
		tf.setRotation(new Quat4f(0, 0, 0, 1));
		DefaultMotionState groundMotionState = new DefaultMotionState(tf);
		RigidBodyConstructionInfo groundCI = new RigidBodyConstructionInfo(0, groundMotionState,
			groundShape, new Vector3f(0, 0, 0));
		RigidBody groundRigidBody = new RigidBody(groundCI);
		world.addRigidBody(groundRigidBody);
	}

	private void addWorld() {
		CollisionConfiguration collisionConf = new DefaultCollisionConfiguration();
		BroadphaseInterface broadphaseInterface = new AxisSweep3(worldAabbMin, worldAabbMax, maxProxies);
		CollisionDispatcher collisionDispatcher = new CollisionDispatcher(collisionConf);
		ConstraintSolver constraintSolver = new SequentialImpulseConstraintSolver();

		world = new DiscreteDynamicsWorld(collisionDispatcher, broadphaseInterface,
			constraintSolver, collisionConf);
		world.setGravity(new Vector3f(0, -1000, 0));

	}
	
	private void drawCamera(float scale) {
		// draw camera
		PImage rgb = kinect.getImage();
		// rgb.resize(rgb.width/2, rgb.height/2);
		pushMatrix();
		scale(scale);
		translate(50, 150);
		image(rgb, 0, 0);
		popMatrix();
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
	
	private int lastVibrateTime;
	
	public void update() {
		world.stepSimulation(1.0f / 60.0f, 8);
		kinect.update();
		updateRope();
		gameModel.update();
		
		int numManifolds = world.getDispatcher().getNumManifolds();
		for (int i = 0; i < numManifolds; i++) {
			PersistentManifold contactManifold = world.getDispatcher().getManifoldByIndexInternal(i);
			CollisionObject obA = (CollisionObject) contactManifold.getBody0();
			CollisionObject obB = (CollisionObject) contactManifold.getBody1();
			
			if ((obA.getUserPointer() instanceof Rope && obB.getUserPointer() instanceof Player)
					|| (obA.getUserPointer() instanceof Player && obB
							.getUserPointer() instanceof Rope)) {
				
				int numContacts = contactManifold.getNumContacts();
				for (int j = 0; j < numContacts; j++) {
					ManifoldPoint pt = contactManifold.getContactPoint(j);
					if (pt.getDistance() < 0.0f) {
						gameModel.resetPoints();
						if (millis() > lastVibrateTime+500) {
							wii.rimokon.vibrateFor(100);
							lastVibrateTime = millis();
						}
						//System.out.printf("collision between %s and %s\n", obA, obB);
						
					}
				}
			}
		}
	}
	
	public void updateRope() {
		if (mousePressed) {
			rope.setPosition(mousePosToWorldPos());
		}
		//else rope.clearPosition();
		if (wii.rCount >= 1) {
			Loc p = wii.rimokon.irLights[0];
			if (p.x > 0 && p.y > 0 && p.z > 0) {
				float x = p.x;
				float y = p.y;
				//sendMsg();
				x = (x - 0.5f) * wiimoteWidth;
				y = (y - 0.5f) * wiimoteHeight;
				//rope.setPosition(new Vector3f(-x, -y, -300));
				rope.setPosition(new Vector3f(400, -y, -x));
			}
		}
	}
	
	public void draw() {
		update();
		
		setLightsAndColors();
		
		drawGround();
		rope.draw();
		//this.drawCamera(0.5f);
		
		drawDebugBox();
		drawSphere(new Vector3f(0,0,0));
		// draw player skeletons
		gameModel.draw(this); // TODO refactor
		
		drawPoints();
		
		drawSphere(sphereBody1);
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

	private void drawPoints() {
		pushMatrix();
		scale(1f, -1f, 1f);
		textSize(32f);
		textAlign(PApplet.CENTER);
		text(gameModel.getPoints() + " points", 0, -400, 0);
		popMatrix();
		
	}

	private void setLightsAndColors() {
		lights();
		directionalLight(128, 128, 128, 0, -1, 0.5f);
		lightSpecular(128, 128, 128);
		fill(255, 255, 255);
		stroke(255);
		background(255);
		strokeWeight(5);
	}

	private void drawDebugBox() {
		// draw a box to get hunch of the coordinates
		pushMatrix();
		//System.out.println("Box in (" + CENTER.x + ", " + CENTER.y + ", " + CENTER.z + ")");
		translate(CENTER.x,CENTER.y,CENTER.z);
		box(50);
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
	
	private Vector3f mousePosToWorldPos() {
		return new Vector3f(400, -mouseY+height*0.5f, mouseX-width*0.5f);
	}
	
	public static void main(String args[]) {
		PApplet.main(new String[] { /*"--present",*/ "jumprope.app.Jumprope" });
	}
	
	
	public GameModel getModel() {
		return this.gameModel;
	}
	
	public void onPlayerAdded(Player p) {
		this.gameModel.addPlayer(p);
	}
	
	public void onPlayerLost(int userid) {
		this.gameModel.removePlayer(userid);
	}
	
	
	// kinect-related events - forwarding calls to kinect class
	public void onNewUser(int userid) {
		kinect.onNewUser(userid);
	}

	public void onLostUser(int userid) {
		kinect.onLostUser(userid);
	}

	public void onStartCalibration(int userId) {
		kinect.onStartCalibration(userId);
	}

	public void onEndCalibration(int userId, boolean successfull) {
		kinect.onEndCalibration(userId, successfull);
	}

	public void onStartPose(String pose, int userId) {
		kinect.onStartPose(pose, userId);
	}

	public void onEndPose(String pose, int userId) {
		kinect.onEndPose(pose, userId);
	}
	
	// /kinect-related calls
}
