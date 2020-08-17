import org.ini4j.Ini;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class imageStitcher {
    public static void main(String[] args) {

        String pathToInputFolder = readPathToInputFolder();

        File inputFolder = new File(pathToInputFolder);
        if (!inputFolder.exists()) {
            System.out.println("ERROR: pathForInputFolder does not exist");
            return;
        }
        String[] test = inputFolder.list();
        System.out.println(test.length);
    }

    
    private static String readPathToInputFolder() {
        String path = imageStitcher.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        path = "/home/anders/git/imageStitcher/workspace/";
        File configFile = new File(path + "config");
        String toReturn = null;

        try {
            Ini config = new Ini(configFile);
            toReturn = config.get("Config", "PathToInputFolder");
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: Config file not found!");
        } catch (IOException e) {
            System.out.println("ERROR: Something went wrong on reading the config file!");
        }
        return toReturn;
    }
}
