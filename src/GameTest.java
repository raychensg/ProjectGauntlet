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
	
	//Data
	private final byte NUM_KEYS = 10;
	private short[] keys = new short[NUM_KEYS];
	private boolean[] keysDown = new boolean[NUM_KEYS];
	
	private boolean playerCenter = false; //Useful for tight rooms where camera movement is unnecessary
	
	private Player player;
	private final String playerSprites = "assets/sprites.png";
	
	public void run() {
		initKeys();
		queue = new ArrayList<GObj>();
		player = new Player(playerSprites, dim.width/2, dim.height/2);
		queue.add(player);
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
		keysDown = new boolean[NUM_KEYS]; //Clear Keys
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
				Vector v = new Vector(player.getX(), player.getY(), e.getX(), e.getY());
				double a = v.getAngle();
				if (a > 45 && a <= 135)
					player.setOrientation((byte) 2);
				else if (a > 135 && a <= 225)
					player.setOrientation((byte) 1);
				else if (a > 225 && a <= 315)
					player.setOrientation((byte) 0);
				else
					player.setOrientation((byte) 3);
				System.out.println(player._getOrientation());
			}
			public void mouseDragged(MouseEvent e) {
				//TODO
			}
		};
		this.addMouseListener(mouseListener);
		this.addMouseMotionListener(mouseListener);
	}
}

class Player extends Character {
	private final int STEP = 15;
	private boolean[] keysDown;
	public Player(String spriteFile, int x, int y) {
		super(spriteFile, x, y);
		// TODO Auto-generated constructor stub
	}
	
	public void setKeysDown(boolean[] keysDown) {
		this.keysDown = keysDown;
	}
	
	public void tick() {
		int deltaX = 0;
		int deltaY = 0;
		if (keysDown[0]) deltaY -= STEP;
		if (keysDown[2]) deltaY += STEP;
		if (keysDown[1]) deltaX -= STEP;
		if (keysDown[3]) deltaX += STEP;
		if (deltaX != 0 || deltaY != 0) moving = true;
		else moving = false;
		x += deltaX;
		y -= deltaY;
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
		System.out.println(angle);
		return angle;
	}
	public float getDistance() {
		return distance;
	}
}

class Character extends GObj {
	private boolean alive = true;
	private File spriteFile;
	private BufferedImage spriteImage;
	private byte orientation;
	boolean moving;
	
	private final byte SPRITE_WIDTH = 30;
	private final byte SPRITE_HEIGHT = 30;
	
	int x, y;
	private int xCorner, yCorner;
	
	public Character(String spriteFileName, int x, int y) {
		this.x = x;
		this.y = y;
		try {
			spriteFile = new File(spriteFileName);
		}
		catch (Exception e) {
			System.out.println(spriteFileName + " failed to load");
		}
	}
	
	public void tick() {}
	
	public void setOrientation(byte o) {
		orientation = o;
	}
	
	public int _getOrientation() {return orientation;}	
	
	public int getX() {return x;}
	public int getY() {return y;}
	
	public void draw(Graphics2D g2) {
		try {
		    spriteImage = ImageIO.read(spriteFile);
		} catch (IOException e) {
			System.out.println();
		}
		xCorner = orientation*SPRITE_WIDTH;
		if (moving){
			yCorner = yCorner + SPRITE_HEIGHT;
		}
		else yCorner = 0;
		if (yCorner > 3*SPRITE_HEIGHT) yCorner = 0;
		g2.drawImage(spriteImage, x - SPRITE_WIDTH/2, y - SPRITE_HEIGHT/2, x + SPRITE_WIDTH/2, y + SPRITE_HEIGHT, xCorner, yCorner, xCorner + SPRITE_WIDTH, yCorner + SPRITE_HEIGHT, null);
	}
	
	public void kill() {
		alive = false;
	}
}