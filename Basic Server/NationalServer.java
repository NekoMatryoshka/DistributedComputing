import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NationalServer extends AbstractFileServer {

    private BasicServer globalServer;
    private String name;
    private String prefix;

    protected NationalServer(String globalHost, String name) throws Exception {
        super(name);
        this.name = name;
        this.prefix = name + "_";
        globalServer = (BasicServer) Naming.lookup("rmi://" + globalHost + "/" + "GlobalServer");
    }

    public List<String> showFiles(String prefix) throws RemoteException {
        List<String> res = new ArrayList<>();
        for (String name : globalServer.showFiles(this.prefix + prefix)) {
            res.add(name.replace(this.prefix, ""));
        }
        res.addAll(super.showFiles(prefix));
        return res;
    }

    public boolean deleteFile(String filename) throws RemoteException {
        return globalServer.deleteFile(this.prefix + filename) | super.deleteFile(filename);
    }

    public byte[] writeFile(String filename) throws RemoteException {
        byte[] buff = super.writeFile(filename);

        if (buff == null) {
            buff = globalServer.writeFile(prefix + filename);
        }
        return buff;
    }

    public boolean readFile(String filename, byte[] file) throws RemoteException {
        boolean res = super.readFile(filename, file);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                File file = new File(root + filename);
                byte[] buff = new byte[(int) file.length()];
                RandomAccessFile raf = null;
                try {
                    raf = new RandomAccessFile(file, "r");
                    raf.readFully(buff);
                    globalServer.readFile(prefix + filename, buff);
                } catch (Exception e) {
                    throw new RuntimeException("exception in uploading thread", e);
                } finally {
                    if (raf != null) {
                        try {
                            raf.close();
                        } catch (IOException e) {
                        }
                    }
                }
                try {
                    NationalServer.super.deleteFile(filename);
                } catch (Exception e) {
                    throw new RuntimeException("exception in uploading thread", e);
                }
            }
        }, 1000);
        return res;
    }

    public static void main(String args[]) throws Exception {
        if (args.length != 2) {
            System.out.println("Syntax - NationalServer [global host] [server name]");
            System.exit(1);
        }

        if (!args[1].matches("[a-zA-Z0-9]*")) {
            System.out.println("server name contains numbers/letters only");
            System.exit(1);
        }

        try {
            Naming.rebind(args[1], new NationalServer(args[0], args[1]));
        } catch (Exception e) {
            System.out.println("Registry fail.");
            e.printStackTrace();
        }

        System.out.println("Server ready");
    }
}
