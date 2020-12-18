package regexPreprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class addWordsToRegexCorpus {
    public static void main(String[] args) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("dbdata.txt"));
            FileWriter fileWriter = new FileWriter(new File("regex.txt"), true);
            Random rand = new Random();

            String currentLine = bufferedReader.readLine();
            for(int i = 0; (currentLine = bufferedReader.readLine()) != null; i++) {
                if (i%16000 == 0){
                    String[] splittedLine = currentLine.split("@@@");
                    String sentence = splittedLine[1];
                    
                    int toWrite;
                    for (int j = 0; j < sentence.length(); j += toWrite) {
                        toWrite = rand.nextInt(sentence.length()-j)+1;
                        fileWriter.write(sentence.substring(j, j+toWrite)+'\n');
                    }
                }
            }
            bufferedReader.close();
            System.out.println("finished writing words to file");
            
            bufferedReader = new BufferedReader(new FileReader("regexPreprocessing/regexCorpus.txt"));
            currentLine = bufferedReader.readLine();
            while ((currentLine = bufferedReader.readLine()) != null) {
                fileWriter.write( currentLine + '\n' );
            }
            bufferedReader.close();
            fileWriter.close();
            System.out.println("finished writing regex to file");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
