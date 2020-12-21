package servers;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;



/**
    Write a simple server that accepts requests from a client through the network.
    The server open a socket on a certain TCP port and wait for incoming requests from the client.

    Inspired from "The Knock Knock Server" : https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
*/
public class ServerNulthread {

    // Port number
    int portNumber;

    // Socket
    ServerSocket serverSocket;

    // Server
    boolean active = true;

    // Threads
    int nMaxThreads;
    int nThreads = 0;

    // Main Memory
    List<Integer> dataTypes = new ArrayList<Integer>();     // List of Integers containing the types per line : 2.442.237
    List<String> dataSentences = new ArrayList<String>();   // List of Strings containing the Strings per line : 2.442.237



    /**
     * Constructor
     * 
     * @param portNumber
     */
    public ServerNulthread(int portNumber, int nMaxThreads) {
        this.portNumber = portNumber;
        this.nMaxThreads = nMaxThreads;
    }

    
    
    /**
     * Start the simple server : 
     *    - Load the data
     *    - Open a socket
     *    - Wait far a connection
     *    - Launch threads
     * 
     * @throws IOException
     */
    public void start() throws IOException {
        System.out.println(" -- Starting the server --");

        /*
         * Load the data into Main memory
         */
        loadMainMemory();
        
        /*
         * Create a socket and accept clients
         */
        try {
            // System.out.println("Creating a socket and waiting for the client");
            serverSocket = new ServerSocket(portNumber);    // Create the socket
        } catch (IOException e) {
            System.out.println("Exception when opening the socket with the portNumber : " + portNumber);
            System.out.println(e.getMessage());
        }

        while(getActive()) { // Keep nMaxThreads running
            if(nThreads < nMaxThreads) {
                Socket clientSocket = serverSocket.accept(); // Accept a client
                Runnable brain = new Brain(clientSocket, dataTypes, dataSentences);
                new Thread(brain).start();
                incrementThreads();
                System.out.println("Accepting a new client, nThreads : " + nThreads);
            }
        }
    }



    private class Brain implements Runnable {

        // Socket
        Socket clientSocket;
        PrintWriter clientOut;
        BufferedReader clientIn;

        // Main Memory
        List<Integer> dataTypes;
        List<String> dataSentences;


        public Brain(Socket clientSocket, List<Integer> dataTypes, List<String> dataSentences) {
            this.clientSocket = clientSocket;
            this.dataTypes = dataTypes;
            this.dataSentences = dataSentences;
        }


        @Override
        public void run() {
            try {
                // System.out.println("Creating the in and out streams with a client");
                clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
                clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                /*
                 * Read the socket
                 */
                // System.out.println("Reading the socket");
                String request;
                while((request = clientIn.readLine()) != null) {    // Read a request (that have the following format : "1,2,3;coucou")
                    searchLine(request);
                    clientOut.println("\n");
                }

                // System.out.println("Ending a client)");
                stopBrain();

            } catch (IOException e) {
                System.out.println("Exception in run()");
                System.out.println(e.getMessage());
            }
        }


        /**
         * Process a request by searching the matching line in Main memory and them back to the client
         * 
         * @param request
         */
        public void searchLine(String request) {
            // System.out.println("Starting searchLine()");

            /*
            * Getting the types and the regex of the request
            */
            // System.out.println("Getting types and regex from the request");
            List<Integer> requestTypes = new ArrayList<Integer>();      // List of Integer containing the tags asked by the request
            String regex;                                               // String containing the regex asked by the request

            String[] splittedLine = request.split(";", 2);              // Split to have the tags (String) and the regex

            if(splittedLine[0].equals("")) {                            // If the request do not contain a type, we are looking for each of them
                splittedLine[0] = "0,1,2,3,4,5";
            }

            String[] stringTypes = splittedLine[0].split(",");          // RequestTypes as a list of Integer
            for(int i = 0; i < stringTypes.length; i++) {
                    requestTypes.add(Integer.valueOf(stringTypes[i]));
            }

            regex = splittedLine[1];  

            
            /*
             * Linear search of the tags & regex into the Main memory
             */
            // System.out.println("Linear search");
            try {
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher;
                ArrayList<String> sendedSentences = new ArrayList<String>();                        // List of the string already sended for ONE request (to avoid duplicates)
                boolean matched = false;
                for(int i = 0; i < this.dataTypes.size(); i++) {                                         // For each line in Main memory
                    int dataType = this.dataTypes.get(i);
                    for(int requestType : requestTypes) {                                           // For each requestType
                        if(requestType == dataType) {                                               // If the type of the line match with one of the requestTypes   
                            String returnSentence = this.dataSentences.get(i);
                            matcher = pattern.matcher(returnSentence);
                            if( matcher.find() && !sendedSentences.contains(returnSentence) ) {     // If we have a match and we do not have send it already (fot this request)
                                matched = true;
                                sendedSentences.add(returnSentence);
                                // System.out.println("   ===> Responding \"" + returnSentence + "\" to the client");
                                // System.out.println("\n");
                                clientOut.println(returnSentence);
                            }
                        }
                    }
                }
                if(!matched) {
                    System.out.println("No match found for the request : " + request);
                }
            } catch (PatternSyntaxException e) {
                System.out.println("/!\\ Wrong request syntax /!\\");
            }
        }


        /**
         * Stop the brain
         */
        public void stopBrain() {
            // System.out.println("Stopping the brain");
            try {
                clientOut.close();
                clientIn.close();
                clientSocket.close();
                decrementThreads();
                System.out.println("Exiting a client, nThreads = " + nThreads);
            } catch(IOException e) {
                System.out.println("IOException at Server.stop()");
                System.out.println(e.getMessage());
            }
        }
    }  



    /**
     * Load the data
     * 
     * @throws IOException
     */
    public void loadMainMemory() throws IOException {

        // System.out.println("Starting LoadMemory()");
        BufferedReader bufferedReader = new BufferedReader(new FileReader("dbdata.txt"));
        
        String currentLine;
        while((currentLine = bufferedReader.readLine()) != null) {
            String[] splittedLine = currentLine.split("@@@");
            dataTypes.add(Integer.valueOf(splittedLine[0]));
            dataSentences.add(splittedLine[1]);
        }

        bufferedReader.close();
    }



    /**
     * Decrement the amount of threads running
     */
    public synchronized void decrementThreads() {
        nThreads--;
    }



    /**
     * Increment the amount of threads running
     */
    public synchronized void incrementThreads() {
        nThreads++;
    }


    
    public synchronized boolean getActive() {
        return active;
    }

    /**
     * Stop the server and close the streams
     */
    public void stop() {
        System.out.println(" -- Stopping the server -- ");
        active = false;
        try {
			serverSocket.close();
        } catch(IOException e) {
            System.out.println("IOException at Server.stop()");
            System.out.println(e.getMessage());
        }
    }
}

//////////////////////////////////////////////////////////////////////////////////////////////////
//
//  NOTES
//
//////////////////////////////////////////////////////////////////////////////////////////////////
/*
 * - When do we want to stop the wile loop ? 
 *
 *
 */