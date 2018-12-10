import java.io.Serializable;

/**
 * This class represents a message {@link IValueManager}s sends to each other
 *
 * Authors: Samuel Mayor, Alexandra Korukova
 */
public class Message implements Serializable {

    /**
     * The logical timestamp which indicates when the message was sent.
     * Used to insure the right execution order of the requests (in order they arrive to the request queue)
     */
    private int timestamp;
    /**
     * can be REQUEST, ACKNOWLEDGEMENT or LIBERATION
     */
    private MessageType messageType;
    /**
     * The port of the {@link IValueManager} emitting the message.
     * Used to reference the sender
     */
    private int emitterPort;

    /**
     * Constructor
     * @param timestamp the logical timestamp of the {@link Message}
     * @param messageType message's {@link MessageType}
     * @param emitterPort emitter's port
     */
    public Message(int timestamp, MessageType messageType, int emitterPort) {
        this.timestamp = timestamp;
        this.messageType = messageType;
        this.emitterPort = emitterPort;
    }

    /**
     * Getter
     * @return {@link Message}'s timestamp
     */
    public int getTimestamp() {
        return timestamp;
    }

    /**
     * Getter
     * @return {@link MessageType} of the message
     */
    public MessageType getMessageType() {
        return messageType;
    }

    /**
     * Getter
     * @return port of the emitter of the message
     */
    public int getEmitterPort() {
        return emitterPort;
    }
}
