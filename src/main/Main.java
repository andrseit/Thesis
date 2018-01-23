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

        Execution exe = new OfflineExecution();
        exe.execute();

        exe = new OnlineExecution();
        //exe.execute();

    }

}
