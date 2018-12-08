import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class defines a remote object implementation.
 *
 * Authors: Samuel Mayor, Alexandra Korukova
 */
public class ValueManager extends UnicastRemoteObject implements IValueManager {

    private static final Logger LOG = Logger.getLogger(ValueManager.class.getName());

    private int value;

    private static String valueManagerName;

    /**
     * Default constructor
     * @throws RemoteException
     */
    public ValueManager(String ...args) throws RemoteException {
        super();
    }

    /**
     * args[0] - the port on which the registry accepts the requests
     * args[1] - the name of the remote object
     * @param args
     * @throws RemoteException
     * @throws AlreadyBoundException
     */
    public static void main(String ...args) throws RemoteException, AlreadyBoundException {
        // create and install the security manager
//        if(System.getSecurityManager() == null) {
//            System.setSecurityManager(new SecurityManager());
//        }
        // create and exports a Registry instance on the localhost that accepts requests
        int port = Integer.parseInt(args[0]);
        int id = Integer.parseInt(args[1]);
        valueManagerName = Constants.REMOTE_OBJ_NAME + id;
        Registry registry = LocateRegistry.createRegistry(port);
        // bind the remote reference to the name in the registry
        registry.bind(valueManagerName, new ValueManager());
        LOG.log(Level.INFO, () -> valueManagerName + " bound");
        LOG.log(Level.INFO, () -> "Listening on incoming remote invocations on port: " + port);

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
