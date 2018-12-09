public class Message {

    private int timestamp;
    private MessageType messageType;
    private int emitterPort;
    private int newValue;

    public Message(int timestamp, MessageType messageType, int emitterPort, int newValue) {
        this.timestamp = timestamp;
        this.messageType = messageType;
        this.emitterPort = emitterPort;
        this.newValue = newValue;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public int getEmitterPort() {
        return emitterPort;
    }

    public int getNewValue() {
        return newValue;
    }
}
