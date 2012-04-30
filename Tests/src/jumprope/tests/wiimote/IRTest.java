package jumprope.tests.wiimote;

import lll.Loc.Loc;
import lll.wrj4P5.Wrj4P5;
import processing.core.PApplet;

public class IRTest extends PApplet {

	Wrj4P5 wii;

	public static void main(String args[]) {
		PApplet.main(new String[] { "jumprope.tests.wiimote.IRTest" });
	}

	public void setup() {
		size(256, 192);
		wii = new Wrj4P5(this).connect(Wrj4P5.IR);
	}

	public void draw() {
		// if (wii.isConnecting()) return;
		background(0);
		noFill();
		stroke(128);
		noStroke();
		fill(255, 128, 128);
		for (int i = 0; i < 4; i++) {
			Loc p = wii.rimokon.irLights[i];
			if (p.x > 0 && p.y > 0 && p.z > 0) {
				float x = (1f - p.x) * width;
				float y = p.y * height;
				ellipse(x, y, 10, 10);
				println(x + " " + y);
			}
		}
	}
}
