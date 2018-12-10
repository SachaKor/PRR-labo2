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

    private Map<Integer, Message> pendingRequests;

    private int nbAcks;

    private boolean criticalSectionRequested;

    private int newValue;

    private void updateLocalTime(int remoteTimestapm) {
        localTime =  Math.max(localTime+1, remoteTimestapm+1);
    }

    public void updateValue(int newValue) throws RemoteException {
        this.value = newValue;
    }

    public void acceptMessage(Message message) throws RemoteException {
        LOG.log(Level.INFO, () -> "[" + localTime + "] " + "message received");
        updateLocalTime(message.getTimestamp());
        LOG.log(Level.INFO, () -> "[" + localTime + "] "
                + message.getMessageType().name()
                + " from " + message.getEmitterPort());
        switch (message.getMessageType()) {
            case REQUEST:
                pendingRequests.put(message.getEmitterPort(), message);
                portManager.get(message.getEmitterPort()).acceptMessage(
                        new Message(localTime, MessageType.ACKNOWLEDGEMENT, port));
                break;
            case ACKNOWLEDGEMENT:
                nbAcks++;
                checkCriticalSection();
                break;
            case LIBERATION:
                pendingRequests.remove(message.getEmitterPort());
                checkCriticalSection();
                break;
            default:
                LOG.log(Level.SEVERE, "Unknown message type");
        }
    }

    private void checkCriticalSection() throws RemoteException {
        if(criticalSectionRequested && nbAcks == nbNodes-1) {
            // check if the local request is the oldest one
            int localRequestTime = pendingRequests.get(port).getTimestamp();
            for (Map.Entry<Integer, Message> entry : pendingRequests.entrySet()) {
                if (localRequestTime > entry.getValue().getTimestamp()) {
                    return;
                }
            }
            // the local request is the oldest one
            LOG.log(Level.INFO, "Entering in the critical section");
            this.value = newValue;
            pendingRequests.remove(port);
            criticalSectionRequested = false;
            nbAcks = 0;
            // inform other nodes of the system
            LOG.log(Level.INFO, "Updating value in other nodes");
            for (IValueManager vm : nodes) {
                vm.acceptMessage(new Message(localTime, MessageType.LIBERATION, port));
                vm.updateValue(newValue);
            }
            LOG.log(Level.INFO, "Value updated, new value: " + value);
        }
    }

    public void sendRequest(int newValue) throws RemoteException {
        localTime++;
        nbAcks = 0;
        criticalSectionRequested = true;
        this.newValue = newValue;
        Message requestMsg = new Message(localTime, MessageType.REQUEST, port);
        pendingRequests.put(port, requestMsg);

        for (IValueManager vm : nodes) {
            vm.acceptMessage(requestMsg);
        }
        LOG.log(Level.INFO, () -> "The set value request is sent to other nodes");
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
        pendingRequests = new TreeMap<>();
        nodes = new ArrayList<>(nbNodes);
        portManager = new HashMap<>();
        criticalSectionRequested = false;
    }

    public void lookup() throws RemoteException, NotBoundException, MalformedURLException {
        // reference the other ValueManagers of the system
        for (int p : ports) {
            String toLookup = "rmi://" + Constants.SERVER_HOST
                    + ":" + p + "/" + Constants.REMOTE_OBJ_NAME;
            Registry registry = LocateRegistry.getRegistry(Constants.SERVER_HOST);
            IValueManager manager = (IValueManager) Naming.lookup(toLookup);
            nodes.add(manager);
            portManager.put(p, manager);
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
        return value;
    }

    /**
     * The implementation of the remote method
     * @throws RemoteException
     */
    @Override
    public void setValue(int value) throws RemoteException {
        sendRequest(value);
        this.value = value;
    }
}
