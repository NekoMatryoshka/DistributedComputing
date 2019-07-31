import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class P2PServer extends AbstractFileServer implements AdvancedServer {

    private int id;
    private List<AdvancedServer> servers;

    protected P2PServer(int id) throws Exception {
        super("Server"+id);
        this.id=id;
        this.servers = new ArrayList<>();
    }

    public void addServer(AdvancedServer s){
        servers.add(s);
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean deleteFile(String filename) throws RemoteException {
        byte[] buff = super.writeFile(filename);
        delete(filename);
        try {
            for (AdvancedServer server : servers)
                if(!server.delete(filename)){
                    throw new RemoteException();
                }
        } catch (RemoteException e) {
            read(filename, buff);
            for (AdvancedServer server : servers) {
                try {
                    server.read(filename, buff);
                } catch (IOException ex) {
                    continue;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean readFile(String filename, byte[] file) throws RemoteException {
        read(filename,file);
        try {
            for (AdvancedServer server : servers)
                if(!server.read(filename,file)){
                    throw new RemoteException();
                }
        } catch (RemoteException e) {
            delete(filename);
            for (AdvancedServer server : servers) {
                try {
                    server.delete(filename);
                } catch (IOException ex) {
                    continue;
                }
            }
            return false;
        }
        return true;
    }


    @Override
    public boolean delete(String filename) throws RemoteException {
        return super.deleteFile(filename);
    }

    @Override
    public boolean read(String filename, byte[] file) throws RemoteException {
        return super.readFile(filename, file);
    }

    public static void main(String args[]) {
        // check args.
        if (args.length != 2) {
            System.out.println("Syntax - P2PServer [server number] [host].");
            System.exit(1);
        }

        int serverNumber = 0;
        try {
            serverNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid Server Number - must be a number.");
            System.exit(1);
        }

        List<P2PServer> servers = new ArrayList<>();
        // initialize and bind every server.
        for (int i = 1; i <= serverNumber; i++) {
            try {
                P2PServer server = new P2PServer(i);
                Naming.rebind("Server" + i, server);
                servers.add(server);
            } catch (Exception e) {
                System.out.println("Server " + i + " fail.");
                System.exit(1);
            }
        }

        // after all servers are initialized, connect them together.
        for (P2PServer server : servers) {
            try {
                for (int i = 1; i <= serverNumber; i++) {
                    if (i != server.getId())
                        server.addServer((AdvancedServer) Naming.lookup("rmi://" + args[1] + "/Server" + i));
                }
            } catch (Exception e) {
                System.out.println("Registry fail.");
                System.exit(1);
            }
        }

        System.out.println("Servers ready.");
    }
}
