package regexPreprocessing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class regexComplexity {
    
    public static void main(String[] args) {
        ArrayList<String> hard = new ArrayList<String>();
        ArrayList<String> medium = new ArrayList<String>();
        ArrayList<String> easy = new ArrayList<String>();

        try {
            BufferedReader brRegex = new BufferedReader(new FileReader("regex.txt"));
            BufferedReader brDB;

            boolean found = false;
            String currentRegex;
            String currentDBLine;
            Pattern pattern;
            Matcher matcher;
            while ((currentRegex = brRegex.readLine()) != null) {
                pattern = Pattern.compile(currentRegex);
                brDB = new BufferedReader(new FileReader("dbdata.txt"));
                while ((currentDBLine = brDB.readLine()) != null) {
                    matcher = pattern.matcher(currentDBLine);
                    if(matcher.find() && !found) {

                        found = true;

                        long initTime = System.nanoTime();
                        matcher = pattern.matcher(currentDBLine);
                        long endTime = System.nanoTime();
                        long time = endTime - initTime;
                        
                        if(time < 500) {
                            // Write in regexEasy
                        }
                        else if(time < 1000) {
                            // Write in regexMedium
                        }
                        else {
                            // Write in regexHard
                        }
                    }
                    // Write in regexNoMatch
                }
                found = false;
                brDB.close();
            }
            brRegex.close();
        }
        catch(IOException e) {
            System.out.println("/!\\ IOException in regexComplexity /!\\");
        }

        System.out.println("The End...");

    }
}
