import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Character extends GObj {
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
			try {
			    spriteImage = ImageIO.read(spriteFile);
			} catch (IOException e) {
				System.out.println("File format incorrect");
			}
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