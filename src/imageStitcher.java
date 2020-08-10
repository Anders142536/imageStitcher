import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class imageStitcher {
    public static void main(String[] args) {

        String config = readConfig();
        String pathForScreenshotInput = config.split("PathForScreenshotsToStitchTogether=")[1];

        File inputFolder = new File(pathForScreenshotInput);
        if (inputFolder.exists()) {
            System.out.println("ERROR: pathForScreenshotInput does not exist");
            return;
        }
        String[] test = null;
    }

    
    private static String readConfig() {
        String path = imageStitcher.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        File test = new File(path + "/file");
        String toReturn = null;

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(test)); 
            toReturn = br.readLine();
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: Config file not found!");
        } catch (IOException e) {
            System.out.println("ERROR: Something went wrong on reading the config file!");
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException e) {
                System.out.println("ERROR: Something went wrong when closing the BufferedReader Instance!");
            }
        }
        return toReturn;
    }
}
