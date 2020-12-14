import java.io.IOException;

import servers.*;
import clients.*;



public class LauncherServer {

    public static void main(String[] args) throws IOException {
        
        // if (args.length != 1) {
        //     System.err.println("Usage: java ServerNul <port number>");
        //     System.exit(1);
        // }
        // int portNumber = Integer.parseInt(args[0]);

        int portNumber = 3900;

        /*
         * ServerNul
         */
        // ServerNul serverNul = new ServerNul(portNumber);
        // serverNul.start();
        // serverNul.stop();

        /*
         * ServerBon
         */
        ServerBon ServerBon = new ServerBon(portNumber);
        ServerBon.start();
        ServerBon.stop();
    }
}