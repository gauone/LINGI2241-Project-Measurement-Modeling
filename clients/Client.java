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

import logger.MyLogger;

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
            //Boolean atEndOfResponse = arrivingTimes.get(arrivingTimes.size()).equals(Long.valueOf(0));
            return this.doStop == false;// && !atEndOfResponse;
        }

        // This is the function called when we launch the tread by doing Thread.start().
        @Override
        public void run() {
            String fromServer;
            try {
                Boolean isAtEndOfResponse = true; //this boolean is use to wait for the end of the response before stopping.
                while ( ( keepRunning() || !isAtEndOfResponse ) && (fromServer = clientIn.readLine()) != null ) {
                    long endTime = System.nanoTime();
                    Client.this.arrivingTimes.add(endTime);
                    System.out.println("Server: " + fromServer);

                    //we receive a \n as a end of response marker
                    if (fromServer.equals("\n") ) {
                        isAtEndOfResponse = true;
                        Client.this.arrivingTimes.add( Long.valueOf(0) );
                        System.out.println("Server: " + "\\n");
                    } else {
                        isAtEndOfResponse = false;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Stop listening the server");
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
            
            // if some requests are to be read from std
            if (inputFromStd) {
                sendRequestFromStd();
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
        this.sendingTimes.add(startTime);
        this.clientOut.println(typesString + ";" + regex);
    }

    /**
     * Send a request to the serveur.
     * @param request a (String) of type <types>;<regex>
     */
    public synchronized void sendRequest(String request) {
        long startTime = System.nanoTime();
        this.sendingTimes.add(startTime);
        this.clientOut.println(request);
    }
    
    /**
     * Listen for requests from std. Those should be of the form <types>;<regex>
     */
    public void sendRequestFromStd() {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String fromUser;
        try {
            while ( !(fromUser = stdIn.readLine()).equals("exit") ) {
                if (fromUser != null) {
                    System.out.println("Client: " + fromUser);
                    this.sendRequest(fromUser);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.stopClient();
    }

    public void stopClient(){
        System.out.println("closingClient " + this);

        // Stop the server listening thread
        this.myServerListener.doStop();

        // Compute the responses times
        ArrayList<Long> responsesTime = new ArrayList<Long>();
        Long start, stop;
        int indexArrival = 0;
        for (int i=0; i < this.sendingTimes.size(); i++) {
            start = this.sendingTimes.get(i);
            stop = this.arrivingTimes.get(indexArrival);
            while ( (!stop.equals(Long.valueOf(0))) && indexArrival <= this.arrivingTimes.size() ) {
                responsesTime.add(stop - start);
                indexArrival++;
                stop = this.arrivingTimes.get(indexArrival);
            }
            responsesTime.add(Long.valueOf(0));
            if (indexArrival < arrivingTimes.size()) {
                indexArrival = indexArrival+1;
            }
        }

        // Write the response times to a file.
        MyLogger.getInstance().println(responsesTime);
    }
}