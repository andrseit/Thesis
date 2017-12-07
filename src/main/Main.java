package main;

import various.ArrayTransformations;
import various.TestRunner;


/**
 * Created by Darling on 28/8/2017.
 */
public class Main {
    public static void main(String[] args) {

        TestRunner test = new TestRunner();
        //test.testRun();
        //test.offlineWithAgents();
        //test.online();
        //test.staticOnline();
        test.staticOffline();


        /*
        int[] chargers = new int[10];
        chargers[0] = 1;
        chargers[1] = 1;
        chargers[2] = 1;
        chargers[3] = 1;
        chargers[4] = 1;
        ArrayTransformations t = new ArrayTransformations();
        for (int s = 0; s < 10; s++) {
            chargers[s]--;
            // 2.1) if not available chargers, then reset the array
            if (chargers[s] == -1) {
                for (int r_s = s; r_s >= 0; r_s--) {
                    chargers[r_s]++;
                }
                break;
            }
            t.printOneDimensionArray("Chargers", chargers);
        }


        t.printOneDimensionArray("Chargers", chargers);
        */
    }



}
