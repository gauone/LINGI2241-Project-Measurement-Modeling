package launchers;

import clients.Client;
import logger.MyLogger;

public class LauncherClient {
    public static void main(String[] args) {

        Double lambda = 5.0;
        int portNumber = 3900;
        int nClients = 5;
        Thread[] threads = new Thread[nClients];

        long start = System.currentTimeMillis();
        for (int i = 0; i < nClients; i++) {
            threads[i] = new Thread(() -> {
                try {
                    new Client("localhost", portNumber, lambda);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.exit(1);
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.exit(1);
            }
        }
        long stop = System.currentTimeMillis();
        MyLogger.getInstance().closeFile();
        System.out.println("Total time taken by the clients: " + String.valueOf((double) (stop-start)/1000) + " seconds");
    }
}