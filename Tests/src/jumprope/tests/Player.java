package jumprope.tests;
import java.util.ArrayList;
import java.util.List;

import processing.core.PVector;


public class Player {

	private int id;
	private List<PVector> parts;
	
	public Player(int id) {
		this.id = id;
		parts = new ArrayList<PVector>();
	}
	
	public int getId() {
		return this.id;
	}
	
	public PVector getPartPosition(int partId) {
		return parts.get(partId);
	}
}
