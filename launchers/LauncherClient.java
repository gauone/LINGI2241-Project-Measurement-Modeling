package launchers;

import clients.Client;

public class LauncherClient {
    public static void main(String[] args) {

        int lambda;
        int portNumber = 3900;
        int nClients = 50;
        Thread[] threads = new Thread[nClients];

        for (int i = 0; i < nClients; i++) {
            threads[i] = new Thread(() -> {
                try {
                    Client client = new Client("localhost", portNumber, true, lambda);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.exit(1);
                }
            });
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.exit(1);
            }
        }
    }
}