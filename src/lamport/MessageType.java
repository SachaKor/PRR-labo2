package lamport;

import lamport.IValueManager;

/**
 * This enumeration represents the types of the messages exchanged by {@link IValueManager}s
 *
 * Authors: Samuel Mayor, Alexandra Korukova
 */
public enum MessageType {
    REQUEST,
    ACKNOWLEDGEMENT,
    LIBERATION
}
