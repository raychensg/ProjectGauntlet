import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.swing.event.MouseInputAdapter;

/* Displays a full screen, with an arc that sweeps in a circle
 */

public class Day5 extends Display{
	private DisplayFrame frame;
	public void init() {
		frame = new Day5Frame();
		setUp(frame);
		frame.init();
	}
}


class Day5Frame extends DisplayFrame {
	//Graphics
	private Graphics2D g2;
	private Dimension dim;	
	private BufferedImage image;
	private Graphics2D imageg2;

	private final int FPS = 60;
	
	//Background
	private final Color BACKGROUND_COLOR = Color.black;	
	private File backgroundFile;
	private BufferedImage backgroundImage = null;
	
	//Audio
	//private File beep = new File("drop1.wav");
	private ArrayList<Arc> arcs = new ArrayList<Arc>();
	private final int MAX_ARCS = 150;
	private int x, y;
	
	public void init() {
		g2 = (Graphics2D) this.getGraphics();
		dim = this.getSize();
		image = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
		imageg2 = image.createGraphics();
		addKeyboard();
		addMouse();
		
		x = dim.width/2;
		y = dim.height/2;
		random();
		run();
	}
	
	public void run() {
		while(true) {
			try {
				Thread.sleep(1000/FPS);
			} catch (InterruptedException e) {}
			action();
			draw();
			g2.drawImage(image, 0, 0, null);
		}
	}
	
	public void action() {
		for(Arc a: arcs) {
			a.step();
		}
	}
	
	public void draw() {
		imageg2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .3f));
		drawBackground(imageg2, dim.width, dim.height, BACKGROUND_COLOR, backgroundFile);
		
		for(Arc a: arcs) {
			a.draw(imageg2);
		}
	}
	
	public void addMouse() {
		MouseInputAdapter mouseListener = new MouseInputAdapter() {
			public void mousePressed(MouseEvent e) {
				x = e.getX();
				y = e.getY();
				random();
				//playAudio(beep, 2000);
			}		
		};
		this.addMouseListener(mouseListener);
		this.addMouseMotionListener(mouseListener);
	}
	
	public void random() {
		arcs = new ArrayList<Arc>();
		int count = (int) (MAX_ARCS*Math.random())/2;
		while (count < MAX_ARCS) {
			arcs.add(new Arc(x, y));
			count++;
		}
	}
}

class Arc{
	private int x, y, theta, radius, width, movement;
	private float alpha, goalAlpha;
	private Color color;
	private final int MAX_RAD = 2000;
	private final int MIN_RAD = 10;
	private final int R_BASE = 205;
	private final int G_BASE = 205;
	private final int B_BASE = 205;
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
	
	public void step() {
		theta+=movement;
		if (theta > 360)
			theta -= 360;
//		radius+=2;
//		alpha = (alpha + goalAlpha)/2;
//		if (alpha < .001) {
//			radius = (int)(Math.random()*(MAX_RAD-MIN_RAD) + MIN_RAD);
//			goalAlpha = (float) Math.random();
//		}
//		if (alpha > goalAlpha - .001)
//			goalAlpha = 0;
	}
	
	public void draw(Graphics2D g2) {
		g2.setColor(color);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha/3));
		
		g2.drawArc(x - radius, y - radius, 2*radius, 2*radius, theta, width);
		g2.drawArc(x - radius, y - radius, 2*radius, 2*radius, theta + 2, width - 4);
		g2.drawArc(x - radius, y - radius, 2*radius, 2*radius, theta + 4, width - 8);
	}
	
}
