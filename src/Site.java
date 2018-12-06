import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Site {
    private static final Logger LOG = Logger.getLogger(Site.class.getName());

    private IValueManager valueManager;
    public static void main(String ...args) throws RemoteException, NotBoundException {
        Site site = new Site();
    }

    public void printValue() throws RemoteException {
        int value = valueManager.getValue();
        System.out.println(value);

    }

    public void setValue(int value) throws RemoteException {
        valueManager.setValue(value);
    }

    public Site() {
//        if(System.getSecurityManager() == null) {
//            System.setSecurityManager(new SecurityManager());
//        }
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            valueManager = (IValueManager)registry.lookup("ValueManager");
            setValue(2);
            printValue();
        } catch (RemoteException | NotBoundException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
