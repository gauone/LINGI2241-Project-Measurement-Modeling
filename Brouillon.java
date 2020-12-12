import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Brouillon {

	public static void main(String[] args) throws IOException {

		// Main Memory
		List<Integer> dataTypes = new ArrayList<Integer>();
		List<String> dataSentences = new ArrayList<String>();

		// Load the data
		BufferedReader bufferedReader;
		bufferedReader = new BufferedReader(new FileReader("dbdata.txt")); // Rajouter ../ !
		
		String currentLine = bufferedReader.readLine();
		for(int i = 0; currentLine != null; i++, currentLine = bufferedReader.readLine()) {
			String[] splittedLine = currentLine.split("@@@");

			dataTypes.add(Integer.valueOf(splittedLine[0]));
			dataSentences.add(splittedLine[1]);

			if(i == 10) {
				break;
			}
		}

		bufferedReader.close();

	}
}