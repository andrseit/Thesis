package main;

import com.sun.org.apache.bcel.internal.generic.JsrInstruction;
import generator.evs.EVGenerator;
import generator.evs.SimpleGenerator;
import generator.evs.StrategyGenerator;
import generator.stations.SimpleStationGenerator;
import generator.stations.StationGenerator;
import io.JSONWriter;
import main.experiments.GenerateOnceExperiment;
import org.json.simple.JSONObject;
import statistics.SimpleMath;
import sun.nio.ch.SelectorImpl;
import various.MicroExperiments;

import java.util.Random;


/**
 * Created by Darling on 28/8/2017.
 */
public class Main {
    public static void main(String[] args) {
        SimpleGenerator simpleGenerator = new SimpleGenerator(10, 5, 2, 5, 1.5);
        StrategyGenerator evGenerator = new StrategyGenerator(simpleGenerator, 1.5);
        SimpleStationGenerator stationGenerator = new SimpleStationGenerator(5);
        DataGenerator generator = new DataGenerator(evGenerator, stationGenerator);
        JSONWriter.writeToFile("files/new_evs.json", generator.generateEVs(10).toJSONString());
        JSONWriter.writeToFile("files/new_stations.json", generator.generateStations(2).toJSONString());

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
