import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IValueManager extends Remote {
    int getValue() throws RemoteException;
    void setValue(int value) throws RemoteException;
    void lookup() throws RemoteException, NotBoundException, MalformedURLException;
    void sendRequest(int newValue) throws RemoteException;
    void acceptMessage(Message message) throws RemoteException;
    void updateValue(int newValue) throws RemoteException;
}
