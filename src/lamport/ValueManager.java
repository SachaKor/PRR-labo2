package lamport;

import utils.Constants;

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
 * This class defines a remote object of the {@link IValueManager} implementation.
 *
 * DESCRIPTION:
 * When the {@link ValueManager} is launched, arguments of the main program are parsed first.
 * The first argument argument (args[0]) is the port on which the current {@link ValueManager} is listening on.
 * The second parameter (args[1]) is the number N of the nodes of the system.
 * The next N-1 arguments are the ports on which the other {@link ValueManager}s are listening on.
 * Every {@link ValueManager} must know the other nodes' ports to execute the mutual exclusion Lamport algorithm
 * when several nodes send the value modification requests.
 *
 * LAMPORT:
 * - When the client demands the modification of the value stored in the {@link IValueManager} attributed to him,
 * the {@link ValueManager} first pushes the request to the request queue and informs all other nodes of this request,
 * so that they also store it in their proper request queues.
 * - When a {@link ValueManager} recieves such a modification request, it responds to the emitter with an
 * acknowledgement message.
 * - Once the {@link ValueManager} collects N-1 acknowledgements, it checks if it is able to get the critical section
 * and change the value. It is the case when it's request is the oldest one in the request queue. If it is not,
 * {@link ValueManager} continues waiting for it's turn.
 * - When the value is modified by the {@link ValueManager}, the critical section is liberated and the the liberation
 * messages are diffused to other nodes of the system. Once this message is received by a node, it also checks if it
 * it's turn to get the critical section.
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
     * This map associates the port with the {@link IValueManager} which is listening on this port
     */
    private Map<Integer, IValueManager> portManager;

    /**
     * This map represents a request queue for the Lamport algorithm
     * The keys of the map are the port of the {@link IValueManager} holding the request
     * The values are the {@link Message}s containing the request
     */
    private Map<Integer, Message> pendingRequests;

    /**
     * Current number of acknowledgements received by the {@link ValueManager}
     */
    private int nbAcks;

    /**
     * This boolean is true if the current node has requested the access to the critical section
     */
    private boolean criticalSectionRequested;

    /**
     * In case if the critical section is requested by the {@link ValueManager}, this integer stores the value
     * which the {@link ValueManager} desires to set
     */
    private int newValue;

    /**
     * @param args
     *      * args[0] - the port on which the registry accepts the requests
     *      * args[1] - number of the nodes of the system - N
     *      * args[2]..args[N-1] - ports of the other {@link ValueManager}s of the system
     * @throws RemoteException
     * @throws AlreadyBoundException
     */
    public static void main(String ...args) throws RemoteException, AlreadyBoundException {

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
     * Constructor
     * @param port the port the {@link ValueManager} is listening on
     * @param nbNodes total number of the nodes ({@link client.Site} - {@link ValueManager} couples) of the system: N
     * @param ports the array containing N-1 ports of other {@link ValueManager}s of the system
     * @throws RemoteException
     */
    public ValueManager(int port, int nbNodes, int[] ports) throws RemoteException {
        super();
        localTime = 0;
        this.port = port;
        this.nbNodes = nbNodes;
        this.ports = ports;
        pendingRequests = new TreeMap<>();
        portManager = new HashMap<>();
        criticalSectionRequested = false;
    }

    /**
     * This method is used to link the current {@link ValueManager} with the other fot the further execution of the
     * Lamport mutual exclusion algorithm
     * @throws RemoteException
     * @throws NotBoundException
     * @throws MalformedURLException
     */
    public void lookup() throws RemoteException, NotBoundException, MalformedURLException {
        // reference the other ValueManagers of the system
        for (int p : ports) {
            String toLookup = "rmi://" + Constants.SERVER_HOST
                    + ":" + p + "/" + Constants.REMOTE_OBJ_NAME;
            Registry registry = LocateRegistry.getRegistry(Constants.SERVER_HOST);
            IValueManager manager = (IValueManager) Naming.lookup(toLookup);
            portManager.put(p, manager);
            LOG.log(Level.INFO, () -> Constants.REMOTE_OBJ_NAME + " is linked with other nodes of the system");
        }
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

    /**
     * This method is called when the user desires to modify the value stored by {@link ValueManager}s.
     * Pushes the request (as a {@link Message}) to the request queue and sends the request messages to other
     * {@link ValueManager}s of the system.
     * @param newValue the value to set
     * @throws RemoteException
     */
    private void sendRequest(int newValue) throws RemoteException {
        localTime++;
        nbAcks = 0;
        criticalSectionRequested = true;
        this.newValue = newValue;
        Message requestMsg = new Message(localTime, MessageType.REQUEST, port);
        pendingRequests.put(port, requestMsg);

        LOG.log(Level.INFO, () -> localTimeStr() + "Sending the "
                + requestMsg.getMessageType().name()  + " to other nodes");
        Iterator it = portManager.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            IValueManager manager = (IValueManager) pair.getValue();
            manager.acceptMessage(requestMsg);
        }
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
     * @param message the {@link Message} sent by some remote {@link IValueManager}
     * @throws RemoteException
     */
    public void acceptMessage(Message message) throws RemoteException {
        LOG.log(Level.INFO, () -> localTimeStr() + "message received");
        updateLocalTime(message.getTimestamp());
        LOG.log(Level.INFO, () -> localTimeStr()
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
                LiberationMessage libMessage = (LiberationMessage) message;
                this.value = libMessage.getNewValue();
                checkCriticalSection();
                break;
            default:
                LOG.log(Level.SEVERE, "Unknown message type");
        }
    }

    /**
     * This method checks if the current {@link ValueManager} can get the access to the critical section.
     * If it does, updates the value stored by all the {@link ValueManager}s of the system and sends the liberation
     * messages to them.
     * @throws RemoteException
     */
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
            LOG.log(Level.INFO, () -> localTimeStr() + "Updating value in other nodes");
            Iterator it = portManager.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                IValueManager manager = (IValueManager)pair.getValue();
                manager.acceptMessage(new LiberationMessage(localTime, MessageType.LIBERATION, port, newValue));
            }
            LOG.log(Level.INFO, () -> localTimeStr() + "Value updated, new value: " + value);
        }
    }

    /**
     * Updates the logical timestamp oh the {@link ValueManager} when the new message is received by the
     * {@link ValueManager}.
     * Used to identify the oldest request by the Lamport algorithm
     * @param remoteTimestapm the timestamp of the {@link Message} received
     */
    private void updateLocalTime(int remoteTimestapm) {
        localTime =  Math.max(localTime+1, remoteTimestapm+1);
    }

    /**
     * Local timestamp String representation for log printing
     * @return ocal timestamp String representation
     */
    private String localTimeStr() {
        return "[" + localTime + "] ";
    }

}
