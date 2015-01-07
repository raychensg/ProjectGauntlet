import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class Stage extends GObj {
	private String stageFileName;
	private File stageFile;
	private BufferedImage stageImage;
	private int x, y;
	private int width, height;
	
	public Stage(String stageFileName, int x, int y) {
		this.stageFileName = stageFileName;
		this.x = x;
		this.y = y;
		try {
			stageFile = new File(stageFileName);
			try {
			    stageImage = ImageIO.read(stageFile);
			    width = stageImage.getWidth();
			    height = stageImage.getHeight();
			} catch (IOException e) {
				System.out.println("File format incorrect");
			}
		}
		catch (Exception e) {
			System.out.println(stageFileName + " failed to load");
		}
	}
	public void tick() {}
	public void draw(Graphics2D g2) {
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
		g2.drawImage(stageImage, x, y, x + width, y + height, 0, 0, width, height, null);
		System.out.println();
	}

}
