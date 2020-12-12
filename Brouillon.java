import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Brouillon {

	public static void main(String[] args) throws IOException {

		String request = ";coucou";
		boolean requestAllTypes = false;

		String[] splittedLine = request.split(";");

		if(splittedLine[0].equals("")) {
			requestAllTypes = true;
		}
		else {
			String[] stringTypes = splittedLine[0].split(",");
			List<Integer> requestTypes = new ArrayList<Integer>();
			for(int i = 0; i < stringTypes.length; i++) {
					requestTypes.add(Integer.valueOf(stringTypes[i]));
			}
		}

		String regex = splittedLine[1];
	}
}