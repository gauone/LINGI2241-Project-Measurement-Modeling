package logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class uses the singleton patern to have one syncronized logger.
 * As suggested on
 * https://stackoverflow.com/questions/10027020/write-to-text-file-from-multiple-threads
 * and in the book Algorithhms by Robert Sedgewick and Kevin Wayne.
 * The file "myLog.log" is overitten when the Logger is created, or it creates a new file.
 */
public class MyLogger {
    private static final MyLogger logger = new MyLogger();
    private BufferedWriter bf;

    private MyLogger() {
        try {
            this.bf = new BufferedWriter(new FileWriter("myLog.log"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MyLogger getInstance() {
        return logger;
    }

    public synchronized void println(String str) {
        try {
            bf.write( str+"\n" );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void println(ArrayList<Long> arr) {
        try {
            for (Long chiffre : arr) {
                bf.write( chiffre.toString() + "\n" );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void closeFile() {
        try {
            bf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
