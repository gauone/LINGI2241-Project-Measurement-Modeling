package clients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    static Socket clientSocket;
    static PrintWriter clientOut;      // to send info to server
    static BufferedReader clientIn;    // to get info from the server

    /**
     * Constructor. It gets the get the static variables, creates the client object and start a loop listening on the socket
     * @param hostName a (Sting) containing the name of the server's hostname
     * @param portNumber a (int) containing the port number to reach the server
     */
    public Client(String hostName, int portNumber) {
        try {
            clientSocket = new Socket(hostName, portNumber);
            clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
            clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        
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

    //public addRequest(String request) {}

}
