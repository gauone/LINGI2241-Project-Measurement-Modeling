package clients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientTest {
    
    public static void main(String[] args){
        String hostName = "localhost";
        int portNumber = 3900;
        Socket socket;
        PrintWriter clientOut;
        BufferedReader clientIn;
		try {
            socket = new Socket(hostName, portNumber);
            clientOut = new PrintWriter(socket.getOutputStream(), true);
            clientIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            Thread sending= new Thread(()-> {
                clientOut.println("0,1,2,3;second");
                clientOut.println("0,1;by");
            });
            sending.start();
            int count = 1;
            String fromServer;
            while ((fromServer = clientIn.readLine()) != null) {
                if (fromServer.equals("") && count%2 == 1){
                    System.out.println("-----new line----");
                    count++;
                }
                else if (fromServer.equals("")){
                    count++;
                } else 
                    System.out.println("Server: " + fromServer + "\n");
            }

        } catch (IOException e) {
			e.printStackTrace();
        }
        
        System.out.print("youpidouuuuu c'est fini");
		
    }
       

}