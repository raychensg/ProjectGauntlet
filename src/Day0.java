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

public class Day0 extends Display{
	private DisplayFrame frame;
	public void init() {
		frame = new Day0Frame();
		setUp(frame);
		frame.init();
	}
}


class Day0Frame extends DisplayFrame {
	//Graphics
	private Graphics2D g2;
	private Dimension dim;	
	private BufferedImage image;
	private Graphics2D imageg2;

	private final int FPS = 60;
	
	//Background
	private final Color BACKGROUND_COLOR = Color.white;	
	private File backgroundFile;
	private BufferedImage backgroundImage = null;
	
	//Audio
	//private File drop1 = new File("drop1.wav");
	//private File drop2 = new File("drop2.wav");
	
	//Functionality
	private final int SMALL = 2;
	private final int LARGE = 4*SMALL;
	private final int HUGE = 6*SMALL;
	private final int SPACING = 20;
	private final int VARIANCE = 200;
	private final int RAININESS = 9;
	private double time;
		
	//Data
	private ArrayList<Ripple> ripples = new ArrayList<Ripple>();
	private ArrayList<Ripple> ripplesQueue = new ArrayList<Ripple>();
	
	public void init() {
		g2 = (Graphics2D) this.getGraphics();
		dim = this.getSize();
		image = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
		imageg2 = image.createGraphics();
		addKeyboard();
		addMouse();
		
		run();
	}
	
	public void run() {
		while(true) {
			try {
				Thread.sleep(1000/FPS);
				time += 1000/FPS;
			} catch (InterruptedException e) {}
			action();
			draw();
			g2.drawImage(image, 0, 0, null);
		}
	}
	
	public void action() {
		random();
		for (int i = 0; i < ripples.size(); i++) {
			ripples.get(i).step();
			if (ripples.get(i).getAlphaValue() < 0.1) {
				ripples.remove(ripples.get(i));
				i--;
			}
		}
		for (Ripple r: ripplesQueue) {
			ripples.add(r);
		}
		ripplesQueue = new ArrayList<Ripple>();
	}
	
	public void draw() {
		imageg2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
		drawBackground(imageg2, dim.width, dim.height, BACKGROUND_COLOR, backgroundFile);

		for (Ripple r: ripples) {
			imageg2.setComposite(r.getAlpha());
			imageg2.setColor(r.getColor());
			r.draw(imageg2);
		}
	}
	
	public void addMouse() {
		MouseInputAdapter mouseListener = new MouseInputAdapter() {
			int oldX, oldY;
			public void mousePressed(MouseEvent e) {
				ripplesQueue.add(new Ripple(e.getX(), e.getY(), LARGE));
				//if (2*Math.random() > 1)
				//	playAudio(drop1, 2000);
				//else
				//	playAudio(drop2, 2000);
				time = 0;
			}		
			public void mouseReleased(MouseEvent e) {
				if (time > 250) {
					ripplesQueue.add(new Ripple(e.getX(), e.getY(), HUGE));
					//if (2*Math.random() > 1)
					//	playAudio(drop1, 2000);
					//else
					//	playAudio(drop2, 2000);
					time = 0;
				}
			}
			public void mouseMoved(MouseEvent e) {
				if (VARIANCE*Math.random() + SPACING < Math.sqrt((oldX-e.getX())*(oldX-e.getX()) + (oldY-e.getY())*(oldY-e.getY()))) {
					ripplesQueue.add(new Ripple(e.getX(), e.getY(), SMALL));
					//if (2*Math.random() > 1)
					//	playAudio(drop1, 2000);
					//else
					//	playAudio(drop2, 2000);
					oldX = e.getX();
					oldY = e.getY();
				}
			}
			public void mouseDragged(MouseEvent e) {
				if (VARIANCE*Math.random() + SPACING < Math.sqrt((oldX-e.getX())*(oldX-e.getX()) + (oldY-e.getY())*(oldY-e.getY()))) {
					ripplesQueue.add(new Ripple(e.getX(), e.getY(), SMALL));
					//if (2*Math.random() > 1)
					//	playAudio(drop1, 2000);
					//else
					//	playAudio(drop2, 2000);
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
				ripplesQueue.add(new Ripple((int)(Math.random()*dim.width), (int)(Math.random()*dim.height), SMALL));
				//if (2*Math.random() > 1)
				//	playAudio(drop1, 2000);
				//else
				//	playAudio(drop2, 2000);
			}
		}
	}
}

class Ripple {
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
	
	public void step() {
		radius += step;
		alpha -= alphaStep;
		if (alpha < 0)
			alpha = 0;
	}
	
	public void draw(Graphics2D g2) {
		g2.drawOval(x - radius,y - radius, 2*radius, 2*radius);
	}
	
	public float getAlphaValue() {
		return alpha;
	}
	
	public Color getColor() {
		return color;
	}
	
	public Composite getAlpha() {
		return AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
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