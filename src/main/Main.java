package main;


import generator.evs.SimpleGenerator;
import generator.evs.StrategyGenerator;
import generator.stations.SimpleStationGenerator;
import io.JSONFileParser;
import io.JSONWriter;
import main.experiments.GenerateOnceExperiment;

/**
 * Created by Darling on 28/8/2017.
 */
public class Main {
    public static void main(String[] args) {
//        SimpleGenerator simpleGenerator = new SimpleGenerator(10, 5, 2, 5, 1.5);
//        StrategyGenerator evGenerator = new StrategyGenerator(simpleGenerator, 1.5);
//        SimpleStationGenerator stationGenerator = new SimpleStationGenerator(5);
//        DataGenerator generator = new DataGenerator(evGenerator, stationGenerator);
//        generator.generateStations(2, "files/new_stations.json");
//        generator.generateEVs(10, "files/new_evs.json");
//
//        JSONFileParser parser = new JSONFileParser();
//        parser.parseEVs("files/new_evs.json");
//        parser.parseStations("files/new_stations.json");
        /*
        for (int i = 0; i < 10; i++) {
            DataGenerator generator = new DataGenerator();
            generator.readStationFile("station.json");
            //generator.generateRandomStations(2, 35);
            generator.generateEVsFile(500, 25, 75, 1.8, 1.8, 288, 2);

            //runExperiment(2);
            runExperiment(1, i); // no alternatives
            //runExperiment(2, i); // alternatives
            //runExperiment(3, i); // offline
            //runExperiment(4); // virtual demand
        }
        */


        //MicroExperiments.testOptimizer();
        //MicroExperiments.clearArray();
        //int inform = MicroExperiments.randomInform(15, 0, 20);

        //ExecutionFlow exe = new ExecutionFlow("files/new_stations.json", "files/new_evs.json", "system.json");
        //exe.runOffline();

        GenerateOnceExperiment experiment = new GenerateOnceExperiment("0", 1, "files/new_stations.json", "files/new_evs.json", "system.json", "files/experiments");
        experiment.setEVsValues(5, 1, 5, 1.4, 1.5);
        experiment.run("offline");

    }

    private static void runExperiment (int id, int iteration) {
        if (id == 1) {
            GenerateOnceExperiment experiment = new GenerateOnceExperiment("online_single_station_no_alternatives_" + iteration, 1, "two_station_alt_1.json", "evs.json",
                    "system.json", "files/experiments/statistics/no_alternatives");
            experiment.useDelays(true);
            //experiment.setStationsValues(1, 40);
            //experiment.setEVsValues(250, 25, 50, 1.8, 1.8);
            experiment.setSystemParameters(288, 2);
            experiment.run("online");
        } else if (id == 2) {
            GenerateOnceExperiment experiment = new GenerateOnceExperiment("online_single_station_alternatives_" + iteration, 1, "single_station_alt.json", "evs.json",
                    "system.json", "files/experiments/statistics/alternatives");
            experiment.useDelays(true);
            //experiment.setStationsValues(1, 40);
            //experiment.setEVsValues(250, 15, 50, 1.8, 1.8);
            experiment.setSystemParameters(288, 2);
            experiment.run("online");
        } else if (id == 3) {
            GenerateOnceExperiment experiment = new GenerateOnceExperiment("offline_single_station_no_alternatives_" + iteration, 1, "station.json", "evs.json",
                    "system.json", "files/experiments/statistics/offline");
            //experiment.useDelays(true);
            //experiment.setStationsValues(1, 40);
            //experiment.setEVsValues(250, 15, 50, 1.8, 1.8);
            experiment.setSystemParameters(288, 2);
            experiment.run("offline");
        } else if (id == 4) {
            GenerateOnceExperiment experiment = new GenerateOnceExperiment("online_single_station_alternatives_virtual_demand_" + iteration, 1, "station_virt_demand.json", "evs.json",
                    "system.json", "files/experiments/statistics");
            //experiment.useDelays(true);
            //experiment.setStationsValues(1, 40);
            //experiment.setEVsValues(250, 15, 50, 1.8, 1.8);
            experiment.setSystemParameters(288, 2);
            experiment.run("online");
        }
    }
}
