package launchers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import servers.*;

public class LauncherServer {

    public static void main(String[] args) throws IOException {

        // Arguments
        Boolean launchNullServer = false;
        int portNumber = 3900;
        String dbFile = "dbdata.txt";

        // Implementation
        ServerNulthread serverNulthread = new ServerNulthread(portNumber, 4, dbFile);
        ServerPuissanthread serverPuissanthread = new ServerPuissanthread(portNumber, 10, dbFile);
        ServerForthread serverForthread = new ServerForthread(portNumber, 10, dbFile);

        Thread serverThread = new Thread(() -> {
            try {
                if (launchNullServer) {serverNulthread.start();}
                else {serverForthread.start();}
                // else {serverPuissanthread.start();}
            }
            catch(IOException e) {e.printStackTrace();}
        });
        serverThread.start();
        
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        boolean hasStopped = false;
        while ( !hasStopped && (stdIn.readLine()) != "stop" ) {
            if (launchNullServer) {serverNulthread.stop();}
            else {serverForthread.stop();}
            // else {serverPuissanthread.stop();}
            hasStopped = true;
        }
        stdIn.close();
        
        System.out.println(" ---- SERVER SHUT DOWN, ZZZzzz ! ----");
    }
}