/*
 * Created on Apr 23, 2012
 * @author verkel
 */
package jumprope.tests;

import java.util.*;

import javax.vecmath.*;

import processing.core.*;

import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.dynamics.*;
import com.bulletphysics.dynamics.constraintsolver.Point2PointConstraint;
import com.bulletphysics.linearmath.*;


public class Rope {
	private static final int SEGMENTS_COUNT = 30;
	private static final float SEGMENT_WIDTH = 20f;
	private static final float SEGMENT_THICKNESS = 10f;
	private static final float SEGMENT_GAP = 0.0f;
	private PApplet g;
	private DynamicsWorld world;
	private List<RigidBody> segments = new ArrayList<RigidBody>();
	private Point2PointConstraint openEndJoint;
	private boolean openEndFixed = false;
	
	public Rope(PApplet g, DynamicsWorld world) {
		this.g = g;
		this.world = world;
		
		addRope();
	}
	
	public void update() {
		
	}
	
	public void draw() {
		RigidBody last = null;
		for (RigidBody body : segments) {
			if (last != null) drawShadow(last, body);
			drawBox(body);
			last = body;
		}	}

	public void clearPosition() {
		if (openEndFixed) {
			world.removeConstraint(openEndJoint);
			openEndFixed = false;
		}
	}
	
	public void setPosition(Vector3f position) {
		if (!openEndFixed) {
			world.addConstraint(openEndJoint);
			openEndFixed = true;
		}
		openEndJoint.setPivotB(position);
	}
	
	private void addRope() {
		RigidBody previousSegment = null;
		for (int i = 0; i < SEGMENTS_COUNT; i++) {
			float x = -400f + i * SEGMENT_WIDTH;
			RigidBody segment = addRopeSegment(new Vector3f(x, 0.0f, 0.0f));
			segments.add(segment);
			if (i == 0) hookFirstRopeSegment(segment);
			else joinSegments(previousSegment, segment);
			
			previousSegment = segment;
		}
		
		hookLastRopeSegment(previousSegment);
	}
	
	private void hookFirstRopeSegment(RigidBody segment) {
		Point2PointConstraint joint = new Point2PointConstraint(segment, new Vector3f(-SEGMENT_WIDTH*0.5f+SEGMENT_GAP, 0.0f, 0.0f));
		world.addConstraint(joint);
	}
	
	private void hookLastRopeSegment(RigidBody segment) {
		openEndJoint = new Point2PointConstraint(segment, new Vector3f(SEGMENT_WIDTH*0.5f+SEGMENT_GAP, 0.0f, 0.0f));
//		world.addConstraint(openEndJoint);
	}
	
//	private void joinSegments(RigidBody segment1, RigidBody segment2) {
//		Point2PointConstraint joint = new Point2PointConstraint(segment2, segment1, 
//			new Vector3f(-SEGMENT_WIDTH*0.5f+SEGMENT_GAP, 0.0f, 0.0f), new Vector3f(SEGMENT_WIDTH*0.5f+SEGMENT_GAP, 0.0f, 0.0f));
//		world.addConstraint(joint, true);
//	}
	
	private void joinSegments(RigidBody segment1, RigidBody segment2) {
		Point2PointConstraint joint = new Point2PointConstraint(segment1, segment2, 
			new Vector3f(SEGMENT_WIDTH*0.5f+SEGMENT_GAP, 0.0f, 0.0f), new Vector3f(-SEGMENT_WIDTH*0.5f+SEGMENT_GAP, 0.0f, 0.0f));
		world.addConstraint(joint, true);
	}
	
//	private void joinSegments(RigidBody segment1, RigidBody segment2, float x) {
//		Transform localA = new Transform(), localB = new Transform();
//		localA.setIdentity();
//		localB.setIdentity();
//		localA.origin.set(x, 0.0f, 0f);
//		localB.origin.set(x+1, 0.0f, 0f);
//		Generic6DofConstraint joint = new Generic6DofConstraint(segment1, segment2, localA, localB, false);
//		world.addConstraint(joint, true);
//	}

	private RigidBody addRopeSegment(Vector3f position) {
		CollisionShape shape = new BoxShape(new Vector3f(SEGMENT_WIDTH*0.5f, SEGMENT_THICKNESS*0.5f, SEGMENT_THICKNESS*0.5f));
//		CollisionShape shape = new SphereShape(10);
		Transform tf = new Transform();
		tf.origin.set(position);
		tf.setRotation(new Quat4f(0, 0, 0, 1));
		DefaultMotionState fallMotionState = new DefaultMotionState(tf);
		float mass = 0.5f;
		Vector3f fallInertia = new Vector3f(0, 0, 0);
		shape.calculateLocalInertia(mass, fallInertia);
		RigidBodyConstructionInfo fallRigidBodyCI = new RigidBodyConstructionInfo(mass,
			fallMotionState, shape, fallInertia);
		RigidBody body = new RigidBody(fallRigidBodyCI);
		body.setUserPointer(this);
//		body.setRestitution(1.0f);
		body.setDamping(0.5f, 0.5f);
		body.setInvInertiaDiagLocal(new Vector3f(0.01f,0.01f,0.01f));
//		body.setInvInertiaDiagLocal(new Vector3f(0.001f,0.001f,0.001f));
		world.addRigidBody(body);
		return body;
	}
	
	private void drawBox(RigidBody body) {
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
		g.box(SEGMENT_WIDTH, SEGMENT_THICKNESS, SEGMENT_THICKNESS);
		g.popMatrix();
	}
	
	
	private void drawShadow(RigidBody last, RigidBody body) {
		Transform tf = new Transform();
		tf = last.getMotionState().getWorldTransform(tf);
		Transform tf2 = new Transform();
		tf2 = body.getMotionState().getWorldTransform(tf2);
		g.strokeWeight(10);
		g.stroke(0, 0, 0, 80-60*(400+tf.origin.y)/800); // 50
		g.line(tf.origin.x,RopeTest.GROUND_H+5, tf.origin.z, tf2.origin.x, RopeTest.GROUND_H+5, tf2.origin.z);
	}
	
	private void drawSphere(RigidBody body) {
		Transform tf = new Transform();
		tf = body.getMotionState().getWorldTransform(tf);

		g.pushMatrix();
		g.translate(tf.origin.x, tf.origin.y, tf.origin.z);
		PMatrix trans = g.getMatrix();
		Quat4f rotQuat = new Quat4f();
		tf.getRotation(rotQuat);
		PMatrix3D rotMatrix = new PMatrix3D();
		MatrixUtils.setRotation(rotMatrix, rotQuat);
		trans.apply(rotMatrix);
		g.setMatrix(trans);
		g.sphereDetail(5);
		g.sphere(10);
		g.popMatrix();
	}
	
	public Vector3f getCenterPosition() {
		RigidBody centerSegment = segments.get((segments.size()-1)/2);
		Transform tf = new Transform();
		tf = centerSegment.getMotionState().getWorldTransform(tf);
		return tf.origin;

	}
}
