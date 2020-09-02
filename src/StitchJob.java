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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StitchJob {
    static Pattern pattern;

    private final String name;
    private final List<File> files;
    private int tileCount;
    private int stepSizeX;
    private int stepSizeY;
    private BufferedImage result;

    public StitchJob(String name) {
        this.name = name;
        files = new ArrayList<>();
    }

    public void addFile(File f) {
        files.add(f);
        if (f.getName().matches("[^_]{4,}_x\\d+_y\\d+")) System.out.println("WARN: File " + f.getName() + " seems to not match the expected naming pattern.");
    }

    public void stitch() throws StitchException {
        long startTimestamp = System.nanoTime();
        prepareStitching();
        drawFilesToResult();
        writeResultToFile();

        long duration = System.nanoTime() - startTimestamp;
//        System.out.println("Stitching duration: " + duration + "ns"); //TODO: make this a log call once logging is implemented
//        showImage();
    }

    private void prepareStitching() throws StitchException {
        if (files.isEmpty()) throw new StitchException(name, "ERROR: No files found to stitch!");
        checkTileCount();
        checkStepSizes();

        result = new BufferedImage(stepSizeX * tileCount, stepSizeY * tileCount, BufferedImage.TYPE_INT_ARGB);
    }

    private void checkTileCount() {
        if (files.size() <= 1) tileCount = 1;
        else if (files.size() <= 4) tileCount = 2;
        else if (files.size() <= 16) tileCount = 4;
        else if (files.size() <= 64) tileCount = 8;
        else if (files.size() <= 256) tileCount = 16;
        else if (files.size() <= 1024) tileCount = 32;
        else tileCount = 64;

        if (files.size() != Math.pow(tileCount, 2)) ImageStitcher.foundIssue(new StitchException(name, "WARN: There seem to be some files missing. " +
                "There were " + files.size() + " files found. A power of 4 is expected."));
    }

    private void checkStepSizes() throws StitchException {
        try {
            BufferedImage sample = ImageIO.read(files.get(0));
            stepSizeX = sample.getWidth();
            stepSizeY = sample.getHeight();
        } catch (IOException e) {
            throw new StitchException(name, "ERROR: Something went wrong when checking the stepsize:\n" + e.getMessage());
        }
    }

    private void drawFilesToResult() throws StitchException {
        Graphics g = result.createGraphics();
        BufferedImage temp;
        Matcher matcher;
        int xOffset;
        int yOffset;
        String filename = "";

        try {
            for (File f : files) {
                filename = f.getName();
                temp = ImageIO.read(f);
                matcher = pattern.matcher(f.getName());
                if (matcher.find()) {
                    xOffset = Integer.parseInt(matcher.group(1));
                    yOffset = Integer.parseInt(matcher.group(2));
                    g.drawImage(temp, stepSizeX * xOffset, stepSizeY * yOffset, null);
                }
                // nasty, i know, don't judge me
                else ImageStitcher.foundIssue(new StitchException(name, "WARN: Couldn't extract x and y offset from filename "
                        + f.getName() + "!. File will be skipped."));
            }
        } catch (IOException e) {
            throw new StitchException(name, "ERROR: Couldn't read file " + filename + ":\n" + e.getMessage());
        }
    }

    public void writeResultToFile() throws StitchException {
        File output = new File(ImageStitcher.pathOutputFolder + "/" + name + ".png");
        try {
            ImageIO.write(result, "png", output);
        } catch (IOException e) {
            throw new StitchException(name, "ERROR: Couldn't write the result to path " + output.getName() + "\n" + e.getMessage());
        }
        result = null; //Freeing the memory used by the result
    }

    public String getName() {
        return name;
    }

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