package clients;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import logger.MyLogger;

public class Client {
    protected Socket clientSocket;
    protected PrintWriter clientOut; // to send info to server
    protected BufferedReader clientIn; // to get info from the server

    protected ArrayList<Long> sendingTimes = new ArrayList<Long>();
    protected ArrayList<Long> arrivingTimes = new ArrayList<Long>();

    Random rand = new Random();

    /**
     * Constructor. It gets the get the static variables, creates the client object,
     * sends a random number of request to the serveur and listens on the socket.
     * 
     * @param hostName   a (Sting) containing the name of the server's hostname
     * @param portNumber a (int) containing the port number to reach the server
     * @param lambda     a (Double) specifing the rate
     */
    public Client(String hostName, int portNumber, Double lambda) {
        try {
            clientSocket = new Socket(hostName, portNumber);
            clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
            clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            int N = rand.nextInt(10) + 1;
            System.out.println(" - " + this + " going to launch " + String.valueOf(N) + " requests.");

            Thread sender = new Thread(() -> {
                // launchRequests(lambda, N);
                // Forcer l'envoi de ces requêtes A SUPPRIMER TODO TODO TODO TODO TODO TODO TODO TODO:
                try {
                    String[] requests = {"1;Transport", "0;Transport", "0,1,2,3;second", "2;second", "3,4;by", "0,1,2,3,4;by"};
                    for (String request : requests){
                        long time = (long) getRandomExponential(lambda);
                        TimeUnit.MILLISECONDS.sleep(time);
                        sendRequest(request);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            sender.start();
            
            int count = 1;
            String fromServer;
            while ( ((fromServer = clientIn.readLine()) != null) && count < 2*N ) {
                if (fromServer.equals("") && count%2 == 1) {
                    this.arrivingTimes.add(System.nanoTime());
                    count++;
                }
                else if (fromServer.equals("")) {
                    count++;
                } else {
                    System.out.println("- Server: " + fromServer + "\n"); // TODO recommenter ca
                }
            }
            
            System.out.println(this + " received "+ String.valueOf(count/2) + " responses.");

            sender.join();
            stopClient();
        } catch (UnknownHostException e) {
            System.out.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        } catch (InterruptedException e) {
			e.printStackTrace();
		}
    }

    public void launchRequests(double lambda, int N) {
        try {
            for (int i = 0; i < N; i++) {
                long time = (long)getRandomExponential(lambda);
                TimeUnit.MILLISECONDS.sleep(time);
                String request = generateRequest();
                sendRequest(request);
                //System.out.println(" - Client: send the request : " + request + "\n");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getRandomExponential(double lambda) {
        return 1000 * Math.log(1 - rand.nextDouble()) / (-lambda);
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

    public void stopClient() {
        // Compute the responses times
        ArrayList<Long> responsesTime = new ArrayList<Long>();
        Long start, stop;
        for (int i = 0; i < this.arrivingTimes.size(); i++) {
            start = this.sendingTimes.get(i);
            stop = this.arrivingTimes.get(i);
            responsesTime.add(stop - start);       
        }

        MyLogger.getInstance().println(responsesTime);

        try {
            clientOut.close();
            clientIn.close();
            clientSocket.close();
        } catch (IOException e) {
            System.out.println(" IOException closing the socket, PrintWriter and BufferedReader");
            System.out.println(e.getMessage());
        }
        System.out.println(" - Finised Thread in client");
    }
}