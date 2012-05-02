package jumprope.app;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.Point2PointConstraint;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;

import jumprope.app.Player.Limb;
import jumprope.tests.MatrixUtils;
import jumprope.tests.Rope;

import SimpleOpenNI.SimpleOpenNI;

import processing.core.PApplet;
import processing.core.PMatrix;
import processing.core.PMatrix3D;
import processing.core.PVector;


public class Player {

	private int id;
	private Map<Integer, PVector> parts;
	private List<Limb> limbs;
	private DynamicsWorld world;
	private RigidBody body;
	private float height;
	private Limb head;
	private Limb lfoot;
	private Point2PointConstraint headJoint;
	private Limb rfoot;
	private Point2PointConstraint feetJoint;
	
	public Player(int id, DynamicsWorld world) {
		this.id = id;
		this.world = world;
		this.parts = new HashMap<Integer, PVector>();
		this.limbs = new ArrayList<Limb>(); 
		
		this.addLimbs();
		addCollisionShape(new Vector3f(0,0,0));
	}
	
	public int getId() {
		return this.id;
	}
	
	public void update() {
		PVector pHeadPos = head.getStartPosition();
		PVector pLfootPos = lfoot.getEndPosition();
		PVector pRfootPos = rfoot.getEndPosition();
		
		PVector betweenFeet = PVector.sub(pRfootPos, pLfootPos);
		betweenFeet.scaleTo(0.5f);
		PVector pFeetMidPos = PVector.add(pLfootPos, betweenFeet);
		
		Vector3f headPos = new Vector3f();
		headPos.x = pHeadPos.x;
		headPos.y = pHeadPos.y;
		headPos.z = pHeadPos.z;
		
		Vector3f feetMidPos = new Vector3f();
		feetMidPos.x = pFeetMidPos.x;
		feetMidPos.y = pFeetMidPos.y;
		feetMidPos.z = pFeetMidPos.z;
		
		//float headY = pHeadPos.y;
		float height = PVector.sub(pHeadPos, pFeetMidPos).mag(); //
		//float height = headY - feetMidPos.y;
		
		setBoundingBoxBounds(height, headPos, feetMidPos);
	}
	
	public void draw(PApplet app) {
		drawSkeleton(app);
		drawBox(body, app);
	}
	
	public void drawSkeleton(PApplet app) {
		//tracker.drawSkeleton(this.getId());
		for (Limb l : this.getLimbs()) {
			PVector start = l.getStartPosition();
			PVector end = l.getEndPosition();
			if (start != null && end != null) {
				app.strokeWeight(4);
				app.stroke(0);
				app.line(start.x, start.y, start.z, end.x, end.y, end.z);
				if (l.getEndPart() == SimpleOpenNI.SKEL_LEFT_FOOT) {
					//System.out.println("Limb between (" + start.x + "," + start.y + "," + start.z + ") and (" + end.x + "," + end.y + "," + end.z + ")");
					//System.out.println("Distance between left foot and ground: " + (l.getEndPosition().y - Jumprope.GROUND_H));
				}
				
			} else {
				System.out.println("Limb positions for player " + this.getId()
						+ " have not yet been initialized");
			}
			
		}
	}
	
	private static final float PLAYER_THICKNESS = 40f;
	
	private void addCollisionShape(Vector3f position) {
		height = 350; // 300
		CollisionShape fallShape = new BoxShape(new Vector3f(PLAYER_THICKNESS*0.5f, height*0.5f, PLAYER_THICKNESS*0.5f));
		Transform tf = new Transform();
		tf.origin.set(position);
		tf.setRotation(new Quat4f(0, 0, 0, 1));
		DefaultMotionState fallMotionState = new DefaultMotionState(tf);
		float mass = 1;
		Vector3f myFallInertia = new Vector3f(0, 0, 0);
		fallShape.calculateLocalInertia(mass, myFallInertia);
		RigidBodyConstructionInfo fallRigidBodyCI = new RigidBodyConstructionInfo(mass,
			fallMotionState, fallShape, myFallInertia);
		RigidBody body = new RigidBody(fallRigidBodyCI);
		body.setActivationState(RigidBody.DISABLE_DEACTIVATION);
		body.setDamping(0.0f, 1.0f);
		body.setUserPointer(this);
		world.addRigidBody(body);
		this.body = body;
		
		feetJoint = new Point2PointConstraint(body, new Vector3f(0.0f, -height*0.5f, 0.0f));
		world.addConstraint(feetJoint);
		
		headJoint = new Point2PointConstraint(body, new Vector3f(0.0f, height*0.5f, 0.0f));
		world.addConstraint(headJoint);
	}
	
