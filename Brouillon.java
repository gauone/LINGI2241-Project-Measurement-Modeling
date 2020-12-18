import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Brouillon {

	public static void main(String[] args) throws IOException {

        String request = generateRequest();

        System.out.println(request);

        System.out.println("End");
    }

    public static String generateRequest() throws IOException {

        int nTypes = getRandomNumberInRange(0, 6); // Amount of types in the request. Can be 0,1,2,3,4,5 => 6
                                                   // possibilities
        ArrayList<Integer> intTypes = new ArrayList<Integer>();
        String stringRequest = "";

        for (int i = 0; i < nTypes; i++) { // Generate random types in [0, 1, 2, 3, 4, 5]
            int type = getRandomNumberInRange(0, 5);
            if (!intTypes.contains(type)) {
                intTypes.add(type);
                String typeString = String.valueOf(type);
                typeString += ',';
                stringRequest += typeString;
            }
        }

        if (nTypes == 0) { // Handle the case when we have no type specified
            stringRequest = ",";
        }

        stringRequest = charRemoveAt(stringRequest, stringRequest.length() - 1);
        stringRequest += ';';

        int entry = getRandomNumberInRange(1, 960); // Because regex.txt have 960 lines !
        int i = 1;

        BufferedReader bufferedReader = new BufferedReader(new FileReader("regex.txt"));
        String currentLine;
        while (((currentLine = bufferedReader.readLine()) != null) && (i <= entry)) {
            if (i == entry) {
                stringRequest += currentLine;
            }
            i++;
        }

        bufferedReader.close();

        return stringRequest;
    }

    private static int getRandomNumberInRange(int min, int max) {
        if(min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    private static String charRemoveAt(String str, int p) {  
        return str.substring(0, p) + str.substring(p + 1);  
    }  

}