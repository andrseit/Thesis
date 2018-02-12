package main;


import io.DataGenerator;
import statistics.Statistics;

/**
 * Created by Darling on 28/8/2017.
 */
public class Main {

    public static void main(String[] args) {

        DataGenerator dt = new DataGenerator(3, 2, 10, 3);
        //dt.generateStationFile();
        //dt.readStationFile();
        //dt.generateEVsFile(2, 5, 0.6, 1.7);
        //dt.generatePriceFile();

        /*
        Execution exe = new OnlineExecution();
        System.out.println("Online");
        //exe.execute();
        exe = new OfflineExecution();
        System.out.println("\n\nOffline");
        exe.execute();
        */


        Execution exe = new OfflineExecution();
        exe.execute();

        //exe = new OnlineExecution();

        //exe.execute();

        Statistics stats = new Statistics(exe.getStationData(), exe.getEVsNumber());
        stats.computeStats();
        stats.printOverallStats();
        stats.printStationStats();
        stats.printTimeStats();

    }

}
