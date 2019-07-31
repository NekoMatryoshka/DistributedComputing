import java.rmi.RemoteException;

public interface AdvancedServer extends BasicServer {
    public boolean delete(String filename) throws RemoteException;
    public boolean read(String filename, byte[] file) throws RemoteException;
}
