package servers;

import java.net.*;
import java.io.*;
import java.util.*;



/**
    Write a simple server that accepts requests from a client through the network.
    The server open a socket on a certain TCP port and wait for incoming requests from the client.

    This command starts the server program ServerNul so that it listens on TCP port 4444 : 
    java ServerNul 4444

    Inspired from "The Knock Knock Server" : https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html

    This server return the whole line, like : 0@@@i thought that was neat.
    I think we might olny return the string... 
*/
public class ServerNul {



    // Port number
    int portNumber;

    // Socket
    ServerSocket serverSocket;
    Socket clientSocket;
    PrintWriter clientOut;
    BufferedReader clientIn;

    // Main Memory
    List<String> data = new ArrayList<String>();            // List of Strings containing the whole line : 2.442.237
    List<Integer> dataTypes = new ArrayList<Integer>();     // List of Integers containing the types per line : 2.442.237
    List<String> dataSentences = new ArrayList<String>();   // List of Strings containing the Strings per line : 2.442.237



    /**
     * Constructor
     * 
     * @param portNumber
     */
    public ServerNul(int portNumber) {
		this.portNumber = portNumber;
    }

    
    
    /**
     * Start the simple server : 
     *    - Load the data
     *    - Open a socket
     *    - Wait far a connection
     *    - Add the request to the queue
     * 
     * @throws IOException
     */
    public void start() throws IOException {

        /*
         * Load the data into Main memory
         */
        loadMainMemory();
        
        /*
         * Open a socket
         */
        try {
            System.out.println("Opening a socket and waiting for the client");
            serverSocket = new ServerSocket(portNumber);
            clientSocket = serverSocket.accept();
            clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
            clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            System.out.println("Socket is open !");
        } catch (IOException e) {
            System.out.println("Exception when opening the socket with the portNumber : " + portNumber);
            System.out.println(e.getMessage());
        }
            
        /*
         * Read the socket (that have his own queue)
         */
        System.out.println("Reading the socket");
        String request;
        while((request = clientIn.readLine()) != null) {    // Read a request (that have the following format : "1,2,3;coucou")
            searchLine(request);
        }
        System.out.println("Ending start()");
    }



    /**
     * Load the data
     * 
     * @throws IOException
     */
    public void loadMainMemory() throws IOException {

        System.out.println("Starting LoadMemory()");
		BufferedReader bufferedReader = new BufferedReader(new FileReader("dbdata2.txt"));
		
		String currentLine = bufferedReader.readLine();
		for(int i = 0; currentLine != null; i++, currentLine = bufferedReader.readLine()) {

            data.add(currentLine);

            String[] splittedLine = currentLine.split("@@@");

			dataTypes.add(Integer.valueOf(splittedLine[0]));
			dataSentences.add(splittedLine[1].toLowerCase());
            
		}

        bufferedReader.close();
    }



    /**
     * Process a request by searching the matching line in Main memory and them back to the client
     * 
     * @param request
     */
    public void searchLine(String request) {

        System.out.println("Starting searchLine()");

        /*
         * Getting the types and the regex of the request
         */
        System.out.println("Getting types and regex from the request");
        List<Integer> requestTypes = new ArrayList<Integer>();      // List of Integer containing the tags asked by the request
        String regex;                                               // String containing the regex asked by the request
        boolean requestAllTypes = false;                            // If the request types is empty => look to all types

        String[] splittedLine = request.split(";");                 // Split to have the tags (String) and the regex

        if(splittedLine[0].equals("")) {
            requestAllTypes = true;
        }
        else {
            String[] stringTypes = splittedLine[0].split(",");
            for(int i = 0; i < stringTypes.length; i++) {
                    requestTypes.add(Integer.valueOf(stringTypes[i]));
            }
        }
        regex = splittedLine[1].toLowerCase();  

        /*
         * Linear search of the tags & regex into the Main memory
         */
        System.out.println("Linear search");
        for(int i = 0; i < dataTypes.size(); i++) {         // for each line in Main memory
            int dataType = dataTypes.get(i);

            boolean isType = true;                          // For the case when requestAllTypes == true 
            if(requestAllTypes == false) {                  // If we are not looking to all types
                isType = false;                     
                for(int requestType : requestTypes) {
                    if(requestType == dataType) {
                        isType = true;                      // if dataType correspond to one of the requestType
                    }
                }
            }

            if(isType) {
                if(dataSentences.get(i).contains(regex)) {
                    System.out.println("   ===> Responding " + data.get(i) + " to the client");
                    System.out.println("\n");
                    clientOut.println(data.get(i));         // Respond directly to the client when a match is found
                }
            }
        }
    }



    /**
     * Stop the server and close the streams
     */
    public void stop() {
        System.out.println("Stopping the server");
        try {
			serverSocket.close();
			clientSocket.close();
            clientOut.close();
            clientIn.close();
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
 * - 
 *
 *
 */