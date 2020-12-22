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
import java.util.regex.PatternSyntaxException;

/**
    Write a better server than serverNulthread
*/
public class ServerForthreadNewCache {

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
    HashMap<Integer, ArrayList<String>> data = new HashMap<Integer, ArrayList<String>>(); // Hashmap with key : type (Integer);
                                                                                          // value : sentences (list of String)

    // CacheHashmap with key : request; Value : (Hashmap with key : request and value sendedSentences)
    HashMap<String, HashMap<Integer, ArrayList<String>>> cache = new HashMap<String, HashMap<Integer, ArrayList<String>>>();

    HashMap<String, Integer> cacheUseBit = new HashMap<String, Integer>(); // Hashmap with key : request;
                                                                           // value : usedBit
    int cacheSize = 0; // Amount of request in the cache
    int nRequests = 0; // Amount of instructions from the last use bits reset
    boolean resetBits = false; // Set to true when we have to reset the use bits
    final int cacheMaxSize = 10; // We keep maximum the last 100 requests with their responses
    final int nReset = 3; // Reset of the use bits each nReset instructions

    final String dbFilename;

    /**
     * Constructor
     * 
     * @param portNumber
     */
    public ServerForthreadNewCache(int portNumber, int nMaxThreads, String dbFilename) {
        this.portNumber = portNumber;
        this.nMaxThreads = nMaxThreads;
        this.dbFilename = dbFilename;
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
        System.out.println(" -- Starting the server Fort with the new cache --");
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
            System.out.println(e.getMessage());
        }

