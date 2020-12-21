import java.util.*;

public class Brouillon {
    public static void main(String[] args) {
		System.out.println("start");

		for (int i=0; i<1000; i++){
            double a = getRandomExponential(1.0);
            if(a > 1) {
                System.out.println(a);
            }
        }
        
		System.out.println("stop");
	}

	public static double getRandomExponential(double lambda) {
		Random rand = new Random();
		return Math.log(1 - rand.nextDouble()) / (-lambda);
	}  

}