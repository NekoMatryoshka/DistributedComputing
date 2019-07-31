import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;


public abstract class AbstractFileServer extends UnicastRemoteObject implements BasicServer {
    protected File dir;
    protected String root;

    protected AbstractFileServer(String name) throws RemoteException {
        super();
        root = ".\\" + name + "\\";
        dir = new File(root);
        dir.mkdirs();
    }

    public List<String> showFiles(String prefix) throws RemoteException {
        List<String> files = new ArrayList<>();
        for (String name : dir.list())
            if (name.startsWith(prefix))
                files.add(name);
        return files;
    }

    public boolean deleteFile(String filename) throws RemoteException {
        for (File file : dir.listFiles())
            if (file.getName().equals(filename)) {
                return file.delete();
            }
        return false;
    }

    public byte[] writeFile(String filename) throws RemoteException {
        for (File file : dir.listFiles())
            if (file.getName().equals(filename)) {
                byte[] buff = new byte[(int) file.length()];
                RandomAccessFile raf = null;
                try {
                    raf = new RandomAccessFile(file, "r");
                    raf.readFully(buff);
                } catch (IOException e) {
                    return null;
                } finally {
                    if (raf != null) {
                        try {
                            raf.close();
                        } catch (IOException e) {
                        }
                    }
                }
                return buff;
            }
        return null;
    }

    public boolean readFile(String filename, byte[] file) throws RemoteException {
        File newFile = new File(root + filename);
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(newFile, "rw");
            newFile.createNewFile();
            raf.write(file);
        } catch (IOException e) {
            return false;
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                }
            }
        }
        return true;
    }
}
