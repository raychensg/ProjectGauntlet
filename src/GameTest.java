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
	private final String playerSprites = "assets/sprites.png";
	
	public void run() {
		initKeys();
		clearQueue();
		player = new Player(playerSprites, getDim().width/2, getDim().height/2);
		addToQueue(player);
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
	
	public void checkOrientation(int pointerX, int pointerY) {
		Vector v = new Vector(x, y, pointerX, pointerY);
		double a = v.getAngle();
		if (a > 45 && a <= 135)
			orientation = 2;
		else if (a > 135 && a <= 225)
			orientation = 1;
		else if (a > 225 && a <= 315)
			orientation = 0;
		else
			orientation = 3;
	}
	
	public void tick() {
		super.tick();
		int deltaX = 0;
		int deltaY = 0;
		if (keysDown[0]) deltaY -= STEP;
		if (keysDown[2]) deltaY += STEP;
		if (keysDown[1]) deltaX -= STEP;
		if (keysDown[3]) deltaX += STEP;
		if (deltaX != 0 || deltaY != 0) {
			moving = true;
		}
		else {
			moving = false;
			movingTime = 0;
		}
		if (moving) movingTime++;
		if (movingTime >= 4*QUARTER_MOVE) movingTime = 0;
	}
}

class Character extends GObj {
	private String spriteFileName;
	private File spriteFile;
	private BufferedImage spriteImage;
	byte orientation;
	boolean moving;
	int movingTime;
	
	private final byte SPRITE_WIDTH = 30;
	private final byte SPRITE_HEIGHT = 30;
	final int QUARTER_MOVE = 5;
	
	int x, y;
	private int xCorner, yCorner;
	
	public Character(String spriteFileName, int x, int y) {
		this.spriteFileName = spriteFileName;
		this.x = x;
		this.y = y;
		try {
			spriteFile = new File(spriteFileName);
		}
		catch (Exception e) {
			System.out.println(spriteFileName + " failed to load");
		}
	}
	
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
			System.out.println("File failed to load");
		}
		xCorner = orientation*SPRITE_WIDTH;
		if (moving){
			if (movingTime >= 0 && movingTime <= QUARTER_MOVE) yCorner = SPRITE_HEIGHT;
			else if (movingTime >= QUARTER_MOVE && movingTime < 2*QUARTER_MOVE) yCorner = 0;
			else if (movingTime >= 2*QUARTER_MOVE && movingTime < 3*QUARTER_MOVE) yCorner = 2*SPRITE_HEIGHT;
			else if (movingTime >= 3*QUARTER_MOVE && movingTime < 4*QUARTER_MOVE) yCorner = 0;
		}
		else yCorner = 0;
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
		g2.drawImage(spriteImage, x - SPRITE_WIDTH/2, y - SPRITE_HEIGHT/2, x + SPRITE_WIDTH/2, y + SPRITE_HEIGHT/2, xCorner, yCorner, xCorner + SPRITE_WIDTH, yCorner + SPRITE_HEIGHT, null);
	}
	
	public String toString() {
		return spriteFileName;
		//TODO more detailed debugging statement
	}
}