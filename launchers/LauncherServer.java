package launchers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import servers.*;



public class LauncherServer {

    public static void main(String[] args) throws IOException {

        int portNumber = 21144;

        ServerNulthread serverNulthread = new ServerNulthread(portNumber, 4);
        ServerForthread serverForthread = new ServerForthread(portNumber, 10);
        Thread serverThread = new Thread(() -> {
            try {
                serverNulthread.start();
                //serverForthread.start();
            }
            catch(IOException e) {}
        });
        serverThread.start();
        
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String fromUser;
        boolean hasStopped = false;
        while ( !hasStopped && (fromUser = stdIn.readLine()) != "stop" ) {
            //serverForthread.stop();
            serverNulthread.stop();
            hasStopped = true;
        }
        stdIn.close();
        
        System.out.println(" ---- SERVER SHUT DOWN, ZZZzzz ! ----");
        
    }
}