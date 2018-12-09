public class RequestMessage extends Message {
    private int newValue;

    public RequestMessage(int timestamp, int emitterPort, int newValue) {
        super(timestamp, MessageType.REQUEST, emitterPort);
        this.newValue = newValue;
    }

    public int getNewValue() {
        return newValue;
    }
}
