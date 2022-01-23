package imageStitcher;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageDisplayer extends Component {
    BufferedImage img;
    public ImageDisplayer(BufferedImage img) {
        this.img = img;
    }

    public void paint(Graphics g) {
        g.drawImage(img, 0, 0, 1280, 720, 0, 0, img.getWidth(), img.getHeight(), null);
    }

    public Dimension getPreferredSize() {
        return new Dimension(1280,720);
    }
}
