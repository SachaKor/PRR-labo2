import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IValueManager extends Remote {
    int getValue() throws RemoteException;
    void setValue(int value) throws RemoteException;
}
