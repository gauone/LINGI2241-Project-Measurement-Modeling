package clients;

import java.util.List;
import java.util.stream.Collectors;

public class ClientTest {
    public static void testRequest(List<Integer> types, String regex) {
        String typesString = types.stream().map(n -> String.valueOf(n)).collect(Collectors.joining(","));

        System.out.println(typesString + ";" + regex);
        
    }
    public static void main(String[] args){
        List l = List.of("1", "2", "3");
        testRequest(l, "salut");
    }

}