        while(getActive()) { // Keep nMaxThreads running
            if(getnThreads() < nMaxThreads) {
                Socket clientSocket = serverSocket.accept(); // Accept a client
                Runnable brain = new Brain(clientSocket, data);
                new Thread(brain).start();
                incrementThreads();
                System.out.println("Accepting a new client, nThreads : " + getnThreads());
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

        public Brain(Socket clientSocket, HashMap<Integer, ArrayList<String>> data) {
            this.clientSocket = clientSocket;
            this.data = data;
        }


        @Override
        public void run() {
            // System.out.println(" * Beginning of a thread");
            try {
                clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
                clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                /*
                 * Read the socket
                 */
                String request;
                while((request = clientIn.readLine()) != null) {    // Read a request (that have the following format : "1,2,3;coucou")
                    if(getnRequests() == nReset) {                       // We have to reset the use bits !
                        setResetBits(true);
                    }
                    searchLine(request);
                    // System.out.println("\n");
                    // System.out.println(" * Request treated (\\n)");
                    clientOut.println("\n");
                    // System.out.println("\n");
                    IncrementnRequests();
                }
                stopBrain();
            } catch (IOException e) {
                System.out.println("/!\\ Exception in run() /!\\");
                System.out.println(e.getMessage());
            }
            // System.out.println(" * End of a thread");
        }


        /**
         * Process a request by searching the matching line in Main memory and them back to the client
         * 
         * @param request
         */
        public void searchLine(String request) {
            // System.out.println(" * New request => searchLine()");

            /*
             * Reset Use Bits
             */
            if(getResetBits()) {
                resetCacheUseBit();    // Replace all values of the HashMap by zero : Set all use bits to 0
                setnRequests(0);
                setResetBits(false);
            }

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

            regex = splittedLine[1]; 

            List<Integer> remaining_types = new ArrayList<Integer>();
            HashMap<Integer,ArrayList<String>> cache_entry;
            /*
             * Search in cache
             */
            if(containsCache(regex)) {
                // System.out.println(" -- It is in Cache");
                cache_entry = getCache(request);                            // take the cache entry if it exits
                for(int requestType : requestTypes) {                       // take all types that you are looking for from the cache
                    if (cache_entry.containsKey(requestType)) {
                        ArrayList<String> sendedSentences = cache_entry.get(requestType);
                        for(String sendedSentence : sendedSentences) {
                            clientOut.println(sendedSentence);
                            setCacheUseBit(regex);
                        }
                    } else {
                        remaining_types.add(requestType);                   // if the some type we are looking for aren't in cache
                    }
                } 
            } else {                                                        // create a new cache entry if it doesn't exist
                cache_entry = new HashMap<Integer, ArrayList<String>>();
            }
            if (remaining_types.size() > 0) {                               // if nothing intressting was in the cache or if the cache doesn't contain all wanted types
                try {
                    /*
                     * Search of the tags & regex into the Main memory
                     *    requestTypes = [1, 2, 3]
                     *    regex = "second"
                     */
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher;
                    ArrayList<String> sendedSentences = new ArrayList<String>();    // List of the string already sended for ONE request (to avoid duplicates)
                    boolean matched = false;
                    for(int requestType : remaining_types) {
                        ArrayList<String> this_type_sentence = new ArrayList<String>();
                        ArrayList<String> sentences = data.get(requestType);
                        for(int i = 0; i < sentences.size(); i++) {
                            String returnSentence = sentences.get(i);
                            matcher = pattern.matcher(returnSentence);
                            if( (matcher.find()) && (!sendedSentences.contains(returnSentence)) ) { // If we have a match and we do not have send it already (for this request)
                                matched = true;
                                sendedSentences.add(returnSentence);
                                this_type_sentence.add(returnSentence);
                                // System.out.println("\n");
                                // System.out.println("   ===> Responding (from main memory) \"" + returnSentence + "\" ");
                                // System.out.println("        To the request : " + request);
                                clientOut.println(returnSentence);
                            }
                        }
                        cache_entry.put(requestType, this_type_sentence);
                    }
                    if(!matched) {
                        System.out.println("No match found for the request : " + request);
                    }
                    

                    /*
                     * Put the request in cache
                     */
                    if(getCacheSize() == cacheMaxSize) {
                        String removedKey = "null";
                        boolean search = true;
                        Set<String> keySet = getUseBitSet();
                        for (Iterator<String> it = keySet.iterator(); it.hasNext() && search;) {    // Looking for a (the first) key with use bit == 0
                                String key = it.next();
                                if(getCacheUseBit(key) == 0) {
                                        removedKey = key;
                                        search = false;
                                }
                        }
            
                        // Remove the key of the cache (do not forget the use bit)
                        if(removedKey.equals("null")) {
                            System.out.println("/!\\ The cache did not find an entry with the use bit at 0 /!\\");
                        }
                        else {
                            removeCache(removedKey);
                            removeCacheUseBit(removedKey);
                            decrementCacheSize();
                        }
                    }

                    // Put the new request in cache
                    putCache(regex, cache_entry);
                    setCacheUseBit(regex);
                    incrementCacheSize();;
                }
                catch(PatternSyntaxException e) {
                    System.out.println("/!\\ Wrong request syntax /!\\ ==> request : " + request);

                }
            }
        }


        /**
         * Stop the brain
         */
        public void stopBrain() {
            // System.out.println(" * Stopping the brain");
            try {
                clientOut.close();
                clientIn.close();
                clientSocket.close();
                decrementThreads();
                System.out.println("Exiting a client, nThreads = " + getnThreads());
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
        // System.out.println(" -- Loading Main memory");

        ArrayList<String> sentences0 = new ArrayList<String>();   // List of Strings containing the Strings per line : 2.442.237
        ArrayList<String> sentences1 = new ArrayList<String>();   // List of Strings containing the Strings per line : 2.442.237
        ArrayList<String> sentences2 = new ArrayList<String>();   // List of Strings containing the Strings per line : 2.442.237
        ArrayList<String> sentences3 = new ArrayList<String>();   // List of Strings containing the Strings per line : 2.442.237
        ArrayList<String> sentences4 = new ArrayList<String>();   // List of Strings containing the Strings per line : 2.442.237
        ArrayList<String> sentences5 = new ArrayList<String>();   // List of Strings containing the Strings per line : 2.442.237
        
		BufferedReader bufferedReader = new BufferedReader(new FileReader(this.dbFilename));
		
		String currentLine;
        while ((currentLine = bufferedReader.readLine()) != null) {

            String[] splittedLine = currentLine.split("@@@");

			int dataType = Integer.valueOf(splittedLine[0]);
			String dataSentence = splittedLine[1];
            
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
     * Stop the server and close the streams
     */
    public void stop() {
        System.out.println(" -- Stopping the server --");
        setActive(false);
        try {
			serverSocket.close();
        } catch(IOException e) {
            System.out.println("/!\\ IOException at Server.stop() /!\\");
            System.out.println(e.getMessage());
        }
    }



    /**
     * Decrement the amount of threads running
     */
    public synchronized void decrementThreads() {
        this.nThreads--;
    }



    /**
     * Increment the amount of threads running
     */
    public synchronized void incrementThreads() {
        this.nThreads++;
    }



    /**
     * Increment the amount of threads running
     */
    public synchronized int getnThreads() {
        return this.nThreads;
    }


    /**
     * Get the boolean active synchronously
     */
    public synchronized boolean getActive() {
        return this.active;
    }



    /**
     * Set the boolean active synchronously
     */
    public synchronized void setActive(boolean bool) {
        this.active = bool;
    }



    /**
     * Check if the cache contains the request synchronously
     */
    public synchronized boolean containsCache(String request) {
        return this.cache.containsKey(request);
    }



    /**
     * Get a value of the cache synchronously
     */
    public synchronized HashMap<Integer, ArrayList<String>> getCache(String request) {
        return this.cache.get(request);
    }
    


    /**
     * Remove the entry associated to removedkey key in the cache synchronously
     */
    public synchronized void removeCache(String removedKey) {
        this.cache.remove(removedKey);;
    }
    


    /**
     * Add an entry in the cache synchronously
     */
    public synchronized void putCache(String regex, HashMap<Integer, ArrayList<String>> cache_entry) {
        this.cache.put(regex, cache_entry);
    }



    /**
     * Return a Set of the key in cacheUseBit synchronously
     */
    public synchronized Set<String> getUseBitSet() {
        return this.cacheUseBit.keySet();
    }



    /**
     * Get the useBit associated to request synchronously
     */
    public synchronized int getCacheUseBit(String request) {
        return this.cacheUseBit.get(request);
    }



    /**
     * Remove the entry associated to request in cacheUseBit synchronously
     */
    public synchronized void removeCacheUseBit(String request) {
        this.cacheUseBit.remove(request);
    }
    
    

    /**
     * Set the useBit associated to request to 1 synchronously
     */
    public synchronized void setCacheUseBit(String request) {
        this.cacheUseBit.put(request, 1);
    }



    /**
     * Reset all values of cacheUseBit to 0 synchronously
     */
    public synchronized void resetCacheUseBit() {
        this.cacheUseBit.replaceAll((key, value) -> 0);
    }

    

    /**
     * Return the value of cacheSize synchronously
     */
    public synchronized int getCacheSize() {
        return this.cacheSize;
    }

    
    /**
     * Increment cacheSize synchronously
     */
    public synchronized void incrementCacheSize() {
        this.cacheSize++;
    }



    /**
     * Decrement cacheSize synchronously
     */
    public synchronized void decrementCacheSize() {
        this.cacheSize--;
    }



    /**
     * Return the value of nRequest synchronously
     */
    public synchronized int getnRequests() {
        return this.nRequests;
    }



    /**
     * Set the value of nRequest synchronously
     */
    public synchronized void setnRequests(int nRequests) {
        this.nRequests = nRequests;
    }



    /**
     * Increment the value of nRequest synchronously
     */
    public synchronized void IncrementnRequests() {
        this.nRequests ++;
    }



    /**
     * Return the value of nRequest synchronously
     */
    public synchronized boolean getResetBits() {
        return this.resetBits;
    }



    /**
     * Set the value of resetBits synchronously
     */
    public synchronized void setResetBits(boolean resetBits) {
        this.resetBits = resetBits;
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