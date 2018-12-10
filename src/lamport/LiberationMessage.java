package lamport;

/**
 * This class represents a liberation message sent by the {@link IValueManager} when it liberates the critical
 * section
 *
 * Authors: Samuel Mayor, Alexandra Korukova
 */
public class LiberationMessage extends Message {

    // the value set by the emitter
    private int newValue;

    /**
     * Constructor
     * @param timestamp the logical timestamp of the {@link Message}
     * @param messageType message's {@link MessageType}
     * @param emitterPort emitter's port
     * @param newValue the value set by the emitter
     */
    public LiberationMessage(int timestamp, MessageType messageType, int emitterPort, int newValue) {
        super(timestamp, messageType, emitterPort);
        this.newValue = newValue;
    }

    /**
     * Getter
     * @return the value to set by the emitter {@link IValueManager}.
     * The receiver must the value to the newValue
     */
    public int getNewValue() {
        return newValue;
    }
}
