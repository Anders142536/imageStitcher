public class StitchException extends Exception {
    String reason;

    public StitchException() {
        reason = "No reason given";
    }

    public StitchException(String reason) {
        this.reason = reason;
    }
}
