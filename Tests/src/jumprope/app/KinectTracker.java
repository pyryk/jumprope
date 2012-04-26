package jumprope.app;

import java.util.ArrayList;
import java.util.List;


import SimpleOpenNI.*;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

public class KinectTracker {

	SimpleOpenNI context;
	
	Jumprope app;

	public static boolean KINECT_AVAILABLE = true;
	//public static boolean KINECT_AVAILABLE = false;

	public KinectTracker(Jumprope app) {
		
		this.app = app;

		if (KINECT_AVAILABLE) {
			context = new SimpleOpenNI(app);
		} else {
		}

		// enable depthMap generation
		if (KINECT_AVAILABLE && context.enableDepth() == false) {
			System.err.println("Can't open the depthMap, maybe the camera is not connected!");
			app.exit();
			return;
		}

		if (KINECT_AVAILABLE) {
			context.setMirror(true);
			// enable camera image generation
			context.enableRGB();
			// enable skeletons
			context.enableUser(SimpleOpenNI.SKEL_PROFILE_ALL);
		}
	}
	
	public void update() {
		context.update();
	}
	
	public PImage getImage() {
		if (KINECT_AVAILABLE) {
			return context.rgbImage();
		} else {
			return null;
		}
	}

	public void updatePlayers() {
			// draw skeletons
			// System.out.println("Players: " + this.gameModel.getPlayerCount());
			List<PVector> allHands = new ArrayList<PVector>();
			
			for (Player player : this.app.getModel().getPlayers()) {
				if (KINECT_AVAILABLE
						&& context.isTrackingSkeleton(player.getId())) {
					// System.out.println("Drawing skeleton for player " +
					// player.getId());
					drawSkeleton(player.getId());
					
					PVector leftHand = new PVector();
					PVector rightHand = new PVector();
					context.getJointPositionSkeleton(player.getId(), SimpleOpenNI.SKEL_LEFT_HAND,
							leftHand);
					context.getJointPositionSkeleton(player.getId(), SimpleOpenNI.SKEL_RIGHT_HAND,
							rightHand);
					

				} else if (KINECT_AVAILABLE) {
					System.out.println("Not tracking skeleton for player ");
				}
			}
	}

	public void onNewUser(int userid) {
		System.out.println("Found user " + userid);
		context.startPoseDetection("Psi", userid);
	}

	public void onLostUser(int userid) {
		System.out.println("Lost user " + userid);
		//this.gameModel.removePlayer(userid);
	}

	public void onStartCalibration(int userId) {
		System.out.println("onStartCalibration - userId: " + userId);
	}

	public void onEndCalibration(int userId, boolean successfull) {
		System.out.println("onEndCalibration - userId: " + userId
				+ ", successfull: " + successfull);

		if (successfull) {
			System.out.println("User calibrated !!!");
			context.startTrackingSkeleton(userId);
			this.app.onPlayerAdded(new Player(userId, this));
			System.out.println("User added to players.");
		} else {
			System.out.println("  Failed to calibrate user !!!");
			System.out.println("  Start pose detection");
			context.startPoseDetection("Psi", userId);
		}
	}

	public void onStartPose(String pose, int userId) {
		System.out.println("onStartPose - userId: " + userId + ", pose: " + pose);
		System.out.println(" stop pose detection");

		context.stopPoseDetection(userId);
		context.requestCalibrationSkeleton(userId, true);
	}

	public void onEndPose(String pose, int userId) {
		System.out.println("onEndPose - userId: " + userId + ", pose: " + pose);
	}

	public void drawSkeleton(int userId) {
		// to get the 3d joint data
		/*
		 * PVector jointPos = new PVector();
		 * context.getJointPositionSkeleton(userId
		 * ,SimpleOpenNI.SKEL_NECK,jointPos); System.out.println(jointPos);
		 */

		context.drawLimb(userId, SimpleOpenNI.SKEL_HEAD, SimpleOpenNI.SKEL_NECK);

		context.drawLimb(userId, SimpleOpenNI.SKEL_NECK,
				SimpleOpenNI.SKEL_LEFT_SHOULDER);
		context.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_SHOULDER,
				SimpleOpenNI.SKEL_LEFT_ELBOW);
		context.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_ELBOW,
				SimpleOpenNI.SKEL_LEFT_HAND);

		context.drawLimb(userId, SimpleOpenNI.SKEL_NECK,
				SimpleOpenNI.SKEL_RIGHT_SHOULDER);
		context.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_SHOULDER,
				SimpleOpenNI.SKEL_RIGHT_ELBOW);
		context.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_ELBOW,
				SimpleOpenNI.SKEL_RIGHT_HAND);

		context.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_SHOULDER,
				SimpleOpenNI.SKEL_TORSO);
		context.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_SHOULDER,
				SimpleOpenNI.SKEL_TORSO);

		context.drawLimb(userId, SimpleOpenNI.SKEL_TORSO,
				SimpleOpenNI.SKEL_LEFT_HIP);
		context.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_HIP,
				SimpleOpenNI.SKEL_LEFT_KNEE);
		context.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_KNEE,
				SimpleOpenNI.SKEL_LEFT_FOOT);

		context.drawLimb(userId, SimpleOpenNI.SKEL_TORSO,
				SimpleOpenNI.SKEL_RIGHT_HIP);
		context.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_HIP,
				SimpleOpenNI.SKEL_RIGHT_KNEE);
		context.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_KNEE,
				SimpleOpenNI.SKEL_RIGHT_FOOT);
	}

	public PVector[] getUserHands(int userid) {
		PVector leftHand = new PVector();
		PVector rightHand = new PVector();
		PVector leftHandWorld = new PVector();
		PVector rightHandWorld = new PVector();
		context.getJointPositionSkeleton(userid, SimpleOpenNI.SKEL_LEFT_HAND,
				leftHand);
		context.getJointPositionSkeleton(userid, SimpleOpenNI.SKEL_RIGHT_HAND,
				rightHand);
		leftHand.y = -leftHand.y;
		rightHand.y = -rightHand.y;
		context.convertProjectiveToRealWorld(leftHand, leftHandWorld);
		context.convertProjectiveToRealWorld(rightHand, rightHandWorld);
		// PVector[] hands = { leftHandWorld, rightHandWorld };
		PVector[] hands = { leftHand, rightHand };
		// System.out.println("Left hand " + hands[0]);
		// System.out.println("Right hand " + hands[1]);
		return hands;
	}

}
