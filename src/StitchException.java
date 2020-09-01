public class StitchException extends Exception {
    String filename;
    String reason;

    public StitchException() {
        reason = "No reason given";
    }

    public StitchException(String filename, String reason) {
        this.filename = filename;
        this.reason = reason;
    }
}
