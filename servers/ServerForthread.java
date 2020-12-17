package servers;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
    Write a better server than serverNul

    This server return only the string of the line, like : 0@@@i thought that was neat => i thought that was neat
*/
public class ServerForthread implements Runnable {

    // Thread
    protected boolean isStopped = false;
    protected Thread runningThread = null;

    // Port number
    int portNumber;

    // Socket
    ServerSocket serverSocket;
    Socket clientSocket;
    PrintWriter clientOut;
    BufferedReader clientIn;

    // Main Memory
    HashMap<Integer, ArrayList<String>> data = new HashMap<Integer, ArrayList<String>>(); // Hashmap with key : type (Integer); value : sentences (list of String)

    // Cache
    HashMap<String, ArrayList<String>> cache = new HashMap<String, ArrayList<String>>(); // Hashmap with key : request (String); value : sendedSentences (list of String)
    HashMap<String, Integer> cacheUseBit = new HashMap<String, Integer>(); // Hashmap with key : request (String); value : usedBit (Integer)
    int cacheMaxSize = 10; // We keep maximum the last 100 requests with their responses
    int cacheSize = 0;      // Amount of request in the cache
    int nReset = 3;



    /**
     * Constructor
     * 
     * @param portNumber
     */
    public ServerForthread(int portNumber) {
		this.portNumber = portNumber;
    }



    /**
     * 
     */
    public void run() {
        synchronized(this) {
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();
        while(!isStopped()) {
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Server Stopped.") ;
                    return;
                }
                throw new RuntimeException(
                    "Error accepting client connection", e);
            }
            new Thread(
                new WorkerRunnable(
                    clientSocket, "Multithreaded Server")
            ).start();
        }
        System.out.println("Server Stopped.") ;
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
            System.out.println(" - Opening a socket and waiting for the client");
            serverSocket = new ServerSocket(portNumber);
            clientSocket = serverSocket.accept();
            clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
            clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            System.out.println(" - Socket is open !");
        } catch (IOException e) {
            System.out.println("Exception when opening the socket with the portNumber : " + portNumber);
            System.out.println(e.getMessage());
        }
            
        /*
         * Read the socket (that have his own queue)
         */
        System.out.println(" - Reading the socket");
        int nRequests = 0;
        String request;
        while((request = clientIn.readLine()) != null) {    // Read a request (that have the following format : "1,2,3;coucou")
            System.out.println("");
            System.out.println(" - Reading a new request");
            if(nRequests == nReset) {
                searchLine(request, true);
                nRequests = 0;
            }
            else {
                searchLine(request, false);
                nRequests++;
            }
            clientOut.print("\n");
        }
        System.out.println(" - Ending start()");
    }



    /**
     * Load the data
     * 
     * @throws IOException
     */
    public void loadMainMemory() throws IOException {

        System.out.println(" - Starting LoadMemory()");

        ArrayList<String> sentences0 = new ArrayList<String>();   // List of Strings containing the Strings per line : 2.442.237
        ArrayList<String> sentences1 = new ArrayList<String>();   // List of Strings containing the Strings per line : 2.442.237
        ArrayList<String> sentences2 = new ArrayList<String>();   // List of Strings containing the Strings per line : 2.442.237
        ArrayList<String> sentences3 = new ArrayList<String>();   // List of Strings containing the Strings per line : 2.442.237
        ArrayList<String> sentences4 = new ArrayList<String>();   // List of Strings containing the Strings per line : 2.442.237
        ArrayList<String> sentences5 = new ArrayList<String>();   // List of Strings containing the Strings per line : 2.442.237
        
		BufferedReader bufferedReader = new BufferedReader(new FileReader("dbdataMini.txt"));
		
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
    public void searchLine(String request, boolean resetBits) {

        System.out.println(" - Starting searchLine()");

        /*
         * Reset Use Bits
         */
        if(resetBits) {
            cacheUseBit.replaceAll((key, value) -> 0);    // Replace all values of the HashMap by zero : Set all use bits to 0
        }


        /*
         * Search in cache
         */
        System.out.println(" - Search in Cache");

        if(cache.containsKey(request)) {
            System.out.println(" - It is in Cache");

            ArrayList<String> sendedSentences = cache.get(request);
            for(String sendedSentence : sendedSentences) {
                System.out.println("   ===> Responding (from cache) \"" + sendedSentence + "\" to the client");
                System.out.println("\n");
                clientOut.println(sendedSentence);
                cacheUseBit.put(request, 1);
            } 
        }
        else {
            System.out.println(" - It is NOT in Cache");

            /*
             * Getting the types and the regex of the request
             */
            System.out.println(" - Getting types and regex from the request");

            List<Integer> requestTypes = new ArrayList<Integer>();      // List of Integer containing the tags asked by the request
            String regex;                                               // String containing the regex asked by the request

            String[] splittedLine = request.split(";", 2);              // Split to have the tags (String) and the regex

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
            System.out.println(" - Linear search in Main memory (hashMap)");

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher;
            ArrayList<String> sendedSentences = new ArrayList<String>();    // List of the string already sended for ONE request (to avoid duplicates)

            for(int requestType : requestTypes) {
                ArrayList<String> sentences = data.get(requestType);
                for(int i = 0; i < sentences.size(); i++) {
                    String returnSentence = sentences.get(i);
                    matcher = pattern.matcher(returnSentence);
                    if( matcher.find() && !sendedSentences.contains(returnSentence) ) {     // If we have a match and we do not have send it already (fot this request)
                        sendedSentences.add(returnSentence);
                        System.out.println("\n");
                        System.out.println("   ===> Responding (from main memory) \"" + returnSentence + "\" to the client");
                        clientOut.println(returnSentence);
                    }
                }
            }
            

            /*
             * Put the request in cache
             */
            System.out.println(" - Handle the instruction in cache");

            if(cacheSize == cacheMaxSize) {
                System.out.println(" - One entry have to be removed : cacheSize == cacheMaxSize");
                String removedKey = "null";
                boolean search = true;
                Set<String> keySet = cacheUseBit.keySet();
                for (Iterator<String> it = keySet.iterator(); it.hasNext() && search;) {    // Looking for a (the first) key with use bit == 0
                        String key = it.next();
                        if(cacheUseBit.get(key) == 0) {
                                removedKey = key;
                                search = false;
                        }
                }
    
                // Remove the key of the cache (do not forget the use bit)
                if(removedKey.equals("null")) {
                    System.out.println(" /!\\ The cache did not find an entry with the use bit at 0 /!\\ ");
                }
                else {
                    System.out.println(" - Removed entry " + removedKey);
                    cache.remove(removedKey);
                    cacheUseBit.remove(removedKey);
                    cacheSize--;
                }
            }

            // Put the new request in cache
            System.out.println(" - Put the instruction in cache");
            cache.put(request, sendedSentences);
            cacheUseBit.put(request, 1);
            cacheSize++;
        }
    }



    /**
     * Return true if this server is stopped ()
     */
    private synchronized boolean isStopped() {
        return this.isStopped;
    }



    /**
     * Stop the server and close the streams
     */
    public synchronized void stop() {
        System.out.println(" - Stopping the server");
        isStopped = true;
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
 * - Main Memory as a Hashmap (key = type; value = sentences)
 * - Deleting the requestAllTypes boolean
 * - Add buffer off sended request to avoid duplicates with differents types (So preprocessing is not needed)
 * 
 * - Keep the most recent request ? 
 * - Multithread ?
 */