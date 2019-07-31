import java.rmi.Naming;
import java.rmi.RemoteException;

public class GlobalServer extends AbstractFileServer{

    protected GlobalServer() throws RemoteException {
        super("GlobalServer");
    }

    public static void main(String args[]) {
        try {
            Naming.rebind("GlobalServer", new GlobalServer());
        }catch(Exception e){
            System.out.println("Registry fail.");
            e.printStackTrace();
        }
        System.out.println("Server ready");
    }
}
