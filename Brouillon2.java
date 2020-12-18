import java.io.IOException;
import java.util.*;

public class Brouillon2 {

    public static void main(String[] args) {  
        String str = "India is my country";
        System.out.println(str.length()); 
        System.out.println(charRemoveAt(str, 18));  
    }

    public static String charRemoveAt(String str, int p) {  
       return str.substring(0, p) + str.substring(p + 1);  
    }  
}