import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

/* Displays a full screen, with ripples that appear when the mouse is clicked or moved.
 */

public class RippleTest extends Display{
	private DisplayFrame frame;
	public void init() {
		frame = new RippleFrame();
		setUp(frame);
		frame.init();
	}
}


class RippleFrame extends DisplayFrame {
	//Audio
	private File drop1 = new File("assets/drop1.wav");
	private File drop2 = new File("assets/drop2.wav");
	private File drop3 = new File("assets/drop3.wav");
	
	//Functionality
	private final int SMALL = 2;
	private final int LARGE = 4*SMALL;
	private final int HUGE = 6*SMALL;
	private final int SPACING = 20;
	private final int VARIANCE = 200;
	private final int RAININESS = 2;
		
	//Data
	private ArrayList<GObj> ripples = new ArrayList<GObj>();
	private ArrayList<GObj> ripplesQueue = new ArrayList<GObj>();
	
	//Background
	private final String BACKGROUND = "assets/kingdra.jpg";
	
	public RippleFrame() {
		setBackground(BACKGROUND, 0.5f);
	}
	
	public void tick() {
		random();
		for (int i = 0; i < ripples.size(); i++) {
			ripples.get(i).tick();
			if (((Ripple) ripples.get(i)).getAlphaValue() < 0.1) {
				ripples.remove(ripples.get(i));
				i--;
			}
		}
		for (GObj r: ripplesQueue) {
			ripples.add(r);
		}
		ripplesQueue = new ArrayList<GObj>();
	}
	
	public void draw() {
		setQueue(ripples);
		super.draw();
	}
	
	public void addMouse() {
		MouseInputAdapter mouseListener = new MouseInputAdapter() {
			int oldX, oldY;
			public void mousePressed(MouseEvent e) {
				ripplesQueue.add(new Ripple(e.getX(), e.getY(), LARGE));
				playAudio(drop3, 2000);
				resetTime();
			}		
			public void mouseReleased(MouseEvent e) {
				if (getTime() > 250) {
					ripplesQueue.add(new Ripple(e.getX(), e.getY(), HUGE));
					playAudio(drop3, 2000);
					resetTime();
				}
			}
			public void mouseMoved(MouseEvent e) {
				if (VARIANCE*Math.random() + SPACING < Math.sqrt((oldX-e.getX())*(oldX-e.getX()) + (oldY-e.getY())*(oldY-e.getY()))) {
					ripplesQueue.add(new Ripple(e.getX(), e.getY(), SMALL));
					if (2*Math.random() > 1)
						playAudio(drop1, 2000);
					else
						playAudio(drop2, 2000);
					oldX = e.getX();
					oldY = e.getY();
				}
			}
			public void mouseDragged(MouseEvent e) {
				if (VARIANCE*Math.random() + SPACING < Math.sqrt((oldX-e.getX())*(oldX-e.getX()) + (oldY-e.getY())*(oldY-e.getY()))) {
					ripplesQueue.add(new Ripple(e.getX(), e.getY(), SMALL));
					if (2*Math.random() > 1)
						playAudio(drop1, 2000);
					else
						playAudio(drop2, 2000);
					oldX = e.getX();
					oldY = e.getY();
				}
			}
		};
		this.addMouseListener(mouseListener);
		this.addMouseMotionListener(mouseListener);
	}
	
	public void random() { //Generates random ripples
		for (int i = 0; i < RAININESS; i++)  {
			if (RAININESS*Math.random() > 10*Math.random()) {
				ripplesQueue.add(new Ripple((int)(Math.random()*getDim().width), (int)(Math.random()*getDim().height), SMALL));
				if (2*Math.random() > 1)
					playAudio(drop1, 2000);
				else
					playAudio(drop2, 2000);
			}
		}
	}
}

class Ripple extends GObj {
	private int x, y, radius, step;
	private float alpha = 1;
	private double alphaStep;
	private Color color;
	
	private final int BRIGHTNESS = 50;
	private final int VARIANCE = 50;
	
	public Ripple(int x, int y, int step) {
		this.x = x;
		this.y = y;
		this.step = step;
		alphaStep = Math.random()/30 + 0.015;
		color = new Color((int)(BRIGHTNESS + VARIANCE*Math.random()), BRIGHTNESS + (int)(VARIANCE*Math.random()), BRIGHTNESS + (int)(VARIANCE*Math.random()));
	}
	
	public void tick() {
		radius += step;
		alpha -= alphaStep;
		if (alpha < 0)
			alpha = 0;
	}
	
	public float getAlphaValue() {
		return alpha;
	}
	
	public void draw(Graphics2D g2) {
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g2.setColor(color);
		g2.drawOval(x - radius,y - radius, 2*radius, 2*radius);
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getRadius() {
		return radius;
	}
}