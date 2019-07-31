import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface BasicServer extends Remote {
    List<String> showFiles(String prefix) throws RemoteException;
    boolean deleteFile(String filename) throws RemoteException;
    byte[] writeFile(String filename) throws RemoteException;
    boolean readFile(String filename, byte[] file) throws RemoteException;
}
