package jumprope.tests.wiimote;

import processing.core.*;
import lll.wrj4P5.*;
import lll.Loc.*;

public class WiimoteTest extends PApplet {

	Wrj4P5 wii;
	float x, y, prevax, prevay;
	boolean isConnected = false;

	public static void main(String args[]) {
		PApplet.main(new String[] { "jumprope.tests.wiimote.WiimoteTest" });
	}

	public void setup() {
		size(500, 500, P3D);
		frameRate(60);
		x = y = prevax = prevay = 0;
		wii = new Wrj4P5(this);
		wii.connect();
	}

	public void draw() {
		background(0);
		stroke(255);
		translate(width * 0.5f, height * 0.5f);
		// lights();
		// rotateX((int) (wii.rimokon.senced.x*30+300));
		// rotateY((int) (wii.rimokon.senced.y*30+300));
		// rotateZ((int) (wii.rimokon.senced.z*30+300));
		// box(100,100,100);
		//int sx = (int) (wii.rimokon.senced.x);
		//int sy = (int) (wii.rimokon.senced.y);
		//int sz = (int) (wii.rimokon.senced.z);
		float ax = wii.rimokon.acc.x;
		float ay = wii.rimokon.acc.y;
		float az = wii.rimokon.acc.z;
		//float pitch = wii.rimokon.stablePitch();
		//float roll = wii.rimokon.stableRoll();
		if (wii.rCount >= 1) {
			/*if (!isConnected) {
				float battery = 0.2f;
				int lid = 0;
				boolean[] leds = {false, false, false, false};
				println(wii.rimokon.getBatteryLevel() * 200);
				while (battery < (wii.rimokon.getBatteryLevel() * 200) && lid < 4) {
					leds[lid] = true;
					battery += 0.2;
					++lid;
				}
				wii.rimokon.setLEDs(leds);
				isConnected = true;
			}*/
			//println(sx + " " + sy + " " + sz + " : " + ax + " " + ay + " " + az);
			//println((int) (pitch * 1000) + " " + (int) (roll * 1000));
			//println(ay + " " + az);
			//System.out.format("%5.2f %5.2f%n", ay, az);
			x += (ax - prevax) * 10;
			prevax = ax * 1.0f;
			if ((int)x > 0) {
				--x;
			}
			else if ((int)x < 0) {
				++x;
			}
			float angle = atan2(az, ay);
			float avert = az * sin(angle) + ay * cos(angle) - 9.6113878103f;
			//System.out.format("%5.2f%n", avert - 9.6113878103);
			y += (avert - prevay) * 10;
			prevay = avert * 0.9f;
			if ((int)y > 0) {
				--y;
			}
			else if ((int)y < 0) {
				++y;
			}
			ellipse(x, y, 10, 10);
		}
	}

	/*public void buttonPressed(RimokonEvent evt, int rid) {
		if (evt.wasPressed(RimokonEvent.TWO)) {
			println("2");
			boolean[] leds = {false, false, false, false};
			wii.rimokon.setLED(1, true);
		}
		if (evt.wasPressed(RimokonEvent.ONE)) {
			println("1");
			boolean[] leds = {true, true, true, true};
			wii.rimokon.setLED(2, true);
		}
		if (evt.wasPressed(RimokonEvent.B)) {
			println("B");
			wii.rimokon.vibrateFor(1000);
		}
		if (evt.wasPressed(RimokonEvent.A)) {
			println("A");
			wii.rimokon.stopVibrating();
		}
		if (evt.wasPressed(RimokonEvent.MINUS)) {
			println("Minus");
			wii.rimokon.setVibrationMagnitude(0);
			println("Vibration mag: " + wii.rimokon.getVibrationMagnitude(0));
		}

		if (evt.wasPressed(RimokonEvent.HOME))
			println("Home");
		if (evt.wasPressed(RimokonEvent.LEFT))
			println("Left");
		if (evt.wasPressed(RimokonEvent.RIGHT))
			println("Right");
		if (evt.wasPressed(RimokonEvent.DOWN))
			println("Down");
		if (evt.wasPressed(RimokonEvent.UP))
			println("Up");
		if (evt.wasPressed(RimokonEvent.PLUS)) {
			println("Plus");
			wii.rimokon.setVibrationMagnitude(1);
			println("Vibration mag: " + wii.rimokon.getVibrationMagnitude(1));
		}
	}*/
}
