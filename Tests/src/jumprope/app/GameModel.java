package jumprope.app;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3f;

import jumprope.tests.Rope;

import processing.core.PApplet;

public class GameModel {
	
	public static int UP_HEIGHT = 70;
	public static int DOWN_HEIGHT = -300;

	private List<Player> players = new ArrayList<Player>();
	private Rope rope;
	
	private boolean beenUp = false;
	
	private int points;
	
	public GameModel(Rope rope) {
		this.rope = rope;
	}
	
	public List<Player> getPlayers() {
		return players;
	}
	
	public int getPoints() {
		return this.points;
	}
	
	public void addPoint() {
		this.points++;
	}
	
	public void addPlayer(Player p) {
		this.players.add(p);
	}
	
	public void removePlayer(int id) {
		this.players.remove(this.getPlayer(id));
	}
	
	public Player getPlayer(int id) {
		for (Player p : players) {
			if (p.getId() == id) {
				return p;
			}
		}
		return null;
	}
	
	public void update() {
		updatePoints();
		updatePlayers();
	}
	
	private void updatePlayers() {
		for (Player p : players) {
			p.update();
		}
	}

	public void updatePoints() {
		Vector3f centerPos = rope.getCenterPosition();
		
		if (beenUp && centerPos.y <= DOWN_HEIGHT) {
			addPoint();
			System.out.println("Adding one point");
			beenUp = false;
		} else if (centerPos.y >= UP_HEIGHT) {
			beenUp = true;
		}
		
	}
	
	public void draw(PApplet app) {
		for (Player p : players) {
			p.draw(app);
		}
	}
}
