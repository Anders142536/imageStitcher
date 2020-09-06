import org.ini4j.Ini;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageStitcher {
    static HashMap<String, StitchJob> jobMap = new HashMap<>();
    static Queue<StitchJob> jobs;
    static List<StitchThread> threads = new ArrayList<>();
    static Pattern pattern;
    static Matcher matcher;

    static int jobsDoneCount = 0;
    static List<StitchException> issues;
    static int numberOfJobs;
    static int numberOfJobsDigitCount;

    //values from config file
    static String pathToInputFolder;
    static int desiredThreads;

    //paths
    static String pathExecutable;
    static String pathParentDir;
    static String pathOutputFolder;

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
        System.out.print(jobMap.size() + " screenshot(s).\n" +
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
//        pathExecutable = "D:/git/imageStitcher/workspace/"; //TODO: delete this

        try {
            pathParentDir = new File(pathExecutable).getParent();
            Ini config = new Ini(new File(pathParentDir + "/imageStitcher.config"));
            pathToInputFolder = config.get("Config", "PathToInputFolder");
            desiredThreads = Integer.parseInt(config.get("Config", "DesiredNumberOfThreads"));
        } catch (FileNotFoundException e) {
            String path = pathParentDir + "/imageStitcher.config";
            System.out.println("\nERROR: Config file not found!\n" +
                    "If this is your first run, you have nothing to worry about. A config file was created for you here:\n" +
                    path + "\nPlease set all configurations and try again.");
            createEmptyConfig(path);
            return false;
        } catch (IOException e) {
            System.out.println("\nERROR: Something went wrong on reading the config file!\n" +
                    "It should be written in the ini format. Please make sure it is a correctly formatted ini file and try again.");
            return false;
        } catch (NumberFormatException e) {
            System.out.println("\nERROR: Something went wrong on reading some number!\n" +
                    "Please make sure that all numbers are actual numbers.");
        }
        System.out.println("done.");
        return true;
    }

    private static void createEmptyConfig(String path) {
        try {
            File f = new File(path);
            f.createNewFile();
            Ini config = new Ini(f);
            config.put("Config", "PathToInputFolder", "Replace me with the path to the input folder. When on windows, a backslash in the path needs to be replaced with either \\ or a /");
            config.put("Config", "DesiredNumberOfThreads", 8);
            config.store();
        } catch (IOException e) {
            System.out.println("ERROR: Something went wrong when trying to create an empty config file!");
            e.printStackTrace();
        }
    }

    private static void readFilesFromInputFolder(File inputFolder) {
        pattern = Pattern.compile("([^_]{4,})_x(\\d+)_y(\\d+)");
        String filename;
        int posX;
        int posY;
        StitchJob job = null;

        for (File f: inputFolder.listFiles()) {
            matcher = pattern.matcher(f.getName());
            if (matcher.find()) {
                filename = matcher.group(1);
                posX = Integer.parseInt(matcher.group(2));
                posY = Integer.parseInt(matcher.group(3));

                if (job == null || !job.getName().equals(filename)) {
                    if (jobMap.containsKey(filename)) job = jobMap.get(filename);
                    else {
                        job = new StitchJob(filename);
                        jobMap.put(filename, job);
                    }
                }

                job.addFile(f, posX, posY);
            } else
                System.out.println("WARN: File " + f.getName() +
                        " seems to not match the expected naming pattern. File will be skipped");
        }
    }

    private static boolean userWantsToStitch() {
        Scanner s = new Scanner(System.in);
        String input;
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

    private static void stitchJobs() {
        prepareStitching();
        long timestamp = System.currentTimeMillis();

        while (!jobs.isEmpty() || areThreadsAlive()) {
            try {
                Thread.sleep(1000);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        long duration = System.currentTimeMillis() - timestamp;
        System.out.println("\nStitching done. It took " + formatDuration(duration));
        if (!issues.isEmpty()) printIssues();
    }

    private static void prepareStitching() {
        jobs = new LinkedList<>(jobMap.values());
        issues = new ArrayList<>();
        numberOfJobs = jobs.size();
        numberOfJobsDigitCount = (int) (Math.log10(numberOfJobs) + 1);

        pathOutputFolder = pathParentDir + "/stitchedScreenshots";
        File outputFolder = new File(pathOutputFolder);
        if (!outputFolder.exists()) outputFolder.mkdir();

        printLoadingbar();
        for (int i = 0; i < desiredThreads; i++) {
            StitchThread newThread = new StitchThread();
            threads.add(newThread);
            newThread.start();
        }
    }

    private static boolean areThreadsAlive() {
        for (StitchThread t: threads) {
            if (t.isAlive()) return true;
        }
        return false;
    }

    static synchronized StitchJob next() {
        return jobs.poll();
    }

    static synchronized void foundIssue(StitchException e) {
        issues.add(e);
    }

    static synchronized void jobDone() {
        jobsDoneCount++;
        printLoadingbar();

    }

    //TODO: improve this if a way is found
    private static void printLoadingbar() {

        String bar = "\r[";
        int lengthBar = 20;
        int progress = ((jobsDoneCount * lengthBar) / numberOfJobs);

        for (int i = progress; i > 0; i--) {
            bar += "#";
        }

        for (int i = lengthBar - progress; i > 0; i--) {
            bar += " ";
        }

        bar += "]";

        String formattedNumbers = String.format("%1$" + numberOfJobsDigitCount + "s / " + numberOfJobs, jobsDoneCount);
        String formattedPercentages = String.format("%1$2d", ((jobsDoneCount * 100) / numberOfJobs));
        System.out.print(bar + " " + formattedNumbers + " (" + formattedPercentages + "%), " + issues.size() + " issue(s)");
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

    private static void printIssues() {
        System.out.println("\nList of issues:");
        for (StitchException e: issues) {
            System.out.println(e.filename + ":\n" + e.reason);
        }
    }
}
