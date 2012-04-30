package jumprope.tests.wiimoteserver;

import lll.Loc.Loc;
import lll.wrj4P5.Wrj4P5;
import netP5.*;
import oscP5.*;
import processing.core.*;

public class WiimoteServer extends PApplet {

	OscP5 osc;
	NetAddress addr;
	static final String serverIP = "127.0.0.1";
	static final int serverPort = 50000;
	static final String clientIP = "127.0.0.1";
	static final int clientPort = 50001;

	Wrj4P5 wii;
	float x, y;

	public static void main(String args[]) {
		PApplet.main(new String[] { "jumprope.tests.wiimoteserver.WiimoteServer" });
	}

	public void setup() {
		size(200, 100);
		background(0);
		fill(255);
		stroke(255);
		osc = new OscP5(this, serverPort);
		addr = new NetAddress(clientIP, clientPort);
		wii = new Wrj4P5(this);
		wii.connect(Wrj4P5.IR);
	}

	public void draw() {
		background(0);
		if (wii.rCount >= 1) {
			Loc p = wii.rimokon.irLights[0];
			if (p.x > 0 && p.y > 0 && p.z > 0) {
				x = p.x;
				y = p.y;
				sendMsg();
			}
			float battery = wii.rimokon.getBatteryLevel() * 20000;
			String batteryStr = String.format("%6.2f", battery);
			String info = "server IP:     " + serverIP +
						"\nserver port: " + serverPort +
						"\nclient IP:      " + clientIP +
						"\nclient port:  " + clientPort +
						"\n\nwiimote battery: " + batteryStr + "%";
			text(info, 10, 20);
		}
		else {
			text("waiting wiimote...", 10, 20);
		}
	}

	public void sendMsg() {
		// send IR light coordinates
		OscMessage msg = new OscMessage("/wiimote");
		msg.add(x);
		msg.add(y);
		osc.send(msg, addr);
	}
	
	public void oscEvent(OscMessage msg) {
		// vibrate wiiremote
		if (msg.checkAddrPattern("/jumprope") && msg.checkTypetag("i")) {
			int millis = msg.get(0).intValue();
			wii.rimokon.vibrateFor(millis);
		}
	}
}
