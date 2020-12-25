package launchers;

import clients.Client;
import clients.ClientSkip;
import logger.MyLogger;

public class LauncherClient {
    public static void main(String[] args) {

        Double useLambda = 10.0;                // Maximum 10.0 otherwise the ditribution squeeze...
        Double distLambda = useLambda/1000 ;    // Distlambda = 0.001 => mean waiting time = 1000 [ms] <==> 1 [req/s]
        int portNumber = 3900;
        int nClients = 50;
        Thread[] threads = new Thread[nClients];

        long start = System.currentTimeMillis();
        for (int i = 0; i < nClients; i++) {
            threads[i] = new Thread(() -> {
                try {
                    new Client("2a02:2788:f4:59f:163:eb8f:b089:d411", portNumber, distLambda);
                    // new ClientSkip("localhost", portNumber);
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