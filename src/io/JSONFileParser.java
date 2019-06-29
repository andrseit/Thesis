package io;

import agents.evs.EV;
import agents.evs.EVParameters;
import agents.evs.strategy.StrategyPreferences;
import agents.station.Station;
import agents.station.optimize.OptimizerFactory;
import main.experiments.parameters.SystemParameters;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import various.JSONUtils;

import java.io.*;
import java.util.*;
import static java.lang.Math.toIntExact;

public class JSONFileParser {

    private int slotsNumber;

    public SystemParameters readSystemParameters (String path) {
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader("files/" + path));
            JSONObject jsonObject = (JSONObject) obj;
            System.out.println(jsonObject);
            slotsNumber = toIntExact((long) jsonObject.get("slots"));
            int gridSize = toIntExact((long) jsonObject.get("gridSize"));
            return new SystemParameters(slotsNumber, gridSize);
        } catch (org.json.simple.parser.ParseException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Station> parseStations (String path) {
        JSONParser parser = new JSONParser();
        JSONUtils util = new JSONUtils();
        ArrayList<Station> list = new ArrayList<>();
        try {
            JSONObject stations = (JSONObject) parser.parse(new FileReader(path));
            JSONArray array = (JSONArray) stations.get("stations");
            for (Object stationObject: array) {
                JSONObject stationJSON = (JSONObject) stationObject;
                JSONObject strategy = (JSONObject) stationJSON.get("strategy");


                StationPricing pr = setPrice(util.getStringValue(stationJSON, "price"));
                // setting the same optimizer to all stations - change that later
                list.add(new Station(util.getIntValue(stationJSON, "id"), 0, 0, util.getIntValue(stationJSON, "chargers"),
                        OptimizerFactory.getOptimizer(util.getStringValue(strategy, "optimizer")),
                        OptimizerFactory.getOptimizer("alternatives"), util.getIntValue(strategy, "alternatives") == 1,
                        pr.getPrice(), slotsNumber));
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<EV> parseEVs (String path) {
        ArrayList<EV> list = new ArrayList<>();
        try {
            JSONParser parser = new JSONParser();
            JSONUtils util = new JSONUtils();
            JSONObject evs = (JSONObject) parser.parse(new FileReader(path));
            JSONArray array = (JSONArray) evs.get("evs");
            for (Object evObject: array) {
                JSONObject evJSON = (JSONObject) evObject;
                JSONObject preferences = (JSONObject) evJSON.get("preferences");
                JSONObject strategy = (JSONObject) evJSON.get("strategy");

                EVParameters parameters = new EVParameters(util.getIntValue(evJSON, "id"), 0, 0, 1, 1,
                        util.getIntValue(preferences, "arrival"), util.getIntValue(preferences, "departure"), util.getIntValue(preferences, "energy"), slotsNumber);

                StrategyPreferences strategyPreferences = new StrategyPreferences(util.getIntValue(preferences, "inform"),
                        util.getIntValue(strategy, "energy"), util.getIntValue(strategy, "arrival"), util.getIntValue(strategy, "departure"),
                        1.8, 2, "price", true);

                EV ev = new EV(parameters, strategyPreferences);
                list.add(ev);
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private StationPricing setPrice (String path) {
        int[] price = new int[slotsNumber];
        int[] renewables = new int[slotsNumber];
        try {
            BufferedReader in = new BufferedReader(new FileReader("files/price/" + path + ".txt"));
            String line;
            for (int s = 0; s < slotsNumber; s++) {
                line = in.readLine();
                String[] tokens = line.split(",");
                price[s] = Integer.parseInt(tokens[0]);
                renewables[s] = Integer.parseInt(tokens[1]);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new StationPricing(price, renewables);
    }

    public int getSlotsNumber() {
        return slotsNumber;
    }

    private class StationPricing {
        private int[] price;
        private int[] renewables;

        public StationPricing(int[] price, int[] renewables) {
            this.price = price;
            this.renewables = renewables;
        }

        public int[] getPrice() {
            return price;
        }

        private int[] getRenewables () { return renewables; }
    }
}
