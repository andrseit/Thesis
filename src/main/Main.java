package main;


import io.DataGenerator;
import statistics.Statistics;

import java.util.Random;

/**
 * Created by Darling on 28/8/2017.
 */
public class Main {

    public static void main(String[] args) {

        DataGenerator dt = new DataGenerator(3, 5, 10, 3);
        //dt.generateStationFile();
        //dt.readStationFile();
        //dt.generateEVsFile();

        /*
        Execution exe = new OnlineExecution();
        System.out.println("Online");
        //exe.execute();
        exe = new OfflineExecution();
        System.out.println("\n\nOffline");
        exe.execute();
        */

        Execution exe = new OfflineExecution();
        //exe.execute();

        exe = new OnlineExecution();
        exe.execute();

        Statistics stats = new Statistics(exe.getStationData(), exe.getEVsNumber());
        stats.computeStats();
        stats.printOverallStats();
        stats.printStationStats();
    }

}
