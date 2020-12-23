package launchers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import servers.*;

public class LauncherServer {

    public static void main(String[] args) throws IOException {

        // Arguments
        Boolean launchNullServer = true;
        int portNumber = 3900;
        String dbFile = "dbdata.txt";
        int nThreads = 5;

        // Implementation
        ServerNulthread serverNulthread = new ServerNulthread(portNumber, nThreads, dbFile);
        ServerPuissanthread serverPuissanthread = new ServerPuissanthread(portNumber, nThreads, dbFile);

        Thread serverThread = new Thread(() -> {
            try {
                if(launchNullServer) {serverNulthread.start();}
                else {serverPuissanthread.start();}
            }
            catch(IOException e) {e.printStackTrace();}
        });
        serverThread.start();
        
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        boolean hasStopped = false;
        while ( !hasStopped && (stdIn.readLine()) != "stop" ) {
            if (launchNullServer) {serverNulthread.stop();}
            else {serverPuissanthread.stop();}
            hasStopped = true;
        }
        stdIn.close();
        
        System.out.println(" ---- SERVER SHUT DOWN, ZZZzzz ! ----");
    }
}