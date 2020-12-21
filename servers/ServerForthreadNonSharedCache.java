package servers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
    Write a better server than serverNulthread

    This server return only the string of the line, like : 0@@@i thought that was neat => i thought that was neat
*/
public class ServerForthreadNonSharedCache {

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
    HashMap<Integer, ArrayList<String>> data = new HashMap<Integer, ArrayList<String>>(); // Hashmap with key : type
                                                                                          // (Integer); value :
                                                                                          // sentences (list of String)

    /**
     * Constructor
     * 
     * @param portNumber
     */
    public ServerForthreadNonSharedCache(int portNumber, int nMaxThreads) {
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
        System.out.println(" -- Starting the server");
        /*
         * Load the data into Main memory
         */
        loadMainMemory();
        
        /*
         * Create a socket and accept clients
         */
        try {
            serverSocket = new ServerSocket(portNumber);    // Create the socket
        } catch (IOException e) {
            System.out.println("/!\\Exception when opening the socket with the portNumber : " + portNumber + " /!\\");
            System.err.println(e.getMessage());
        }

        while(getActive()) { // Keep nMaxThreads running
            if(nThreads < nMaxThreads) {
                Socket clientSocket = serverSocket.accept(); // Accept a client
                Runnable brain = new Brain(clientSocket, data);
                new Thread(brain).start();
                incrementThreads();
            }
        }
    }



    private class Brain implements Runnable {

        // Socket
        Socket clientSocket;
        PrintWriter clientOut;
        BufferedReader clientIn;

        // Main Memory
        HashMap<Integer, ArrayList<String>> data;

        // Cache
        HashMap<String, ArrayList<String>> cache = new HashMap<String, ArrayList<String>>();    // Hashmap with key : request; value : sendedSentences
        HashMap<String, Integer> cacheUseBit = new HashMap<String, Integer>();                  // Hashmap with key : request; value : usedBit
        int cacheMaxSize = 10;                                                                  // We keep maximum the last 100 requests with their responses
        int cacheSize = 0;                                                                      // Amount of request in the cache
        int nReset = 3;                                                                         // Reset of the use bits each nReset instructions
        int nRequests = 0;                                                                      // Amount of instructions from the last use bits reset
        boolean resetBits = false;                                                              // Set to true when we have to reset the use bits


        public Brain(Socket clientSocket, HashMap<Integer, ArrayList<String>> data) {
            this.clientSocket = clientSocket;
            this.data = data;
        }


        @Override
        public void run() {
            System.out.println(" * Beginning of a thread");
            try {
                clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
                clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                /*
                 * Read the socket
                 */
                String request;
                while((request = clientIn.readLine()) != null) {    // Read a request (that have the following format : "1,2,3;coucou")
                    if(nRequests == nReset) {                       // We have to reset the use bits !
                        resetBits = true;
                    }
                    searchLine(request);
                    System.out.println("\n");
                    System.out.println(" * Request treated (\\n)");
                    clientOut.println("\n");
                    System.out.println("\n");
                    nRequests++;
                }
                stopBrain();
            } catch (IOException e) {
                System.out.println("/!\\ Exception in run() /!\\");
                System.out.println(e.getMessage());
            }
            System.out.println(" * End of a thread");
        }


        /**
         * Process a request by searching the matching line in Main memory and them back to the client
         * 
         * @param request
         */
        public void searchLine(String request) {
            System.out.println(" * New request => searchLine()");

            /*
             * Reset Use Bits
             */
            if(resetBits) {
                cacheUseBit.replaceAll((key, value) -> 0);    // Replace all values of the HashMap by zero : Set all use bits to 0
                nRequests = 0;
                resetBits = false;
            }


            /*
             * Search in cache
             */
            System.out.println(" * Search in Cache");

            if(cache.containsKey(request)) {
                System.out.println(" -- It is in Cache");

                ArrayList<String> sendedSentences = cache.get(request);
                for(String sendedSentence : sendedSentences) {
                    System.out.println("\n");
                    System.out.println("   ===> Responding (from cache) \"" + sendedSentence + "\" ");
                    System.out.println("        To the request : " + request);
                    clientOut.println(sendedSentence);
                    cacheUseBit.put(request, 1);
                } 
            }
            else {
                System.out.println(" * It is NOT in Cache");

                /*
                 * Getting the types and the regex of the request
                 */

                try {
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
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher;
                    ArrayList<String> sendedSentences = new ArrayList<String>();    // List of the string already sended for ONE request (to avoid duplicates)
                    boolean matched = false;
                    for(int requestType : requestTypes) {
                        ArrayList<String> sentences = data.get(requestType);
                        for(int i = 0; i < sentences.size(); i++) {
                            String returnSentence = sentences.get(i);
                            matcher = pattern.matcher(returnSentence);
                            if( (matcher.find()) && (!sendedSentences.contains(returnSentence)) ) { // If we have a match and we do not have send it already (fot this request)
                                matched = true;
                                sendedSentences.add(returnSentence);
                                System.out.println("\n");
                                System.out.println("   ===> Responding (from main memory) \"" + returnSentence + "\" ");
                                System.out.println("        To the request : " + request);
                                clientOut.println(returnSentence);
                            }
                        }
                    }
                    if(!matched) {
                        System.out.println("\n");
                        System.out.println("   ===> No match found for the request : " + request);
                    }
                    

                    /*
                     * Put the request in cache
                     */
                    if(cacheSize == cacheMaxSize) {
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
                            System.out.println("/!\\ The cache did not find an entry with the use bit at 0 /!\\");
                        }
                        else {
                            cache.remove(removedKey);
                            cacheUseBit.remove(removedKey);
                            cacheSize--;
                        }
                    }

                    // Put the new request in cache
                    cache.put(request, sendedSentences);
                    cacheUseBit.put(request, 1);
                    cacheSize++;
                }
                catch(Exception e) {
                    System.out.println("/!\\ Wrong request syntax /!\\");
                }
            }
        }


        /**
         * Stop the brain
         */
        public void stopBrain() {
            System.out.println(" * Stopping the brain");
            try {
                clientOut.close();
                clientIn.close();
                clientSocket.close();
                decrementThreads();
            } catch(IOException e) {
                System.out.println("/!\\ IOException at Server.stop() /!\\");
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
        System.out.println(" -- Loading Main memory");

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
        System.out.println(" -- Stopping the server");
        active = false;
        try {
			serverSocket.close();
        } catch(IOException e) {
            System.out.println("/!\\ IOException at Server.stop() /!\\");
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
 * - Add a pool to keep track of each thread an a function joint
 * - Try catch bad regex
 *
 */