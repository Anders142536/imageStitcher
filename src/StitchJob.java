import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StitchJob {
    String name;
    List<File> files;
    File[][] grid;
    File result;

    public StitchJob(String name) {
        this.name = name;
        files = new ArrayList<>();
    }

    public void addFile(File f) {
        files.add(f);
    }

    private void prepareStitching() {

    }

    public void stitch() {
        prepareStitching();
    }

    public File getResult() {
        return result;
    }
}