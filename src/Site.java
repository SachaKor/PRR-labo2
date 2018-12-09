import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Site {
    private static final Logger LOG = Logger.getLogger(Site.class.getName());

    private IValueManager valueManager;

    /**
     * args[0] - {@link ValueManager}'s id
     * @param args
     * @throws RemoteException
     * @throws NotBoundException
     */
    public static void main(String ...args) throws RemoteException, NotBoundException {
        int port = Integer.parseInt(args[0]);
        Site site = new Site(port);
    }

    public void printValue() throws RemoteException {
        int value = valueManager.getValue();
        System.out.println(value);

    }

    public void setValue() throws RemoteException {
        Scanner scanner = new Scanner(System.in);
        String str;
        int value;
        while (true) {
            System.out.println("Enter the value to set:");
            str = scanner.next();
            if(str.matches("-?\\d+"))   {
                value = Integer.valueOf(str);
                break;
            }
            System.out.println("The value must be an integer");
        }
        valueManager.setValue(value);
    }

    public void askUserForCommands() throws RemoteException {
        System.out.println("NOTE: After all servers are launched, be sure you have linked " +
                "the servers between them to insure the system to be executed properly");
        boolean run = true;
        while (run) {
            System.out.println("Enter the command you would like to execute: \n" +
                    "- tap \"l\" to link the nodes of the system between them\n" +
                    "- tap \"p\" to print the current value\n" +
                    "- tap \"w\" followed by an integer to set the new value\n" +
                    "- tap \"q\" to quit the program");
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
                    } catch (Exception e) {
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

    public Site(int port) {
//        if(System.getSecurityManager() == null) {
//            System.setSecurityManager(new SecurityManager());
//        }
        try {
            Registry registry = LocateRegistry.getRegistry(Constants.SERVER_HOST);
            valueManager = (IValueManager) Naming.lookup("rmi://" + Constants.SERVER_HOST + ":" +
                    port + "/" + Constants.REMOTE_OBJ_NAME);
            LOG.log(Level.INFO, () -> Constants.REMOTE_OBJ_NAME + " is found on port " + port);
            askUserForCommands();
        } catch (RemoteException | NotBoundException | MalformedURLException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
