package clients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

public class Client {
    static Socket clientSocket;
    static PrintWriter clientOut;      // to send info to server
    static BufferedReader clientIn;    // to get info from the server

    /**
     * Inner class listening on the socket's server
     * 
     * Copyright: Stopping inmplementation from http://tutorials.jenkov.com/java-concurrency/creating-and-starting-threads.html
     */
    private class ServerListener implements Runnable {
        //this boolean is set to true when the thread should stop
        private boolean doStop = false;

        //method to call from outside of the thread to stop the thread
        public synchronized void doStop() {
            this.doStop = true;
        }
    
        private synchronized boolean keepRunning() {
            return this.doStop == false;
        }

        // This is the function called when we launch the tread by doing Thread.start().
        @Override
        public void run() {
            // TODO Auto-generated method stub
            
        }
        
    }

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

            Boolean InputFromStd = true;

            String fromServer;
            while ((fromServer = clientIn.readLine()) != null) {

                if(InputFromStd){
                    String fromUser;
                    fromUser = stdIn.readLine();
                    if (fromUser != null) {
                        System.out.println("Client: " + fromUser);
                        clientOut.println(fromUser);
                    }
                }

                System.out.println("Server: " + fromServer);
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }
    }

    /**
     * Send a request to the serveur. The request is of the form: <types>;<regex>
     * @param types (String) a comma-separated list of integers without spaces between the elements of the list
     * @param regex (String) a string containing a regular expression
     */
    public void sendRequest(String types, String regex) {
        clientOut.println(types + ";" + regex);
    }

    /**
     * Send a request to the serveur. The request is of the form: String <types>;<regex> e.g.: "1,2,3,4;Coucou"
     * @param types (List<String>) a comma-separated list of integers
     * @param regex (String) a string containing a regular expression
     */
    public void sendRequest(List<Integer> types, String regex) {
        String typesString = types.stream().map(n -> String.valueOf(n)).collect(Collectors.joining(",")); //line from https://www.geeksforgeeks.org/java-8-streams-collectors-joining-method-with-examples/
        clientOut.println(typesString + ";" + regex);
    }


}
