import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

/**
 * This class defines a remote object implementation.
 *
 * Authors: Samuel Mayor, Alexandra Korukova
 */
public class ValueManager extends UnicastRemoteObject implements IValueManager {

    private static final Logger LOG = Logger.getLogger(ValueManager.class.getName());

    private int value;

    /**
     * Default constructor
     * @throws RemoteException
     */
    public ValueManager() throws RemoteException {
        super();
    }

    public static void main(String ...args) throws RemoteException, AlreadyBoundException {
        // create and install the security manager
//        if(System.getSecurityManager() == null) {
//            System.setSecurityManager(new SecurityManager());
//        }
        // create and exports a Registry instance on the localhost that accepts requests on the port 1099
        // TODO: custom port
        Registry registry = LocateRegistry.createRegistry(1099);
        // bind the remote reference to the name in the registry
        registry.bind("ValueManager", new ValueManager());
    }

    /**
     * The implementation of the remote method
     * @return the value
     * @throws RemoteException
     */
    @Override
    public int getValue() throws RemoteException {
        // TODO
        return value;
    }

    /**
     * The implementation of the remote method
     * @throws RemoteException
     */
    @Override
    public void setValue(int value) throws RemoteException {
        // TODO
        this.value = value;
    }
}
