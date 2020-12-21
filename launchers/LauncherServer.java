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
        String dbFile = "dbdata2.txt";

        // implementation
        ServerNulthread serverNulthread = new ServerNulthread(portNumber, 4, dbFile);
        ServerForthread serverForthread = new ServerForthread(portNumber, 10, dbFile);

        Thread serverThread = new Thread(() -> {
            try {
                if (launchNullServer) {serverNulthread.start();}
                else{ serverForthread.start();}
            }
            catch(IOException e) { e.printStackTrace();}
        });
        serverThread.start();
        
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String fromUser;
        boolean hasStopped = false;
        while ( !hasStopped && (fromUser = stdIn.readLine()) != "stop" ) {
            if (launchNullServer) {serverNulthread.stop();}
            else{ serverForthread.stop();}
            hasStopped = true;
        }
        stdIn.close();
        
        System.out.println(" ---- SERVER SHUT DOWN, ZZZzzz ! ----");
        
    }
}