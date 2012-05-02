package jumprope.app;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;

public class GameModel {

	private List<Player> players = new ArrayList<Player>();
	
	public List<Player> getPlayers() {
		return players;
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
	
	public void draw(PApplet app) {
		for (Player p : players) {
			p.drawSkeleton(app);
		}
	}
}
