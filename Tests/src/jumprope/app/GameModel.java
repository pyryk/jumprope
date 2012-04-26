package jumprope.app;

import java.util.ArrayList;
import java.util.List;

public class GameModel {

	private List<Player> players = new ArrayList<Player>();
	
	public List<Player> getPlayers() {
		return players;
	}
	
	public void addPlayer(Player p) {
		this.players.add(p);
	}
	
	public void draw() {
		for (Player p : players) {
			p.drawSkeleton();
		}
	}
}
