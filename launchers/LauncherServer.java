package launchers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import servers.*;

public class LauncherServer {

    public static void main(String[] args) throws IOException {

        int portNumber = 3900;
        
        // ServerForthread serverForthread = new ServerForthread(portNumber, 10, "dbdata2.txt");
        ServerNulthread serverNulthread = new ServerNulthread(portNumber, 4, "dbdata2.txt");
        Thread serverThread = new Thread(() -> {
            try {
                // serverForthread.start();
                serverNulthread.start();
            }
            catch(IOException e) {}
        });
        serverThread.start();
        
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String fromUser;
        boolean hasStopped = false;
        while ( !hasStopped && (fromUser = stdIn.readLine()) != "stop" ) {
            // serverForthread.stop();
            serverNulthread.stop();
            hasStopped = true;
        }
        stdIn.close();
        
        System.out.println(" ---- SERVER SHUT DOWN, ZZZzzz ! ----");
        
    }
}