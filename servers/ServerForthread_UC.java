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
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
    Write a better server than serverNul

    This server return only the string of the line, like : 0@@@i thought that was neat => i thought that was neat
*/
public class ServerForthread_UC {

    // Threads
    private int nThread;
    private List<Thread> pool;

    // Queue
    private int queueMaxSize = 1000;
	private ArrayList<String> queue = new ArrayList<String>();

    // Port number
    int portNumber;

    // Socket
    ServerSocket serverSocket;
    Socket clientSocket;
    PrintWriter clientOut;
    BufferedReader clientIn;

    // Main Memory
    HashMap<Integer, ArrayList<String>> data = new HashMap<Integer, ArrayList<String>>();   // Hashmap with key : type (Integer); value : sentences (list of String)

    // Cache
    HashMap<String, ArrayList<String>> cache = new HashMap<String, ArrayList<String>>();    // Hashmap with key : request (String); value : sendedSentences (list of String)
    HashMap<String, Integer> cacheUseBit = new HashMap<String, Integer>();                  // Hashmap with key : request (String); value : usedBit (Integer)
    int cacheMaxSize = 10;                                                                  // We keep maximum the last 100 requests with their responses
    int cacheSize = 0;                                                                      // Amount of request in the cache
    int nReset = 3;                                                                         // Reset of the use bits each nReset instructions
    int nRequests = 0;                                                                      // Amount of instructions from the last use bits reset
    boolean resetBits = false;                                                              // Set to true when we have to reset the use bits



    /**
     * Constructor
     * 
     * @param portNumber : port number
     * @param nThread : amount of thread allowed for the server
     */
    public ServerForthread(int portNumber, int nThread) {
        this.portNumber = portNumber;
        this.nThread = nThread;
        this.pool = new ArrayList<Thread>(nThread);
    }



    /**
     * Creation of all the threads used by the server
     */ 
    public void initiatePool(){
        for(int i=0;i<nThread;i++){
            Thread t = new Thread(runThread);
            pool.add(t);
        }
    }



    /**
     * Starting all the threads used by the server
     */
    public void startThreads(){
        for(Thread t : pool){
            t.start();
        }
    }



    /**
     * Wait for all the threads to finish
     * 
     * @throws InterruptedException
     */
    public void joinThreads() throws InterruptedException{
        for(Thread t : pool){
            t.join();
        }
    }



    /** 
     * Runnabe used to define the behavior of the threads
     * 
     * /!\ UNDER CONSTRUCTION /!\
     * 
     */
    Runnable runThread = new Runnable(){
        public void run() {
            System.out.println(" - Starting runThread (Runnable)");

            if(queue.size() != 0) {
                String request = queue.remove(0);

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
        }
    };

    
    
    /**
     * Start the server : 
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

        
        String request;
        initiatePool();
        startThreads();

        while((request = clientIn.readLine()) != null) {    // Read a request (that have the following format : "1,2,3;coucou")
            System.out.println("");
            System.out.println(" - Reading a new request");

            if(nRequests == nReset) {                       // We have to reset the use bits !
                resetBits = true;
            }

            if(queue.size() == queueMaxSize) {              // The queue is full
				System.out.println(" /!\\ The queue is full... Reject : " + request);
            }
            else {                                          // Add the request to the queue
                queue.add(request);
                nRequests++;
            }
        }

        System.out.println(" - End of the reception : joining all threads");
		try{
            joinThreads();
        }
        catch(InterruptedException e) {
            System.out.println("IOException at the end of start()");
            System.out.println(e.getMessage());
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
    public void searchLine(String request) {

        System.out.println(" - Starting searchLine()");

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
     * Stop the server and close the streams
     */
    public void stop() {
        System.out.println(" - Stopping the server");
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