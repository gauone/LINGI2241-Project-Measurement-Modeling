import clients.Client;

public class LauncherClient {
    public static void main(String[] args) {
        
        // if (args.length != 1) {
        //     System.err.println("Usage: java ServerNul <port number>");
        //     System.exit(1);
        // }
        // int portNumber = Integer.parseInt(args[0]);

        int portNumber = 3900;

        Client client = new Client("localhost", portNumber, true);
    }
}