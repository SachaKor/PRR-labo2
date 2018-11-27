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
        // TODO: scenario
    }

    public Site() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            valueManager = (IValueManager)registry.lookup("valueManager");
        } catch (RemoteException | NotBoundException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
