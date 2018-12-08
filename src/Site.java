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
    private static String valueManagerName;

    /**
     * args[0] - {@link ValueManager}'s id
     * @param args
     * @throws RemoteException
     * @throws NotBoundException
     */
    public static void main(String ...args) throws RemoteException, NotBoundException {
        int port = Integer.parseInt(args[0]);
        int id = Integer.parseInt(args[1]);
        valueManagerName = Constants.REMOTE_OBJ_NAME +  id;
        System.out.println(valueManagerName);
        Site site = new Site(id, port);
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

    public void askUserForCommands() {
        while (true) {
            System.out.println("Enter the command you would like to execute: \n" +
                    "- tap \"p\" to print the current value\n" +
                    "- tap \"w\" followed by an integer to set the new value\n" +
                    "- tap \"q\" to quit the program");
            Scanner scanner = new Scanner(System.in);
            String command = scanner.next();
            break;
        }
    }

    public Site(int id, int port) {
//        if(System.getSecurityManager() == null) {
//            System.setSecurityManager(new SecurityManager());
//        }
        try {
            Registry registry = LocateRegistry.getRegistry(Constants.SERVER_HOST);
            valueManager = (IValueManager) Naming.lookup("rmi://localhost:/" + port + "/" + valueManagerName);
            LOG.log(Level.INFO, () -> valueManagerName + " is found");
            setValue();
            printValue();
            askUserForCommands();
        } catch (RemoteException | NotBoundException | MalformedURLException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
