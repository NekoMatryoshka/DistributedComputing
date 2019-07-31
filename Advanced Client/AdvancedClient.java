import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Scanner;

public class AdvancedClient extends BasicClient {
    public AdvancedClient(String host, String name, String username) throws Exception {
        super(host, name, username);
    }

    public boolean shareFile(String filename, String toUsername) throws RemoteException {
        return server.readFile(toUsername + "_" + filename, server.writeFile(prefix + filename));
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Syntax - AdvancedClient [host] [host name] [username]");
            System.exit(1);
        }

        if (!args[1].concat(args[2]).matches("[a-zA-Z0-9]*")) {
            System.out.println("server name and username contains numbers/letters only");
            System.exit(1);
        }

        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());

        AdvancedClient client = null;
        try {
            client = new AdvancedClient(args[0], args[1], args[2]);
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
            System.out.println("Share File    --- 5;[shared username];[filename]");
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
                    case "5":
                        try {
                            boolean res5 = client.shareFile(inputs[2], inputs[1]);
                            if (res5)
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
