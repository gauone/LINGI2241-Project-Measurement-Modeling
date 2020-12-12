import java.io.IOException;

public class Launcher {

    public static void main(String[] args) throws IOException {
        
        if (args.length != 1) {
            System.err.println("Usage: java ServerNul <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);

        

    }
}