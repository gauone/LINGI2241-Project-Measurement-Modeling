import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Brouillon {

	// Main Memory
	List<Integer> dataTypes = new ArrayList<Integer>();
	List<String> dataSentences = new ArrayList<String>();

	public static void main(String[] args) throws IOException {

		// Load the data
		BufferedReader bufferedReader;
		bufferedReader = new BufferedReader(new FileReader("dbdata.txt")); // Rajouter ../ !
		
		String currentLine = bufferedReader.readLine();
		for(int i = 0; currentLine != null; i++, currentLine = bufferedReader.readLine()) {
			String[] splittedLine = currentLine.split("@@@");

			// dataTypes[i] = splittedLine[0];
			// dataSentences[i] = splittedLine[1];

			System.out.println("Reading the " + i + "th line : ");
			System.out.println("   dataTypes = " + splittedLine[0]);
			System.out.println("   dataSentences = " + splittedLine[1]);

			if(i == 10) {
				break;
			}
		}

		bufferedReader.close();

	}
}