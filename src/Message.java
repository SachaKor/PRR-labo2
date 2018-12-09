import java.io.Serializable;

public class Message implements Serializable {

    private int timestamp;
    private MessageType messageType;
    private int emitterPort;

    public Message(int timestamp, MessageType messageType, int emitterPort) {
        this.timestamp = timestamp;
        this.messageType = messageType;
        this.emitterPort = emitterPort;
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
}
