public class Request {
    private int timestamp;
    private int value;
    private int nbAcks;

    public Request(int timestamp, int value) {
        this.timestamp = timestamp;
        this.value = value;
        nbAcks = 0;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public int getValue() {
        return value;
    }
}
