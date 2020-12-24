package clients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import logger.MyLogger;
import regexPreprocessing.RequestGenerator;

public class Client {
    Boolean auto_query_generation = true;
    
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

            int N;
            if(auto_query_generation){
                N = rand.nextInt(5) + 1;
            } else {
                N = 5;  // choose the number of request by client
            }
            System.out.println(" - " + this + " going to launch " + String.valueOf(N) + " requests.");

            Thread sender = new Thread(() -> {
                if (auto_query_generation) {
                    launchRequests(lambda, N);
                } else {
                    try {
                        String[] requests = {";Transports", "0;Transports", "1;Transports", "2;Transports", "3;Transports"};
                        for (String request : requests){
                            long time = (long) getRandomExponential(lambda);
                            TimeUnit.MILLISECONDS.sleep(time);
                            sendRequest(request);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            sender.start();
            
            int count = 1;
            String fromServer;
            while ( ((fromServer = clientIn.readLine()) != null) && count < 2*N ) {
                if (fromServer.equals("") && count%2 == 1) {
                    this.arrivingTimes.add(System.nanoTime());
                    count++;
                    System.out.println("- Request treated");
                }
                else if (fromServer.equals("")) {
                    count++;
                } else {
                    // System.out.println("- Server: " + fromServer + "\n");
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

                String request = RequestGenerator.getInstance().generateRequest();
                sendRequest(request);
                System.out.println(" - Client (" + this +") send the request : " + request + "\n");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //  using the inverse method to go from a uniform distribution to an exponential
    public double getRandomExponential(double lambda) {
        return 1000 * Math.log(1 - rand.nextDouble()) / (-lambda);
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