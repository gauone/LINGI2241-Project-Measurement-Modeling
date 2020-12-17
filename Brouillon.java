import java.io.IOException;
import java.util.*;

public class Brouillon {

	public static void main(String[] args) throws IOException {

        List<String> columnsOld = Arrays.asList("red", "blue", "blue", "green", "red");
        List<String> columnsNew = Arrays.asList("red", "green", "green", "yellow");

        columnsOld.retainAll(columnsNew);

        System.out.println("End");
	}
}