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

			String[] splittedLine = currentLine.split(" ");

			for(int i = 0; i < splittedLine.length; i++) {

				String word = splittedLine[i];
				ArrayList<String> sentences = new ArrayList<String>();

				if(!data.containsKey(word)) {			// If the word is not in memory : put it with the (first) sentence where it is found
					sentences.add(currentLine);
					data.put(word, sentences);
				}
				else {
					sentences = data.remove(word);		// If the word is already in memory : get the associated sentences, add it the new one and actualise the memory
					sentences.add(currentLine);
					data.put(word, sentences);
				}
			}
		}

			


		bufferedReader.close();
		
	}
}