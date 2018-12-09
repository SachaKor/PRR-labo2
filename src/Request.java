public class Request {
    private int timestamp;
    private int value;

    public Request(int timestamp, int value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public int getValue() {
        return value;
    }
}
