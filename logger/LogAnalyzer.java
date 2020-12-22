package logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * This class bring together differents method to analyse the measures of the
 * logger
 */
public class LogAnalyzer {

    Long totalTime;
    Long minTime;
    Long maxTime;
    Long meanTime;
    int nTimes;

    public static void main(String[] args) {
        LogAnalyzer la = new LogAnalyzer();
        la.computeMinMaxMean();
        System.out.println("totalTime = " + la.totalTime + ", minTime = " + la.minTime + ", maxTime = " + la.maxTime + ", meanTime = " + la.meanTime + ", nTimes = " + la.nTimes);
    }

    public LogAnalyzer() {
        this.totalTime = (long) 0;
        this.minTime = Long.MAX_VALUE;
        this.maxTime = (long) 0;
        this.meanTime = (long) 0;
        this.nTimes = 0;
    }

    public void computeMinMaxMean() {

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("myLog.log"));
            String currentLine;
            while((currentLine = bufferedReader.readLine()) != null) {
                Long currentTime = Long.valueOf(currentLine);

                // Get the cumulate time
                totalTime += currentTime;

                // Get the minimum time
                if(currentTime < minTime) {
                    minTime = currentTime;
                }

                // Get the maximum time
                if(currentTime > maxTime) {
                    maxTime = currentTime;
                }

                nTimes++;
            }
            bufferedReader.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        meanTime = totalTime/nTimes;
    }
}