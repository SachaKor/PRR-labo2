import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class defines a remote object implementation.
 *
 * Authors: Samuel Mayor, Alexandra Korukova
 */
public class ValueManager extends UnicastRemoteObject implements IValueManager {

    private static final Logger LOG = Logger.getLogger(ValueManager.class.getName());

    /**
     * The value stored by {@link ValueManager}
     */
    private int value;

    /**
     * Local timestamp which is updated with every incoming/emitted {@link Message}
     */
    private int localTime;

    /**
     * The port on which {@link ValueManager} is listening for the incoming requests
     */
    private int port;

    /**
     * Number of the nodes of the system
     */
    private int nbNodes;

    /**
     * Ports of all other {@link ValueManager}s of the system
     */
    private int[] ports;

    /**
     * Remote {@link ValueManager}s
     */
    private List<IValueManager> nodes;

    /**
     * This {@link PriorityQueue} stores all the pending requests in the increasing order of the timestamps
     * Used by the system to execute the requests in the order they are created (Lamport algorithm)
     */
    private PriorityQueue<Request> pendingRequests;


    public void acceptMessage(Message message) {
        // TODO
    }

    public void sendRequest(int newValue) {
        localTime++;
        Message request = new Message(localTime, MessageType.REQUEST, port, newValue);
        // TODO: send requests to other nodes
    }

    /**
     * Default constructor
     * @throws RemoteException
     */
    public ValueManager(int port, int nbNodes, int[] ports) throws RemoteException {
        super();
        localTime = 0;
        this.port = port;
        this.nbNodes = nbNodes;
        this.ports = ports;
        pendingRequests = new PriorityQueue<>(nbNodes, (r1, r2) -> (r1.getTimestamp() - r2.getTimestamp()));
        nodes = new ArrayList<>(nbNodes);
    }

    public void lookup() throws RemoteException, NotBoundException, MalformedURLException {
        // reference the other ValueManagers of the system
        for (int p : ports) {
            String toLookup = "rmi://" + Constants.SERVER_HOST
                    + ":" + p + "/" + Constants.REMOTE_OBJ_NAME;
            Registry registry = LocateRegistry.getRegistry(Constants.SERVER_HOST);
            nodes.add((IValueManager) Naming.lookup(toLookup));

        }
    }

    /**
     * args[0] - the port on which the registry accepts the requests
     * args[1] - the name of the remote object
     * args[2] - number of the nodes of the system
     * @param args
     * @throws RemoteException
     * @throws AlreadyBoundException
     */
    public static void main(String ...args) throws RemoteException, AlreadyBoundException {
        // create and install the security manager
//        if(System.getSecurityManager() == null) {
//            System.setSecurityManager(new SecurityManager());
//        }

        // parse the main arguments
        int port = Integer.parseInt(args[0]);
        int nbNodes = Integer.parseInt(args[1]);
        int[] ports = new int[nbNodes-1];
        for(int i  = 0; i < nbNodes-1; i++) {
            ports[i] = Integer.parseInt(args[i+2]);
        }

        // create and exports a Registry instance on the localhost that accepts requests
        Registry registry = LocateRegistry.createRegistry(port);
        // bind the remote reference to the name in the registry
        registry.bind(Constants.REMOTE_OBJ_NAME, new ValueManager(port, nbNodes, ports));
        LOG.log(Level.INFO, () -> Constants.REMOTE_OBJ_NAME + " bound");
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
