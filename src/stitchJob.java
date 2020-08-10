import java.io.File;

public class stitchJob {
    File[] files;
    String name;
    File result;

    public stitchJob(File[] files) {
        this.files = files;
        peelName(files[0]);
    }

    private void peelName(File input){
        name = input.getName().replaceAll("_\\d*[x,y]", "");
    }

    public void stitch() {

    }

    public File getResult() {
        return result;
    }
}