import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
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

    private Map<Integer, IValueManager> portManager;

    /**
     * Remote {@link ValueManager}s
     */
    private List<IValueManager> nodes;

    private Map<Integer, Request> pendingRequests;

    private int nbAcks;

    private void updateLocalTime(int remoteTimestapm) {
        localTime =  Math.max(localTime+1, remoteTimestapm+1);
    }

    public void acceptMessage(Message message) throws RemoteException {
        LOG.log(Level.INFO, () -> "message received");
        updateLocalTime(message.getTimestamp());
        switch (message.getMessageType()) {
            case ACKNOLEGMENT:
                break;
            case LIBERATION:
                break;
            default:
        }
    }

    public void acceptRequestMessage(RequestMessage message) throws RemoteException {
        pendingRequests.add(new Request(message.getTimestamp(), message.getNewValue(), port));
    }

    public void sendRequest(int newValue) throws RemoteException {
        localTime++;
        nbAcks = 0;
        RequestMessage requestMsg = new RequestMessage(localTime, port, newValue);
        pendingRequests.add(new Request(localTime, newValue, port));

        for (IValueManager vm : nodes) {
            vm.acceptRequestMessage(requestMsg);
        }
        LOG.log(Level.INFO, () -> "The set value request is sent to other nodes, value = " + newValue);
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
        pendingRequests = new TreeMap<>(nbNodes);
        nodes = new ArrayList<>(nbNodes);
        portManager = new HashMap<>();
    }

    public void lookup() throws RemoteException, NotBoundException, MalformedURLException {
        // reference the other ValueManagers of the system
        for (int p : ports) {
            String toLookup = "rmi://" + Constants.SERVER_HOST
                    + ":" + p + "/" + Constants.REMOTE_OBJ_NAME;
            Registry registry = LocateRegistry.getRegistry(Constants.SERVER_HOST);
            IValueManager manager = (IValueManager) Naming.lookup(toLookup)
            nodes.add(manager);
            portManager.put(port, manager);
            LOG.log(Level.INFO, () -> Constants.REMOTE_OBJ_NAME + " is linked with other nodes of the system");
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
        sendRequest(value);
        this.value = value;
    }
}
