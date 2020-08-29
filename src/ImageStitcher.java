import org.ini4j.Ini;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ImageStitcher {
    static String pathExecutable;
    static String pathParentDir;
    static String pathOutputFolder;
    static HashMap<String, StitchJob> jobs = new HashMap<>();

    //values from config file
    static String pathToInputFolder;

    public static void main(String[] args) {
        welcome();
        if (!loadConfig()) return;

        File inputFolder = new File(pathToInputFolder);
        if (!inputFolder.exists()) {
            System.out.println("ERROR: pathForInputFolder does not exist");
            return;
        }
        if (inputFolder.length() == 0) {
            System.out.println("No files found in input folder.");
            return;
        }

        System.out.print(inputFolder.list().length + " files found in input folder being groupable into.. ");
        readFilesFromInputFolder(inputFolder);
        System.out.print(jobs.size() + " screenshot(s).\n" +
                "Please keep in mind that this might take quite a while and will take the same amount of space on your hard drive" +
                " as the initial input folder.\n" +
                "Do you want to start stitching? (yes/no) ");

        if (!userWantsToStitch()) return;

        stitchJobs();
    }

    /**
     * Shows a nice welcome message with my skull ascii art.
     */
    private static void welcome() {
        String welcome = "               /@@@@@@@@@@@@@@@@              \n" +
                "          @@@@@@@@@@@@@@@@@@@@@@(,            \n" +
                "       @@@@@@@@@@@@@@@@@@@@@@@@@(##/*         \n" +
                "     %@@@@@@@@@@@@@@@@@@@@@@@@@@ .(#****      \n" +
                "    @@@@@@@@@@@@@@@@@@@@@@@@@@@    .# .**#    \n" +
                "   @@@@@@@@@@@@@@@@@@@@@@@@@*. .**#####(/(/#  \n" +
                "   @@@@@@@@@@@@@@@@@@@       ...*.   /*##*(*  \n" +
                "   @@@@@@@@@@@@@          ..... .*..**,****   \n" +
                "    @@@@@@@@@%           ....   .....** .*.*#,\n" +
                "  @  @@@@@@@@@          ..            .*#***. \n" +
                "  @@ @@@@@@@@@@@@@@             ....**** .,*  \n" +
                "   (@@@@@@@@@@     %@@       ......******..   \n" +
                "   @@                          .,.....**.     \n" +
                "  @                          .    .   ...     \n" +
                "  @@                @  @        .* *.*..\n" +
                "  @@                @@@@\n" +
                "@@@@@              @@@@@(\n" +
                ".@@@@@@         @@@@@\n" +
                " @@@@             @\n" +
                "  &@             @\n" +
                "             ,@@@(      Welcome!\n" +
                "            ,@@@@&      This is the image stitcher program written by Anders142536.                      \n" +
                "             @@@@.      It is designed to stitch the split output of the Factorio mod\n" +
                "            ,/@@@       FacAutoScreenshot by Anders142536 into whole images.\n" +
                "          @@@@%(@       If you find any issues pls contact anders142536@gmail.com or\n" +
                "          &@@@@         send a message on github.\n" +
                "           @@@@@@       \n" +
                "            @@@@        \n";

        System.out.println(welcome);
    }

    /**
     * Attempts to load the config file. If there was no Config file found an empty shell will be created.
     * @return true if the loading was successful
     */
    private static boolean  loadConfig() {
        System.out.print("Loading config file.. ");
        pathExecutable = ImageStitcher.class.getProtectionDomain().getCodeSource().getLocation().getPath();
//        pathExecutable = "/home/anders/git/imageStitcher/workspace/"; //TODO: delete this

        try {
            pathParentDir = new File(pathExecutable).getParent();
            Ini config = new Ini(new File(pathParentDir + "/imageStitcher.config"));
            pathToInputFolder = config.get("Config", "PathToInputFolder");
        } catch (FileNotFoundException e) {
            System.out.println("\nERROR: Config file not found!\n" +
                    "If this is your first run, you have nothing to worry about. A config file was created for you here:\n" +
                    pathExecutable + "config\n" +
                    "Please set all configurations and try again.");
            createEmptyConfig();
            return false;
        } catch (IOException e) {
            System.out.println("\nERROR: Something went wrong on reading the config file!\n" +
                    "It should be written in the ini format. Please make sure it is a correctly formatted ini file and try again.");
            return false;
        }
        System.out.println("done.");
        return true;
    }

    private static void createEmptyConfig() {
        try {
            File f = new File(pathParentDir + "/imageStitcher.config");
            f.createNewFile();
            Ini config = new Ini(f);
            config.put("Config", "PathToInputFolder", "Replace me with the path to the input folder");
            config.store();
        } catch (IOException e) {
            System.out.println("ERROR: Something went wrong when trying to create an empty config file!");
            e.printStackTrace();
        }
    }

    private static void readFilesFromInputFolder(File inputFolder) {
        //TODO: optimize this to only touch the map if the filename is different from the one of last runs
        String filename;
        StitchJob job;
        for (File f: inputFolder.listFiles()) {
            filename = peelName(f);
            if (!jobs.containsKey(filename)) {
                job = new StitchJob(filename);
                jobs.put(filename, job);
            } else job = jobs.get(filename);

            job.addFile(f);
        }
    }

    private static boolean userWantsToStitch() {
        Scanner s = new Scanner(System.in);
        String input = null;
        while ((input = s.nextLine()) != null) {
            switch (input) {
                case "yes":
                case "y":
                    return true;
                case "no":
                case "n":
                    return false;
                default:
                    System.out.print("Please answer with yes or no. ");
            }
        }

        return false;
    }

    private static String peelName(File input){
        return input.getName().replaceAll("_.*", "");
    }

    private static void stitchJobs() {
        prepareStitching();
        System.out.println("Starting stitching...");
        long timestamp = System.currentTimeMillis();
        long lastMessage = timestamp;
        int jobCount = 0;
        int issueCount = 0;
        int numberOfJobsDigitCount = (int) (Math.log10(jobs.size()) + 1);

        for (StitchJob j: jobs.values()) {
            try {
                j.stitch();
            } catch (StitchException e) {
                System.out.println("ERROR: Could not stitch " + j.getName() + "" +
                        "\nReason:\n" + e.reason);
                issueCount++;
            }
            jobCount++;

            //if at least one second has passed and it was not the last job that just finished
            if (System.currentTimeMillis() - 1000 > lastMessage && jobCount < jobs.size()) {
                String formattedNumbers = String.format("%1$" + numberOfJobsDigitCount + "s / " + jobs.size(), jobCount);
                String formattedPercentages = String.format("%1$2d", ((jobCount * 100) / jobs.size()));
                System.out.println(formattedNumbers + " (" + formattedPercentages + "%).");
                lastMessage = System.currentTimeMillis();
            }
        }
        long duration = System.currentTimeMillis() - timestamp;
        System.out.print("Stitching done. It took " + formatDuration(duration));
        if (issueCount != 0) System.out.println(issueCount + " files had issues and might not have been handled correctly");
    }

    private static void prepareStitching() {
        StitchJob.pattern = Pattern.compile("(\\d*)_y(\\d*)");
        pathOutputFolder = pathParentDir + "/stitchedScreenshots";
        File outputFolder = new File(pathOutputFolder);
        if (!outputFolder.exists()) outputFolder.mkdir();
    }

    private static String formatDuration(long duration) {
        // 1000 000 000
        //  min   s  ms
        String result = "";
        if (duration >= 3_600_000) {
            result += (int)(duration / 3_600_000) + "h ";
            duration %= 3_600_000;
        }
        if (duration >= 60_000) {
            result += (int)(duration / 60_000) + "min ";
            duration %= 60_000;
        }
        if (duration >= 1000) {
            result += (int)(duration / 1000) + "s ";
            duration %= 1000;
        }
        result += duration + "ms";
        return result;
    }
}
