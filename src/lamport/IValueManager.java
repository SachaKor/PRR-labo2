package lamport;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The Remote Interface implemented by Value Managers of the system
 *
 * Authors: Samuel Mayor, Alexandra Korukova
 */
public interface IValueManager extends Remote {
    /**
     * Returns the current value stored in the value manager
     * Does not wait all the requests from the queue to be executed
     * @return the current value stored in the value manager
     * @throws RemoteException
     */
    int getValue() throws RemoteException;

    /**
     * Requests the modification of the value. The value will be set to the integer passed as a parameter once
     * it is the current node's turn (Lamport algorithm)
     * @param value the value to set
     * @throws RemoteException
     */
    void setValue(int value) throws RemoteException;

    /**
     * Looks up for the other nodes of the system.
     * This command has to be requested by the client after all the value managers are started to link t
     * he value managers between them.
     * @throws RemoteException
     * @throws NotBoundException
     * @throws MalformedURLException
     */
    void lookup() throws RemoteException, NotBoundException, MalformedURLException;

    /**
     * Accepts the incoming {@link Message} from another value manager
     * @param message the {@link Message} sent by some remote {@link IValueManager}
     * @throws RemoteException
     */
    void acceptMessage(Message message) throws RemoteException;

    /**
     * Updates the value stored
     * @param newValue the new value to set
     * @throws RemoteException
     */
    void updateValue(int newValue) throws RemoteException;
}
