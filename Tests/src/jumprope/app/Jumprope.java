package jumprope.app;

import SimpleOpenNI.SimpleOpenNI;
import processing.core.PApplet;
import processing.core.PImage;

@SuppressWarnings("serial")
public class Jumprope extends PApplet {
	
	private KinectTracker kinect;
	private GameModel gameModel;
	
	public void setup() {

		size(screen.width, screen.height, P3D);
		frameRate = 30;
		
		lights();

		kinect = new KinectTracker(this);
		gameModel = new GameModel();

		background(200, 0, 0);
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
	
	public void draw() {
		kinect.update();
		
		background(255);
		
		this.drawCamera(0.5f);
		
		// draw player skeletons
		gameModel.draw(); // TODO refactor
	}
	
	public static void main(String args[]) {
		PApplet.main(new String[] { "--present", "jumprope.app.Jumprope" });
	}
	
	
	public GameModel getModel() {
		return this.gameModel;
	}
	
	public void onPlayerAdded(Player p) {
		this.gameModel.addPlayer(p);
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
