package regexPreprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class regexComplexity {

    public static void main(String[] args) {
        try {
            BufferedWriter regexEasy = new BufferedWriter(new FileWriter("regexFolder/regexEasy.txt"));
            BufferedWriter regexMedium = new BufferedWriter(new FileWriter("regexFolder/regexMedium.txt"));
            BufferedWriter regexHard = new BufferedWriter(new FileWriter("regexFolder/regexHard.txt"));
            BufferedWriter regexNoMatch = new BufferedWriter(new FileWriter("regexFolder/regexNoMatch.txt"));

            BufferedReader brRegex = new BufferedReader(new FileReader("regex.txt"));
            BufferedReader brDB;

            boolean found = false;
            String currentRegex;
            String currentDBLine;
            Pattern pattern;
            Matcher matcher;
            while((currentRegex = brRegex.readLine()) != null) {
                pattern = Pattern.compile(currentRegex);
                brDB = new BufferedReader(new FileReader("dbdata.txt"));
                while((currentDBLine = brDB.readLine()) != null) {
                    matcher = pattern.matcher(currentDBLine);
                    if(matcher.find() && !found) {

                        found = true;

                        long initTime = System.nanoTime();
                        matcher = pattern.matcher(currentDBLine);
                        long endTime = System.nanoTime();
                        long time = endTime - initTime;
                        
                        if(time < 500) {
                            // Write in regexEasy
                            regexEasy.write(currentRegex);
                        }
                        else if(time < 1000) {
                            // Write in regexMedium
                            regexMedium.write(currentRegex);
                        }
                        else {
                            // Write in regexHard
                            regexHard.write(currentRegex);
                        }
                    }
                }
                if (!found){
                    // Write in regexNoMatch
                    regexNoMatch.write(currentRegex);
                }
                found = false;
                brDB.close();
            }
            //close the reader
            brRegex.close();

            //close the writers
            regexEasy.close();
            regexMedium.close();
            regexHard.close();
            regexNoMatch.close();
        }
        catch(IOException e) {
            System.out.println("/!\\ IOException in regexComplexity /!\\");
        }

        System.out.println("The End...");

    }
}
