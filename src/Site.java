import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class represents the client side of the application.
 * Communicates with the user and remote {@link IValueManager}s to set or print the value.
 *
 * Authors: Samuel Mayor, Alexandra Korukova
 */
public class Site {
    private static final Logger LOG = Logger.getLogger(Site.class.getName());

    private IValueManager valueManager;

    /**
     * @param args
     *  - args[0] - associated {@link ValueManager}'s port
     * @throws RemoteException
     * @throws NotBoundException
     */
    public static void main(String ...args) throws RemoteException, NotBoundException {
        int port = Integer.parseInt(args[0]);
        Site site = new Site(port);
    }

    /**
     * Prints the menu and executes the tasks demanded by the user
     * @throws RemoteException
     */
    public void userCommands() throws RemoteException {
        boolean run = true;
        boolean serversLinked = false;
        while (run) {
            System.out.println("Enter the command you would like to execute: \n" +
                    "- tap \"l\" to link the nodes of the system between them\n" +
                    "- tap \"p\" to print the current value\n" +
                    "- tap \"w\" followed by an integer to set the new value\n" +
                    "- tap \"q\" to quit the program");
            if(!serversLinked) {
                System.out.println("NOTE: After all servers are launched, be sure you have linked " +
                        "the servers between them to insure the system to be executed properly\n" +
                        "use \"l\" command");
            }
            Scanner scanner = new Scanner(System.in);
            String command = scanner.next();
            switch (Character.toUpperCase(command.charAt(0))) {
                case Constants.PRINT: {
                    int value = valueManager.getValue();
                    System.out.println(value);
                    break;
                }
                case Constants.WRITE: {
                    String valueStr = scanner.next();
                    try {
                        int value = Integer.parseInt(valueStr);
                        valueManager.setValue(value);
                    } catch (NumberFormatException e) {
                        System.out.println(valueStr);
                        System.out.println(Constants.UNKNOWN_COMMAND);
                    }
                    break;
                }
                case Constants.LOOKUP: {
                    try {
                        valueManager.lookup();
                    } catch (NotBoundException | MalformedURLException e) {
                        LOG.log(Level.SEVERE, e.getMessage(), e);
                    }
                    serversLinked = true;
                    break;
                }
                case Constants.QUIT: {
                    run = false;
                    break;
                }
                default:
                    System.out.println(Constants.UNKNOWN_COMMAND);
            }
        }
    }

    /**
     * Constructor
     * @param port the associated remote {@link IValueManager}'s port
     */
    public Site(int port) {
//        if(System.getSecurityManager() == null) {
//            System.setSecurityManager(new SecurityManager());
//        }
        try {
            Registry registry = LocateRegistry.getRegistry(Constants.SERVER_HOST);
            valueManager = (IValueManager) Naming.lookup("rmi://" + Constants.SERVER_HOST + ":" +
                    port + "/" + Constants.REMOTE_OBJ_NAME);
            LOG.log(Level.INFO, () -> Constants.REMOTE_OBJ_NAME + " is found on port " + port);
            userCommands();
        } catch (RemoteException | NotBoundException | MalformedURLException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
