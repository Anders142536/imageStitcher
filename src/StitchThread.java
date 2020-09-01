public class StitchThread extends Thread {
    @Override
    public void run() {
        StitchJob current;
        while (null != (current = ImageStitcher.next())) {
            try {
                current.stitch();
                ImageStitcher.jobDone();
            } catch (StitchException e) {
                ImageStitcher.foundIssue(e);
            }
        }
    }
}
