package main;


import io.DataGenerator;

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

    }

}
