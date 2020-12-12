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
*/
public class ServerNul {

    // Port number
    int portNumber;

    // Main Memory
    List<Integer> dataTypes = new ArrayList<Integer>();
    List<String> dataSentences = new ArrayList<String>();

    // Queue
    List<String> requestList = new ArrayList<String>();

    /**
		Constructor
	*/
    public ServerNul(int portNumber) {
		this.portNumber = portNumber;
	}

    /**
        Start the simple server : 
         - Load the data
         - Wait far a connection
         - Add the request to the queue
    */
    public void start() throws IOException {

		// Main Memory
		List<Integer> dataTypes = new ArrayList<Integer>();
		List<String> dataSentences = new ArrayList<String>();

		// Load the data
		BufferedReader bufferedReader = new BufferedReader(new FileReader("../dbdata.txt"));
		
		String currentLine = bufferedReader.readLine();
		for(int i = 0; currentLine != null; i++, currentLine = bufferedReader.readLine()) {
			String[] splittedLine = currentLine.split("@@@");

			dataTypes.add(Integer.valueOf(splittedLine[0]));
			dataSentences.add(splittedLine[1]);

			if(i == 10) {
				break;
			}
		}

		bufferedReader.close();


        try ( 
            ServerSocket serverSocket = new ServerSocket(portNumber);
            Socket clientSocket = serverSocket.accept();
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
        
            String inputLine, outputLine;
            
            // Initiate conversation with client
            // outputLine = kkp.processInput(null);
            // out.println(outputLine);

            // while ((inputLine = in.readLine()) != null) {
            //     outputLine = kkp.processInput(inputLine);
            //     out.println(outputLine);
            //     if (outputLine.equals("Bye."))
            //         break;
            // }
            
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }

    }


    /**
		Stop the server and close the streams
    */
    
    /**
		Process a request
	*/
}