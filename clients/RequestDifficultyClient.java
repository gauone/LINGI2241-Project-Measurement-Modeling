package clients;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;


public class RequestDifficultyClient {
    protected Socket clientSocket;
    protected PrintWriter clientOut; // to send info to server
    protected BufferedReader clientIn; // to get info from the server

    public RequestDifficultyClient(String hostName, int portNumber) {
        try {
            clientSocket = new Socket(hostName, portNumber);
            clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
            clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            BufferedReader bufferedReader = new BufferedReader(new FileReader("regex.txt"));
            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null) {
                this.clientOut.println(";" + currentLine);
                System.out.println("-- testing the request: ;" + currentLine);
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