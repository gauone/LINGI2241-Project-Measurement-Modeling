package clients;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;


public class ClientSkip {
    protected Socket clientSocket;
    protected PrintWriter clientOut; // to send info to server
    protected BufferedReader clientIn; // to get info from the server

    public ClientSkip(String hostName, int portNumber) {
        try {
            clientSocket = new Socket(hostName, portNumber);
            clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
            clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            BufferedReader bufferedReader = new BufferedReader(new FileReader("regex.txt"));
            String currentLine;
            int N = 0;
            while ((currentLine = bufferedReader.readLine()) != null) {
                N++;
                this.clientOut.println(";" + currentLine);
                System.out.println("-- testing the request: ;" + currentLine);
            }

            int count = 1;
            String fromServer;
            while ( ((fromServer = clientIn.readLine()) != null) && count < 2*N ) {
                if (fromServer.equals("") && count%2 == 1) {
                    count++;
                }
                else if (fromServer.equals("")) {
                    count++;
                } else {
                    // System.out.println("- Server: " + fromServer + "\n");
                }
            }

            bufferedReader.close();
            stopClient();
        } catch (UnknownHostException e) {
            System.out.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }
    }

    public void stopClient() {
        try {
            clientOut.close();
            clientIn.close();
            clientSocket.close();
        } catch (IOException e) {
            System.out.println(" IOException closing the socket, PrintWriter and BufferedReader");
            System.out.println(e.getMessage());
        }
        System.out.println(" - Finised Thread in client");
    }
}