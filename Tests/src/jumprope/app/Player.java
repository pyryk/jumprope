package jumprope.app;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jumprope.tests.Rope;

import SimpleOpenNI.SimpleOpenNI;

import processing.core.PApplet;
import processing.core.PVector;


public class Player {

	private int id;
	private Map<Integer, PVector> parts;
	private List<Limb> limbs;
	
	public Player(int id) {
		this.id = id;
		this.parts = new HashMap<Integer, PVector>();
		this.limbs = new ArrayList<Limb>(); 
		
		this.addLimbs();
	}
	
	public int getId() {
		return this.id;
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
				System.out.println("Limb between (" + start.x + "," + start.y + "," + start.z + ") and (" + end.x + "," + end.y + "," + end.z + ")");
			} else {
				System.out.println("Limb positions for player " + this.getId()
						+ " have not yet been initialized");
			}
			
		}
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
		limbs.add(new Limb(SimpleOpenNI.SKEL_HEAD, SimpleOpenNI.SKEL_NECK));
		
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
		limbs.add(new Limb(SimpleOpenNI.SKEL_LEFT_KNEE, SimpleOpenNI.SKEL_LEFT_FOOT));
		
		limbs.add(new Limb(SimpleOpenNI.SKEL_TORSO, SimpleOpenNI.SKEL_RIGHT_HIP));
		limbs.add(new Limb(SimpleOpenNI.SKEL_RIGHT_HIP, SimpleOpenNI.SKEL_RIGHT_KNEE));
		limbs.add(new Limb(SimpleOpenNI.SKEL_RIGHT_KNEE, SimpleOpenNI.SKEL_RIGHT_FOOT));
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
