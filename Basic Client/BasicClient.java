import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BasicClient {
    protected BasicServer server;
    private String username;
    protected String prefix;

    public BasicClient(String host, String name, String username) throws Exception {
        this.username = username;
        prefix = username + "_";
        server = (BasicServer) Naming.lookup("rmi://" + host + "/" + name);
    }

    public List<String> showFiles() throws RemoteException {
        List<String> res = new ArrayList<>();
        for (String name : server.showFiles(prefix)) {
            res.add(name.replace(prefix, ""));
        }
        return res;
    }

    public boolean deleteFile(String filename) throws RemoteException {
        return server.deleteFile(prefix + filename);
    }

    public boolean uploadFile(String from, String filename) throws IOException {
        File file = new File(from);
        byte[] buff = new byte[(int) file.length()];
        RandomAccessFile raf = new RandomAccessFile(from, "r");
        raf.readFully(buff);
        raf.close();
        return server.readFile(prefix + filename, buff);
    }

    public boolean downloadFile(String to, String filename) throws IOException {
        File file = new File(to);

        boolean alreadyExists = !file.createNewFile();
        if (alreadyExists)
            throw new IllegalArgumentException("to path already exists");

        byte[] buff = server.writeFile(prefix + filename);
        if (buff == null)
            return false;

        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.write(buff);
        raf.close();
        return true;
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Syntax - BasicClient [host] [host name] [username]");
            System.exit(1);
        }

        if (!args[1].concat(args[2]).matches("[a-zA-Z0-9]*")) {
            System.out.println("server name and username contains numbers/letters only");
            System.exit(1);
        }

        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());

        BasicClient client = null;
        try {
            client = new BasicClient(args[0], args[1], args[2]);
        } catch (Exception e) {
            System.out.println("network fail");
            e.printStackTrace();
            System.exit(1);
        }
        Scanner sc = new Scanner(System.in);

        for (; ; ) {
            System.out.println("Show Files    --- 1");
            System.out.println("Delete File   --- 2;[filename]");
            System.out.println("Upload File   --- 3;[source file];[filename]");
            System.out.println("Download File --- 4;[destination file];[filename]");
            System.out.println("Quit -- Other input");
            System.out.print("Choice: ");

            String[] inputs = sc.nextLine().split(";");

            try {
                switch (inputs[0]) {
                    case "1":
                        List<String> files = client.showFiles();
                        if (files.size() == 0)
                            System.out.println("   No file uploaded yet");
                        else
                            for (String file : files)
                                System.out.println("   " + file);
                        break;
                    case "2":
                        boolean res2 = client.deleteFile(inputs[1]);
                        if (res2)
                            System.out.println("Delete Done");
                        else
                            System.out.println("File not found");
                        break;
                    case "3":
                        try {
                            boolean res3 = client.uploadFile(inputs[1], inputs[2]);
                            if (res3)
                                System.out.println("Upload Done");
                            else
                                System.out.println("File already exists on server");
                        } catch (IOException e) {
                            System.out.println("File system error");
                        }
                        break;
                    case "4":
                        try {
                            boolean res4 = client.downloadFile(inputs[1], inputs[2]);
                            if (res4)
                                System.out.println("Download Done");
                            else
                                System.out.println("File not found on server");
                        } catch (IOException e) {
                            System.out.println("File system error");
                        }
                        break;
                    default:
                        System.out.println("Terminate");
                        System.exit(0);
                        break;
                }
            } catch (RemoteException e) {
                System.out.println("network fail");
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
