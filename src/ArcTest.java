import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.swing.event.MouseInputAdapter;

/* Displays a full screen, with arcs that sweep in a circle
 */

public class ArcTest extends Display{
	private DisplayFrame frame;
	public void init() {
		frame = new ArcFrame();
		setUp(frame);
		frame.init();
	}
}

class ArcFrame extends DisplayFrame {
	
	//Audio
	private File beep = new File("assets/drop1.wav");
	
	//Data
	private final int MAX_ARCS = 150;
	private int x, y;
	
	//Background
	private final String BACKGROUND = "assets/kingdra.jpg";
	
	public ArcFrame() {
		setBackground(BACKGROUND, 0.1f);
		clearQueue();
	}
	
	public void run() {
		x = getDim().width/2;
		y = getDim().height/2;
		random();
		super.run();
	}
	
	public void keyRead(char key, boolean state) {
		super.keyRead(key, state);
		if (key == 32){ //Java ASCII for Spacebar
			System.out.println("Space!");
		}
	}
	
	public void addMouse() {
		MouseInputAdapter mouseListener = new MouseInputAdapter() {
			public void mousePressed(MouseEvent e) {
				x = e.getX();
				y = e.getY();
				random();
				playAudio(beep, 2000);
			}		
		};
		this.addMouseListener(mouseListener);
		this.addMouseMotionListener(mouseListener);
	}
	
	public void random() {
		clearQueue();
		int count = (int) (MAX_ARCS*Math.random())/2;
		while (count < MAX_ARCS) {
			addToQueue(new Arc(x, y));
			count++;
		}
	}
}

class Arc extends GObj{
	private int x, y, theta, radius, width, movement;
	private float alpha, goalAlpha;
	private Color color;
	private final int MAX_RAD = 2000;
	private final int MIN_RAD = 10;
	private final int R_BASE = 205;
	private final int G_BASE = 105;
	private final int B_BASE = 105;
	private final int R_VARIANCE = 50;
	private final int G_VARIANCE = 50;
	private final int B_VARIANCE = 50;
	
	public Arc(int x, int y) {
		this.x = x;
		this.y = y;
		radius = (int)(Math.random()*(MAX_RAD-MIN_RAD) + MIN_RAD);
		width = (int)(Math.random()*360);	
		while (movement == 0) 
			movement = (int)(Math.random()*20 - 10);
		theta = (int)(Math.random()*360);
		alpha = (float) Math.random();
		color = new Color((int)(Math.random()*R_VARIANCE + R_BASE), (int)(Math.random()*G_VARIANCE + G_BASE), (int)(Math.random()*B_VARIANCE + B_BASE));
	}
	
	public void tick() {
		theta+=movement;
		if (theta > 360)
			theta -= 360;
	}
	
	public void draw(Graphics2D g2) {
		g2.setColor(color);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha/3));
		
		g2.drawArc(x - radius, y - radius, 2*radius, 2*radius, theta, width);
		g2.drawArc(x - radius, y - radius, 2*radius, 2*radius, theta + 2, width - 4);
		g2.drawArc(x - radius, y - radius, 2*radius, 2*radius, theta + 4, width - 8);
	}
	
}
