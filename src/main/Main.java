package main;

import main.experiments.GenerateOnceExperiment;
import main.experiments.GeneratePerIterationExperiment;

/**
 * Created by Darling on 28/8/2017.
 */
public class Main {
    public static void main(String[] args) {
        GenerateOnceExperiment experiment = new GenerateOnceExperiment("offline_1", 2, "station.json", "evs.json",
                "system.json", "files/experiments/statistics");
        experiment.setStationsValues(2, 1);
        experiment.setEVsValues(5, 2, 5, 1.5, 1.5);
        experiment.setSystemParameters(10, 2);
        experiment.run("offline");
    }
}
