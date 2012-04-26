package jumprope.app;
import java.util.ArrayList;
import java.util.List;

import processing.core.PVector;


public class Player {

	private int id;
	private List<PVector> parts;
	private KinectTracker tracker; 
	
	public Player(int id, KinectTracker tracker) {
		this.id = id;
		parts = new ArrayList<PVector>();
		this.tracker = tracker; 
	}
	
	public int getId() {
		return this.id;
	}
	
	public void drawSkeleton() {
		tracker.drawSkeleton(this.getId());
	}
	
	public PVector getPartPosition(int partId) {
		return parts.get(partId);
	}
}
