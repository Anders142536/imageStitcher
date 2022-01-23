package imageStitcher;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StitchJob {
    private final String name;
    private final List<Tile> tiles;
    private int stepSizeX;
    private int stepSizeY;
    private int stepsX;
    private int stepsY;
    private BufferedImage result;

    public StitchJob(String name) {
        this.name = name;
        tiles = new ArrayList<>();
    }

    public void addFile(File f, int posX, int posY) {
        // positions start counting at 0, steps at 1!
        if (posX >= stepsX) stepsX = posX + 1;
        if (posY >= stepsY) stepsY = posY + 1;
        tiles.add(new Tile(f, posX, posY));
    }

    public void stitch() throws StitchException {
//        long startTimestamp = System.nanoTime();
        if (tiles.isEmpty()) throw new StitchException(name, "ERROR: No files found to stitch!");
        if (!tilesSizeIsValid()) ImageStitcher.foundIssue(new StitchException(name, "WARN: There seem to be some files missing. " +
                "There were " + tiles.size() + " files found. " + stepsX + " times " + stepsY + " were expected."));
        getStepSizesFromSample();

        result = new BufferedImage(stepSizeX * stepsX, stepSizeY * stepsY, BufferedImage.TYPE_INT_ARGB);

        drawFilesToResult();
        writeResultToFile();

//        long duration = System.nanoTime() - startTimestamp;
//        System.out.println("Stitching duration: " + duration + "ns"); //TODO: make this a log call once logging is implemented
//        showImage();
    }

    private boolean tilesSizeIsValid() {
        return tiles.size() == stepsX * stepsY;
    }

    private void getStepSizesFromSample() throws StitchException {
        try {
            BufferedImage sample = ImageIO.read(tiles.get(0).file);
            stepSizeX = sample.getWidth();
            stepSizeY = sample.getHeight();
        } catch (IOException e) {
            throw new StitchException(name, "ERROR: Something went wrong when getting the stepsize:\n" + e.getMessage());
        }
    }

    private void drawFilesToResult() throws StitchException {
        Graphics g = result.createGraphics();
        BufferedImage temp;

        for (Tile t : tiles) {
            try {
                temp = ImageIO.read(t.file);
                g.drawImage(temp, stepSizeX * t.posX, stepSizeY * t.posY, null);
            } catch (IOException e) {
                throw new StitchException(name, "ERROR: Couldn't read file " + t.file.getName() + ":\n" + e.getMessage());
            }
        }
    }

    public void writeResultToFile() throws StitchException {
        File output = new File(ImageStitcher.pathOutputFolder + "/" + name + ".png");
        try {
            ImageIO.write(result, "png", output);
        } catch (IOException e) {
            throw new StitchException(name, "ERROR: Couldn't write the result to path " + output.getName() + "\n" + e.getMessage());
        }
    }

    public String getName() { return name; }

    //Debug method that directly shows the image that is built. Exclusively for testing.
    private void showImage() {
        JFrame f = new JFrame("Image Preview");

        f.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        f.add(new ImageDisplayer(result));
        f.pack();
        f.setVisible(true);
    }
}