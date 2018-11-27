import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ValueManager extends UnicastRemoteObject implements IValueManager {

    private static final Logger LOG = Logger.getLogger(ValueManager.class.getName());

    private int value;

    public ValueManager() throws RemoteException {
        super();
    }

    public static void main(String ...args) throws RemoteException, AlreadyBoundException{
        Registry registry = null;
        registry = LocateRegistry.createRegistry(1099);
        registry.bind("valueManager", new ValueManager());
    }

    @Override
    public int getValue() throws RemoteException {
        // TODO
        return value;
    }

    @Override
    public void setValue() throws RemoteException {
        // TODO
        this.value = value;
    }
}
