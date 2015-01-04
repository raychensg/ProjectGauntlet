import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.event.MouseInputAdapter;

public class Display extends JApplet {
	private DisplayFrame frame;
	public void init() {
		frame = new DisplayFrame();
		setUp(frame);
		frame.init();
	}
	
	public void setUp(JFrame frame) {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setUndecorated(true);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setMinimumSize(new Dimension(800, 600));
		frame.setVisible(true);
	}
}

class DisplayFrame extends JFrame {
	private Graphics2D g2;
	private BufferedImage image;
	private Graphics2D imageg2;
	private Dimension dim;
	
	private final int FPS = 60;
	private double time;
	
	private final boolean PRESSED = true;
	private final boolean RELEASED = false;
	
	
	//Background
	private final Color BACKGROUND_COLOR = Color.black;
	private float bgAlpha;
	private File backgroundFile;
	private String bgFileName;
	private BufferedImage backgroundImage;
	
	//Audio
	private AudioThread audio;
	
	//Data
	private ArrayList<GObj> queue = new ArrayList<GObj>();
	
	public void init() {
		initGraphics();
		addKeyboard();
		addMouse();
		run();
	}
	
	public void initGraphics() {
		g2 = (Graphics2D) this.getGraphics();
		dim = this.getSize();
		image = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
		imageg2 = image.createGraphics();
	}
	
	public void setBackground(String fileName, float alpha) {
		bgAlpha = alpha;
		bgFileName = fileName;
		try {
			backgroundFile = new File(this.bgFileName);
		}
		catch (Exception e) {
			backgroundFile = null;
		}
	}
	
	public Dimension getDim() {return dim;}
	public double getTime() {return time;}
	public void resetTime() { time = 0;}
	public void addToQueue(GObj o) { queue.add(o);}
	public void setQueue(ArrayList<GObj> q) { queue = q;}
	public void clearQueue() { queue = new ArrayList<GObj>();}
	
	public void run() {		
		while(true) {
			try {
				Thread.sleep(1000/FPS);
				time += 1000/FPS;
			} catch (InterruptedException e) {}
			tick();
			draw();
		g2.drawImage(image, 0, 0, null);
	}}
	
	public void tick() {
		for (GObj o : queue) {
			try {
				o.tick();
			}
			catch (Exception e) {
				System.out.println("Concurrent  Modification: " + e);
			}
		}
	}
	
	public void draw() {
		drawBackground(imageg2, dim.width, dim.height, BACKGROUND_COLOR, backgroundFile);
		
		for (GObj o : queue) {
			try {
				o.draw(imageg2);
			}
			catch (Exception e){
				System.out.println(e);
			};
		}
	}
	
	public void addKeyboard() {
		this.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				keyRead(e.getKeyChar(), PRESSED);
			}
			public void keyReleased(KeyEvent e) {
				keyRead(e.getKeyChar(), RELEASED);
			}
			public void keyTyped(KeyEvent e) {}
		});
	}
	
	public void addMouse() {
		MouseInputAdapter mouseListener = new MouseInputAdapter() {};
		this.addMouseListener(mouseListener);
		this.addMouseMotionListener(mouseListener);
	}
	
	public void keyRead(char key, boolean state) {
		if (key == 27){ //27 is the Java ASCII for esc
			WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
	        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
		}
	}
	
	public void drawBackground(Graphics2D g2, int width, int height, Color color, File file) {
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bgAlpha));
		if (file == null) {
			g2.setColor(color);
			g2.fillRect(0, 0, width, height);
		} else {
			try {
			    backgroundImage = ImageIO.read(file);
			} catch (IOException e) {
				g2.setColor(color);
				g2.fillRect(0, 0, width, height);
			}
			g2.drawImage(backgroundImage, 0, 0, width, height, 0, 0, width, height, null);
		}
	}
	
	public void playAudio(File file, int time) { //Plays an audio file for a specified time in milliseconds
		audio = new AudioThread(file, time);
		try {
			audio.start();
		}
		catch (Exception e) {
			//Logger info
		}
		audio = null;
	}
}

class GObj {
	public void tick() {}
	public void draw(Graphics2D g2) {};
	
}

class AudioThread extends Thread {
	private File soundFile;
	private AudioInputStream audioInputStream;
	private AudioFormat	audioFormat;
	private SourceDataLine line;
	private DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
	private int time = 1000;
	
	public AudioThread(File file, int time) {
		super();
		this.soundFile = file;
		this.time = time;
	}
	
	public AudioThread(File file) {
		super();
		this.soundFile = file;
	}
	
	public void run() {
		try {
			audioInputStream = AudioSystem.getAudioInputStream(soundFile);
		} catch (Exception e) {
			System.out.println(soundFile + " failed to load");
			System.exit(1);
		}
		audioFormat = audioInputStream.getFormat();
		try {
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(audioFormat);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		line.start();
		int	nBytesRead = 0;
		byte[] abData = new byte[(int) (time * audioFormat.getFrameRate())/1000];
		try	{
			nBytesRead = audioInputStream.read(abData, 0, abData.length);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (nBytesRead >= 0) {
			int	nBytesWritten = line.write(abData, 0, nBytesRead);
		}
		line.drain();
		line.close();
	}
}