import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.event.MouseInputAdapter;

/* Displays a full screen, with a moving map and a character that moves smoothly in 8 directions 
 * and interferes correctly with walls.
 */

public class GameTest extends Display{
	private DisplayFrame frame;
	public void init() {
		frame = new GameFrame();
		setUp(frame);
		frame.init();
	}
}

class GameFrame extends DisplayFrame {
	
	//Audio
	private File beep = new File("assets/drop1.wav");
	
	//Keyboard Input
	//private String configFileName = "config.txt";
	//private File configFile;
	private final byte NUM_KEYS = 10;
	private short[] keys = new short[NUM_KEYS];
	private boolean[] keysDown = new boolean[NUM_KEYS];
	
	//Player Data
	private Player player;
	private String playerSprites = "assets/sprites.png";
	
	private Stage stage;
	private String stageFile = "assets/aquabase.png";
	//private String stageDir = "assets/stages/";
	
	public void run() {
		initKeys();
		clearQueue();
		player = new Player(playerSprites, getDim().width/2, getDim().height/2);
		stage = new Stage(stageFile, 423, 270);
		addToQueue(stage);
		addToQueue(player);
		//from stage load enemies;
		super.run();
	}
	
	public void initKeys() {
		//TODO Read from a config file to allow custom keybinding
		keys[0] = 115; //s, down
		keys[2] = 119; //w, up
		keys[1] = 97;  //a, left
		keys[3] = 100; //d, right
		//Shift bound as sprint for convenience
		//keys[8] = 0; //Sprint
		keys[4] = (short) (keys[0] - 32); //S, down sprint
		keys[6] = (short) (keys[0] - 32); //W, up sprint
		keys[5] = (short) (keys[0] - 32);  //A, left sprint
		keys[7] = (short) (keys[0] - 32); //D, right sprint
	}
	
	public void tick() {
		player.setKeysDown(keysDown);
		super.tick();
		//Do Stuff
	}
	
	public void keyRead(char key, boolean state) {
		super.keyRead(key, state);
		//Add Key Commands here
		for (byte i = 0; i < NUM_KEYS; i++) {
			if (keys[i] == key) {
				keysDown[i] = state;
			}
		}
	}
	
	public void addMouse() {
		MouseInputAdapter mouseListener = new MouseInputAdapter() {
			public void mousePressed(MouseEvent e) {
				playAudio(beep, 2500);
				//aimTime = 0;
				//TODO
			}		
			public void mouseReleased(MouseEvent e) {
				//TODO
			}
			public void mouseMoved(MouseEvent e) {
				player.checkOrientation(e.getX(), e.getY());
			}
			public void mouseDragged(MouseEvent e) {
				//TODO
			}
		};
		this.addMouseListener(mouseListener);
		this.addMouseMotionListener(mouseListener);
	}
}

class Vector {
	private float distance;
	private float angle;
	public Vector(int x1, int y1, int x2, int y2) {
		distance = (float) Math.sqrt((x1 - x2)*(x1 - x2) + (y1 - y2) * (y1 - y2));
		angle = (float) Math.toDegrees(Math.atan2(y2 - y1, x1 - x2));
		angle += 180;
		if (angle < 0) angle += 360;
		if (angle > 360) angle = angle%360;
	}
	public float getAngle() {
		return angle;
	}
	public float getDistance() {
		return distance;
	}
}