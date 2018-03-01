package main;


import io.DataGenerator;
import statistics.Statistics;

/**
 * Created by Darling on 28/8/2017.
 */
public class Main {

    public static void main(String[] args) {



        int iterations = 1;
        for (int i = 0; i < iterations; i++) {
            DataGenerator dt = new DataGenerator(4, 200, 288, 4);
            //dt.generateStationFile();
            dt.readStationFile();
            dt.generateEVsFile(25, 75, 0.4, 1.4);
            //dt.generatePriceFile();
            Execution exe;
            Statistics stats;



            // Offline with no suggestions
            exe = new OfflineExecution(false);
            exe.execute();
            stats = new Statistics(exe.getStationData(), exe.getEVsNumber());
            stats.computeStats();
            //stats.printOverallStats();
            //stats.printStationStats();
            //stats.printTimeStats();
            //System.out.println(stats.fileStationsString());
            System.out.println(stats.timesString());
            System.out.println("----------------------------------------------");


            // Offline with suggestions
            exe = new OfflineExecution(true);
            exe.execute();
            stats = new Statistics(exe.getStationData(), exe.getEVsNumber());
            stats.computeStats();
            //stats.printOverallStats();
            //stats.printStationStats();
            //stats.printTimeStats();
            //System.out.println(stats.fileStationsString());
            System.out.println(stats.timesString());
            System.out.println("----------------------------------------------");


            // Online without suggestions
            exe = new OnlineExecution(false);
            exe.execute();
            stats = new Statistics(exe.getStationData(), exe.getEVsNumber());
            stats.computeStats();
            //stats.printOverallStats();
            //stats.printStationStats();
            //stats.printTimeStats();
            //System.out.println(stats.fileStationsString());
            System.out.println(stats.timesString());
            System.out.println("----------------------------------------------");


            // Online with suggestions
            exe = new OnlineExecution(true);
            exe.execute();
            stats = new Statistics(exe.getStationData(), exe.getEVsNumber());
            stats.computeStats();
            //stats.printOverallStats();
            //stats.printStationStats();
            //stats.printTimeStats();
            //System.out.println(stats.fileStationsString());
            System.out.println(stats.timesString());

            System.out.println("------------------------------");


        }

    }

}
