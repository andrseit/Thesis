package main;


/**
 * Created by Darling on 28/8/2017.
 */
public class Main {

    public static void main(String[] args) {

        /*
        Execution exe = new OnlineExecution();
        System.out.println("Online");
        //exe.execute();
        exe = new OfflineExecution();
        System.out.println("\n\nOffline");
        exe.execute();
        */

        Execution2 exe = new OfflineExecution2();
        //exe.execute();

        exe = new OnlineExecution2();
        exe.execute();

    }

}
