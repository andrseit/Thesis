package main;


import io.DataGenerator;

import java.util.HashSet;

/**
 * Created by Darling on 28/8/2017.
 */
public class Main {

    public static void main(String[] args) {

        //DataGenerator dt = new DataGenerator();
        //dt.generateEVsFile(10, 10, 10, 10);

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
