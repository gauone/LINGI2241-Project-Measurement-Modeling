package launchers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import servers.*;



public class LauncherServer {

    public static void main(String[] args) throws IOException {
        
        // if (args.length != 1) {
        //     System.err.println("Usage: java ServerNul <port number>");
        //     System.exit(1);
        // }
        // int portNumber = Integer.parseInt(args[0]);

        int portNumber = 21144;

        /*
         * ServerNul
         */
        // ServerNul serverNul = new ServerNul(portNumber);
        // serverNul.start();
        // serverNul.stop();

        /*
         * serverBon
         */
        // ServerBon serverBon = new ServerBon(portNumber);
        // serverBon.start();
        // serverBon.stop();

        /*
         * serverFort
         */
        // ServerFort serverFort = new ServerFort(portNumber);
        // serverFort.start();
        // serverFort.stop();

        /*
         * serverNulthread
         */
        // ServerNulthread serverNulthread = new ServerNulthread(portNumber, 2);
        // serverNulthread.start();
        // serverNulthread.stop();

        /*
         * serverNulthread
         */
        ServerForthread serverForthread = new ServerForthread(portNumber, 10);
        Thread serverThread = new Thread(() -> {
            try {
                
                serverForthread.start();
            }
            catch(IOException e) {}
        });
        serverThread.start();
        
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String fromUser;
        boolean hasStopped = false;
        while ( !hasStopped && (fromUser = stdIn.readLine()) != "stop" ) {
            serverForthread.stop();
            hasStopped = true;
        }
        stdIn.close();
        
        System.out.println(" ---- SERVER SHUT DOWN, ZZZzzz ! ----");
    }
}