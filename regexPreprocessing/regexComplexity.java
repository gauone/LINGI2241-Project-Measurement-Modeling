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
        MatchComplexity();
        // CPUComplexity();
    }


    public static void MatchComplexity() {
        try {
            BufferedWriter regexEasyMatch = new BufferedWriter(new FileWriter("regexFolder/regexEasyMatch.txt"));
            BufferedWriter regexMediumMatch = new BufferedWriter(new FileWriter("regexFolder/regexMediumMatch.txt"));
            BufferedWriter regexHardMatch = new BufferedWriter(new FileWriter("regexFolder/regexHardMatch.txt"));

            BufferedReader brRegex = new BufferedReader(new FileReader("regexFolder/regex.txt"));
            BufferedReader brDB;

            int matchs = 0;
            String currentRegex;
            String currentDBLine;
            Pattern pattern;
            Matcher matcher;
            while((currentRegex = brRegex.readLine()) != null) {

                pattern = Pattern.compile(currentRegex);
                brDB = new BufferedReader(new FileReader("dbdata.txt"));

                while((currentDBLine = brDB.readLine()) != null) {
                    matcher = pattern.matcher(currentDBLine);
                    if(matcher.find()) {
                        matchs++;
                    }
                }

                System.out.println(matchs + " : " + currentRegex);

                if(matchs < 2) {
                    // Write in regexEasyMatch
                    regexEasyMatch.write(currentRegex+"\n");
                }
                else if(matchs < 20) {
                    // Write in regexMedium
                    regexMediumMatch.write(currentRegex+"\n");
                }
                else {
                    // Write in regexHard
                    regexHardMatch.write(currentRegex+"\n");
                }

                matchs = 0;
                brDB.close();
            }
            //close the reader
            brRegex.close();

            //close the writers
            regexEasyMatch.close();
            regexMediumMatch.close();
            regexHardMatch.close();
        }
        catch(IOException e) {
            System.out.println("/!\\ IOException in regexComplexity /!\\");
        }

        System.out.println("The End...");
    }


    public static void CPUComplexity() {
        try {
            BufferedWriter regexEasy = new BufferedWriter(new FileWriter("regexFolder/regexEasy.txt"));
            BufferedWriter regexMedium = new BufferedWriter(new FileWriter("regexFolder/regexMedium.txt"));
            BufferedWriter regexHard = new BufferedWriter(new FileWriter("regexFolder/regexHard.txt"));
            BufferedWriter regexNoMatch = new BufferedWriter(new FileWriter("regexFolder/regexNoMatch.txt"));

            BufferedReader brRegex = new BufferedReader(new FileReader("regexFolder/regex.txt"));
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
                        
                        if(time < 900) {
                            // Write in regexEasy
                            regexEasy.write(currentRegex+"\n");
                        }
                        else if(time < 1600) {
                            // Write in regexMedium
                            regexMedium.write(currentRegex+"\n");
                        }
                        else {
                            // Write in regexHard
                            regexHard.write(currentRegex+"\n");
                        }
                    }
                }
                if (!found){
                    // Write in regexNoMatch
                    regexNoMatch.write(currentRegex+"\n");
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
