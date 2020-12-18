package clients;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import logger.MyLogger;

public class Client {
    protected Socket clientSocket;
    protected PrintWriter clientOut; // to send info to server
    protected BufferedReader clientIn; // to get info from the server

    protected ArrayList<Long> sendingTimes = new ArrayList<Long>();
    protected ArrayList<Long> arrivingTimes = new ArrayList<Long>();

    ServerListener myServerListener;
    Random rand = new Random();

    /**
     * Constructor. It gets the get the static variables, creates the client object
     * and start a thread listening on the socket.
     * 
     * @param hostName     a (Sting) containing the name of the server's hostname
     * @param portNumber   a (int) containing the port number to reach the server
     * @param inputFromStd a (Boolean) telling if the some requests are to be
     *                     listened from std.
     */
    public Client(String hostName, int portNumber, Boolean inputFromStd, Double lambda) {
        try {
            clientSocket = new Socket(hostName, portNumber);
            clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
            clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            this.myServerListener = new ServerListener();
            Thread thread = new Thread(this.myServerListener);
            thread.start();

            // if some requests are to be read from std
            if (inputFromStd) {
                sendRequestFromStd();
            }

            launchRequests(lambda);
            
            while ( !NSendEqualsNReceived() ) {}

            // Stop the server listening thread
            this.myServerListener.doStop();

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }
    }

    public synchronized Boolean NSendEqualsNReceived(){
        return this.sendingTimes.size() != this.arrivingTimes.size();
    }
    
    public synchronized void addToArrivalTimes(long time){
        this.arrivingTimes.add(time);
    }

    /**
     * Listen for requests from std. Those should be of the form <types>;<regex>
     */
    public void sendRequestFromStd() {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String fromUser;
        try {
            while (!(fromUser = stdIn.readLine()).equals("exit")) {
                if (fromUser != null) {
                    System.out.println(" - Client: " + fromUser + "\n");
                    this.sendRequest(fromUser);
                }
            }
            stdIn.close();
        } catch (IOException e) {
            e.printStackTrace();
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
        String typesString = types.stream().map(n -> String.valueOf(n)).collect(Collectors.joining(",")); // line from https://www.geeksforgeeks.org/java-8-streams-collectors-joining-method-with-examples/
        long startTime = System.nanoTime();
        this.sendingTimes.add(startTime);
        this.clientOut.println(typesString + ";" + regex);
    }

    /**
     * Send a request to the serveur.
     * 
     * @param request a (String) of type <types>;<regex>
     */
    public synchronized void sendRequest(String request) {
        long startTime = System.nanoTime();
        this.sendingTimes.add(startTime);
        this.clientOut.println(request);
    }

    public double getRandomExponential(double lambda) {
        return Math.log(1 - rand.nextDouble()) / (-lambda);
    }

    public void launchRequests(double lambda) {
        // int N = rand.nextInt(500);
        int N = 1;
        try {
            for (int i = 0; i < N; i++) {
                TimeUnit.SECONDS.sleep((long)getRandomExponential(lambda));
                String request = generateRequest();
                System.out.println(" - Client send the request : " + request + "\n");
                sendRequest(request);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String generateRequest() throws IOException {

        int nTypes = getRandomNumberInRange(0, 6);      // Amount of types in the request. Can be 0,1,2,3,4,5 => 6 possibilities
        ArrayList<Integer> intTypes = new ArrayList<Integer>();
        String stringRequest = "";

        for(int i = 0; i < nTypes; i++) {               // Generate random types in [0, 1, 2, 3, 4, 5]
            int type = getRandomNumberInRange(0, 5);
            if(!intTypes.contains(type)) {
                intTypes.add(type);
                String typeString = String.valueOf(type);
                typeString += ',';
                stringRequest += typeString;
            }
        }

        if(nTypes == 0) { // Handle the case when we have no type specified
            stringRequest = ",";
        }

        stringRequest = charRemoveAt(stringRequest, stringRequest.length()-1);
        stringRequest += ';';

        int entry = getRandomNumberInRange(1, 960);  // Because regex.txt have 960 lines !
        int i = 1;

        BufferedReader bufferedReader = new BufferedReader(new FileReader("regex.txt"));
        String currentLine;
        while ( ((currentLine = bufferedReader.readLine()) != null) && (i <= entry)) {
            if (i == entry) {
                stringRequest += currentLine;
            }
            i++;
        }

        bufferedReader.close();

        return stringRequest;
    }

    private int getRandomNumberInRange(int min, int max) {
        if(min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    private String charRemoveAt(String str, int p) {  
        return str.substring(0, p) + str.substring(p + 1);  
    }

    public void stopClient() {
        // Compute the responses times
        ArrayList<Long> responsesTime = new ArrayList<Long>();
        Long start, stop;
        for (int i = 0; i < this.sendingTimes.size(); i++) {
            start = this.sendingTimes.get(i);
            stop = this.arrivingTimes.get(i);
            responsesTime.add(stop - start);            
        }

        // Write the response times to a file.
        MyLogger.getInstance().println(responsesTime);

        try {
            clientOut.close();
            clientIn.close();
            clientSocket.close();
        } catch (IOException e) {
            System.err.println(" IOException closing the socket, PrintWriter and BufferedReader");
            System.err.println(e.getMessage());
        }

        System.out.println(" - Finised Thread in client \n");
    }

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
            return this.doStop == false;// && !atEndOfResponse;
        }

        // This is the function called when we launch the tread by doing Thread.start().
        @Override
        public void run() {
            try {
                int c;
                String fromServer = "";
                Boolean isEmpty = false;
                while (((isEmpty = fromServer.equals("")) || keepRunning()) && (c = clientIn.read()) > 0) {
                    if (c == '\n' && isEmpty) {
                        long endTime = System.nanoTime();
                        Client.this.addToArrivalTimes(endTime);
                        System.out.println(" - Server : ------- !! New Line !! ------ \n");
                    } else if (c == '\n') {
                        System.out.println(" - Server : " + fromServer + "\n");
                        fromServer = "";
                    } else {
                        fromServer += (char) c;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(" - Stop listening the server \n");
            Client.this.stopClient();
        }
    }
}