package clients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Client {
    protected Socket clientSocket;
    protected PrintWriter clientOut; // to send info to server
    protected BufferedReader clientIn; // to get info from the server
    
    protected ArrayList<Long> sendingTimes = new ArrayList<Long>();
    protected ArrayList<Long> arrivingTimes = new ArrayList<Long>();

    ServerListener myServerListener;

    /**
     * Inner class listening on the socket's server
     * 
     * Copyright: Stopping inmplementation from
     * http://tutorials.jenkov.com/java-concurrency/creating-and-starting-threads.html
     */
    private class ServerListener implements Runnable {
        // this boolean is set to true when the thread should stop
        private boolean doStop = false;

        // method to call from outside of the thread to stop the thread
        public synchronized void doStop() {
            this.doStop = true;
        }

        // method to look if doStop is at false
        private synchronized boolean keepRunning() {
            return this.doStop == false;
        }

        // This is the function called when we launch the tread by doing Thread.start().
        @Override
        public void run() {
            String fromServer;
            try {
                while (keepRunning() && (fromServer = clientIn.readLine()) != null) {
                    long endTime = System.nanoTime();
                    Client.this.arrivingTimes.add(endTime);
                    System.out.println("Server: " + fromServer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Constructor. It gets the get the static variables, creates the client object
     * and start a thread listening on the socket.
     * 
     * @param hostName   a (Sting) containing the name of the server's hostname
     * @param portNumber a (int) containing the port number to reach the server
     * @param inputFromStd a (Boolean) telling if the some requests are to be listened from std.
     */
    public Client(String hostName, int portNumber, Boolean inputFromStd) {
        try {
            clientSocket = new Socket(hostName, portNumber);
            clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
            clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
            this.myServerListener = new ServerListener();
            Thread thread = new Thread( this.myServerListener );
            thread.start();
            
            // if the first request is to be read from std
            if (inputFromStd) {
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
                String fromUser;
                fromUser = stdIn.readLine();
                if (fromUser != null) {
                    System.out.println("Client: " + fromUser);
                    clientOut.println(fromUser);
                }
            }

            if (inputFromStd) {
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
                String fromUser;
                while ((fromUser = stdIn.readLine()) != "exit") {
                    if (fromUser != null) {
                        System.out.println("Client: " + fromUser);
                        clientOut.println(fromUser);
                    }
                }
                
                System.out.println("exiting the client");
                this.stopClient();
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
     * 
     * @param types (String) a comma-separated list of integers without spaces
     *              between the elements of the list
     * @param regex (String) a string containing a regular expression
     */
    public synchronized void sendRequest(String types, String regex) {
        long startTime = System.nanoTime();
        sendingTimes.add(startTime);
        clientOut.println(types + ";" + regex);
    }

    /**
     * Send a request to the serveur. The request is of the form: String
     * <types>;<regex> e.g.: "1,2,3,4;Coucou"
     * 
     * @param types (List<String>) a comma-separated list of integers
     * @param regex (String) a string containing a regular expression
     */
    public synchronized void sendRequest(List<Integer> types, String regex) {
        String typesString = types.stream().map(n -> String.valueOf(n)).collect(Collectors.joining(",")); //line from https://www.geeksforgeeks.org/java-8-streams-collectors-joining-method-with-examples/
        long startTime = System.nanoTime();
        sendingTimes.add(startTime);
        clientOut.println(typesString + ";" + regex);
    }

    public void stopClient(){
        this.myServerListener.doStop();
        int minLen = java.lang.Math.min(this.sendingTimes.size(), this.arrivingTimes.size());
        StringBuffer strBuf = new StringBuffer(minLen);
        for (int i=0; i < minLen ;i++) {
            strBuf.append(this.arrivingTimes.get(i) - this.sendingTimes.get(i) );
        }
        System.out.println("Results: " + strBuf.toString());
    }
}
