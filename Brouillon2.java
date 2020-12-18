import java.io.IOException;
import java.util.*;

public class Brouillon2 {
	public static void main(String[] args) throws IOException {

        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add("coucou");
        arrayList.add("aurevoir");
        arrayList.remove(0);
        String testGet = arrayList.remove(0);
        String testNull = arrayList.remove(0);

        System.out.println(arrayList);
        System.out.println(testGet);
        System.out.println(testNull);

        System.out.println("End");
	}
}