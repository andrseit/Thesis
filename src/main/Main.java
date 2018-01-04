package main;

import various.DataGenerator;
import various.TestRunner;


/**
 * Created by Darling on 28/8/2017.
 */
public class Main {


    public static void main(String[] args) {


        /*
        JSONFileParser parser = new JSONFileParser();
        parser.readEVsData();
        */

        TestRunner test = new TestRunner();
        //test.testRun();
        //test.offlineWithAgents();
        //test.online();
        //test.staticOnline();
        test.staticOffline();


        /**
         * data files generation
        DataGenerator dt = new DataGenerator();
        dt.generateEVsFile(10, 10,5);
        dt.generateStationFile(10, 10);
        */

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