	private void drawBox(RigidBody body, PApplet g) {
		Transform tf = new Transform();
		tf = body.getMotionState().getWorldTransform(tf);

		g.noStroke();
//		g.stroke(77, 81, 31);
		g.fill(242, 255, 96);
		g.pushMatrix();
		g.translate(tf.origin.x, tf.origin.y, tf.origin.z);
		PMatrix trans = g.getMatrix();
		Quat4f rotQuat = new Quat4f();
		tf.getRotation(rotQuat);
		PMatrix3D rotMatrix = new PMatrix3D();
		MatrixUtils.setRotation(rotMatrix, rotQuat);
		trans.apply(rotMatrix);
		g.setMatrix(trans);
		g.box(PLAYER_THICKNESS, height, PLAYER_THICKNESS);
		g.popMatrix();
	}
	
	public void setBoundingBoxBounds(float height, Vector3f headPos, Vector3f feetPos) {
		//this.height = height;
		//body.setCollisionShape(new BoxShape(
		//	new Vector3f(PLAYER_THICKNESS*0.5f, height, PLAYER_THICKNESS*0.5f)));
		
		//Transform tf = new Transform();
		//tf.origin.set(pos);
		//body.getMotionState().setWorldTransform(tf);
		//body.proceedToTransform(tf);
		//body.activate(true);
		
		//Vector3f headPos = new Vector3f(pos);
		//headPos.y += height*0.5f;
		//headJoint.setPivotA(new Vector3f(0.0f, height*0.5f, 0.0f));
		//feetJoint.setPivotA(new Vector3f(0.0f, -height*0.5f, 0.0f));
		headJoint.setPivotB(headPos);
		feetJoint.setPivotB(feetPos);
	}
	
	public PVector getPartPosition(int partId) {
		return parts.get(partId);
	}
	
	public void setPartPosition(int partId, PVector position) {
		this.parts.put(partId, position);
	}
	
	public List<Limb> getLimbs() {
		return this.limbs;
	}
	
	private void addLimbs() {
		limbs.add(head = new Limb(SimpleOpenNI.SKEL_HEAD, SimpleOpenNI.SKEL_NECK));
		
		limbs.add(new Limb(SimpleOpenNI.SKEL_NECK, SimpleOpenNI.SKEL_LEFT_SHOULDER));
		limbs.add(new Limb(SimpleOpenNI.SKEL_LEFT_SHOULDER, SimpleOpenNI.SKEL_LEFT_ELBOW));
		limbs.add(new Limb(SimpleOpenNI.SKEL_LEFT_ELBOW, SimpleOpenNI.SKEL_LEFT_HAND));
		
		limbs.add(new Limb(SimpleOpenNI.SKEL_NECK, SimpleOpenNI.SKEL_RIGHT_SHOULDER));
		limbs.add(new Limb(SimpleOpenNI.SKEL_RIGHT_SHOULDER, SimpleOpenNI.SKEL_RIGHT_ELBOW));
		limbs.add(new Limb(SimpleOpenNI.SKEL_RIGHT_ELBOW, SimpleOpenNI.SKEL_RIGHT_HAND));
		
		limbs.add(new Limb(SimpleOpenNI.SKEL_LEFT_SHOULDER,	SimpleOpenNI.SKEL_TORSO));
		limbs.add(new Limb(SimpleOpenNI.SKEL_RIGHT_SHOULDER, SimpleOpenNI.SKEL_TORSO));
		
		limbs.add(new Limb(SimpleOpenNI.SKEL_TORSO, SimpleOpenNI.SKEL_LEFT_HIP));
		limbs.add(new Limb(SimpleOpenNI.SKEL_LEFT_HIP, SimpleOpenNI.SKEL_LEFT_KNEE));
		limbs.add(lfoot = new Limb(SimpleOpenNI.SKEL_LEFT_KNEE, SimpleOpenNI.SKEL_LEFT_FOOT));
		
		limbs.add(new Limb(SimpleOpenNI.SKEL_TORSO, SimpleOpenNI.SKEL_RIGHT_HIP));
		limbs.add(new Limb(SimpleOpenNI.SKEL_RIGHT_HIP, SimpleOpenNI.SKEL_RIGHT_KNEE));
		limbs.add(rfoot = new Limb(SimpleOpenNI.SKEL_RIGHT_KNEE, SimpleOpenNI.SKEL_RIGHT_FOOT));
	}
	
	public boolean touches(Rope rope) {
		return false; // TODO
	}
	
	public class Limb {
		private int start;
		private int end;
		
		public Limb(int start, int end) {
			this.start = start;
			this.end = end;
		}
		
		public int getStartPart() {
			return start;
		}
		
		public int getEndPart() {
			return end;
		}
		
		public PVector getStartPosition() {
			PVector startVec = Player.this.getPartPosition(this.start);
			return startVec;
		}
		
		public PVector getEndPosition() {
			PVector endVec = Player.this.getPartPosition(this.end);
			return endVec;
		}
	}
}
