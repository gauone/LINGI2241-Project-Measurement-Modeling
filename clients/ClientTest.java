package clients;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClientTest {
    public static void testRequest(List<Integer> types, String regex) {
        String typesString = types.stream().map(n -> String.valueOf(n)).collect(Collectors.joining(","));

        System.out.println(typesString + ";" + regex);
        
    }
    public static void main(String[] args){
        List l = List.of("1", "2", "3");
        //testRequest(l, "salut");
        testComputation();
    }

    protected static ArrayList<Long> sendingTimes = new ArrayList<Long>();
    protected static ArrayList<Long> arrivingTimes = new ArrayList<Long>();

    public static void testComputation() {
        sendingTimes.add(Long.valueOf(10));
        sendingTimes.add(Long.valueOf(10));
        sendingTimes.add(Long.valueOf(10));
        sendingTimes.add(Long.valueOf(10));
        sendingTimes.add(Long.valueOf(10));
        sendingTimes.add(Long.valueOf(10));
 
        arrivingTimes.add(Long.valueOf(0));
        arrivingTimes.add(Long.valueOf(1));
        arrivingTimes.add(Long.valueOf(2));
        arrivingTimes.add(Long.valueOf(0));
        arrivingTimes.add(Long.valueOf(1));
        arrivingTimes.add(Long.valueOf(2));
        arrivingTimes.add(Long.valueOf(0));
        arrivingTimes.add(Long.valueOf(9));
        arrivingTimes.add(Long.valueOf(8));
        arrivingTimes.add(Long.valueOf(0));
        arrivingTimes.add(Long.valueOf(1));
        arrivingTimes.add(Long.valueOf(0));
        arrivingTimes.add(Long.valueOf(1));
        arrivingTimes.add(Long.valueOf(0));
        arrivingTimes.add(Long.valueOf(8));
        arrivingTimes.add(Long.valueOf(0));

        ArrayList<Long> responsesTime = new ArrayList<Long>();
        Long start, stop;
        int indexArrival = 0;
        for (int i=0; i < sendingTimes.size(); i++) {
            start = sendingTimes.get(i);
            stop = arrivingTimes.get(indexArrival);
            while ( (!stop.equals(Long.valueOf(0))) && indexArrival < arrivingTimes.size() ) {
                responsesTime.add(stop - start);
                indexArrival++;
                stop = arrivingTimes.get(indexArrival);
            }
            responsesTime.add(Long.valueOf(0));
            if (indexArrival < arrivingTimes.size()) {
                indexArrival = indexArrival+1;
            }
        }

        // Write the response times to a file.
        System.out.println(responsesTime);
    }

}