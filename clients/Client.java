package clients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    private PrintWriter clientOut;
    private BufferedReader clientIn;

    public Client(String hostName, int portNumber) {

        try (Socket clientSocket = new Socket(hostName, portNumber);
            this.clientOut = new PrintWriter(clientSocket.getOutputStream(), true);  //to send info to server
            this.clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));) {  //to get info from server
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            
            String fromServer;
            String fromUser;
            while ((fromServer = clientIn.readLine()) != null) {
                System.out.println("Server: " + fromServer);
                if (fromServer.equals("Bye."))
                    break;
                
                fromUser = stdIn.readLine();
                if (fromUser != null) {
                    System.out.println("Client: " + fromUser);
                    clientOut.println(fromUser);
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }
    }
}
