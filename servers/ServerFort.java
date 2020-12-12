package servers;

import java.net.*;
import java.io.*;
import java.util.*;


/**
    Write a better server than serverNul

    This server return only the string of the line, like : 0@@@i thought that was neat => i thought that was neat
*/
public class ServerFort {



    // Port number
    int portNumber;

    // Socket
    ServerSocket serverSocket;
    Socket clientSocket;
    PrintWriter clientOut;
    BufferedReader clientIn;

    // Main Memory
    HashMap<Integer, ArrayList<String>> data = new HashMap<Integer, ArrayList<String>>(); // Hashmap with key : type (Integer); value : sentences (list of String)


    /**
     * Constructor
     * 
     * @param portNumber
     */
    public ServerFort(int portNumber) {
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

        ArrayList<String> sentences0 = new ArrayList<String>();   // List of Strings containing the Strings per line : 2.442.237
        ArrayList<String> sentences1 = new ArrayList<String>();   // List of Strings containing the Strings per line : 2.442.237
        ArrayList<String> sentences2 = new ArrayList<String>();   // List of Strings containing the Strings per line : 2.442.237
        ArrayList<String> sentences3 = new ArrayList<String>();   // List of Strings containing the Strings per line : 2.442.237
        ArrayList<String> sentences4 = new ArrayList<String>();   // List of Strings containing the Strings per line : 2.442.237
        ArrayList<String> sentences5 = new ArrayList<String>();   // List of Strings containing the Strings per line : 2.442.237
        
		BufferedReader bufferedReader = new BufferedReader(new FileReader("dbdata2.txt"));
		
		String currentLine;
        while ((currentLine = bufferedReader.readLine()) != null) {

            String[] splittedLine = currentLine.split("@@@");

			int dataType = Integer.valueOf(splittedLine[0]);
			String dataSentence = splittedLine[1].toLowerCase();
            
            switch(dataType) {
                case 0 :
                    sentences0.add(dataSentence);
                    break;
                case 1 :
                    sentences1.add(dataSentence);
                    break;
                case 2 :
                    sentences2.add(dataSentence);
                    break;
                case 3 :
                    sentences3.add(dataSentence);
                    break;
                case 4 :
                    sentences4.add(dataSentence);
                    break;
                case 5 :
                    sentences5.add(dataSentence);
                    break;
            }
            
        }
        data.put(0, sentences0);
        data.put(1, sentences1);
        data.put(2, sentences2);
        data.put(3, sentences3);
        data.put(4, sentences4);
        data.put(5, sentences5);

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

        String[] splittedLine = request.split(";");                 // Split to have the tags (String) and the regex

        if(splittedLine[0].equals("")) {                            // If the request do not contain a type, we are looking for each of them
            splittedLine[0] = "0,1,2,3,4,5";
        }

        String[] stringTypes = splittedLine[0].split(",");
        for(int i = 0; i < stringTypes.length; i++) {
                requestTypes.add(Integer.valueOf(stringTypes[i]));
        }

        regex = splittedLine[1].toLowerCase();  

        /*
         * Search of the tags & regex into the Main memory
         *    requestTypes = [1, 2, 3]
         *    regex = "second"
         */
        System.out.println("Linear search");

        ArrayList<String> sendedSentences = new ArrayList<String>();    // List of the string already sended for ONE request (to avoid duplicates)

        for(int requestType : requestTypes) {
            ArrayList<String> sentences = data.get(requestType);
            for(int i = 0; i < sentences.size(); i++) {
                if(sentences.get(i).contains(regex)) {
                    String returnSentence = sentences.get(i);
                    if(!sendedSentences.contains(returnSentence)) {     // Send only if we do not have send this string for the actual request
                        sendedSentences.add(returnSentence);
                        System.out.println("   ===> Responding " + sentences.get(i) + " to the client");
                        System.out.println("\n");
                        clientOut.println(data.get(i));
                    }
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
//  Modifications
//
//////////////////////////////////////////////////////////////////////////////////////////////////
/*
 * - Main Memory as a Hashmap
 * - Deleting the requestAllTypes boolean
 * - Add buffer off sended request to avoid duplicates with differents types (So preprocessing is not needed)
 * 
 * - Keep the most recent request ? 
 * - Multithread ?
 */