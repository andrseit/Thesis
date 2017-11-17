package main;

import evs.EV;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import optimize.CPLEX;
import various.EVData;
import various.JSONFileParser;
import various.TestRunner;

import java.util.ArrayList;

/**
 * Created by Darling on 28/8/2017.
 */
public class Main {
    public static void main(String[] args) {

        TestRunner test = new TestRunner();
        test.testRun();
        test.offline();
        /*
        int[][] array = new int[0][0];
        System.out.println(array.length);

        StaticRun sr = new StaticRun();
        //sr.online();
        //sr.offline();
        online();
        */

    }



}
