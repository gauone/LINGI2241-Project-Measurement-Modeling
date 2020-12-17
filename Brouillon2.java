import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Brouillon2 {

	public static void main(String[] args) throws IOException {

		HashMap<String, ArrayList<String>> data = new HashMap<String, ArrayList<String>>(); // Hashmap with key : word (String); value : sentences (list of String)
        
		BufferedReader bufferedReader = new BufferedReader(new FileReader("dbdataMini.txt"));
		
		String currentLine;
        while((currentLine = bufferedReader.readLine()) != null) {

			String[] splittedLine = currentLine.split("@@@");
			splittedLine = splittedLine[1].split(" ");

			for(int i = 0; i < splittedLine.length; i++) {

				String word = splittedLine[i];
				ArrayList<String> sentences = new ArrayList<String>();

				if(!data.containsKey(word)) {			// If the word is not in memory : put it with the (first) sentence where it is found
					sentences.add(currentLine.toLowerCase());
					data.put(word, sentences);
				}
				else {
					sentences = data.remove(word);		// If the word is already in memory : get the associated sentences, add it the new one and actualise the memory
					sentences.add(currentLine.toLowerCase());
					data.put(word, sentences);
				}
			}
		}

		bufferedReader.close();

		String request = "1,2,3;second"; 	// 2 occurence of type 1 and 2

        /*
         * Getting the types and the regex of the request
         */
        System.out.println("Getting types and regex from the request");
        List<Integer> requestTypes = new ArrayList<Integer>();      // List of Integer containing the tags asked by the request
        String regex;                                               // String containing the regex asked by the request

        String[] splittedLine = request.split(";");                 // Split to have the tags (String) and the regex

        if(splittedLine[0].equals("")) {                            // If the request do not contain a type, we are looking for each of them
            splittedLine[0] = "0,1,2,3,4,5";
        }

        String[] stringTypes = splittedLine[0].split(",");
        for(int i = 0; i < stringTypes.length; i++) {
                requestTypes.add(Integer.valueOf(stringTypes[i]));
        }

		regex = splittedLine[1].toLowerCase(); 
		
		/*
         * testing the memory storage
         */

		ArrayList<ArrayList<String>> regexCandidate = new ArrayList<ArrayList<String>>();
		String[] splittedRegex = request.split(" ");
		
		/*
		 * Get the sentences associated to each uniRegex
		 */
		for(int i = 0; i < splittedRegex.length; i++) {											// For each uniRegex 

			ArrayList<String> uniRegexCandidate = new ArrayList<String>();
			String uniRegex = splittedRegex[i];
			ArrayList<String> uniRegexSentences = data.get(uniRegex);

			for(int j = 0; j < uniRegexSentences.size(); j++) {									// For each sentences associated to the uniRegex

				String[] splittedUniRegexSentence = uniRegexSentences.get(i).split("@@@");
				String sentence = splittedUniRegexSentence[1];
				Integer type = Integer.valueOf(splittedUniRegexSentence[0]);

				if( requestTypes.contains(type) && !uniRegexCandidate.contains(sentence) ) {	// If the sentence have a requested type and is not already a candidate
					uniRegexCandidate.add(sentence);
				}
			}

			regexCandidate.add(uniRegexCandidate);

		}

		/*
		 * Compare the uniRegexCandidate to get the intersection
		 */
		


		System.out.println("End of the main()");

	}
}