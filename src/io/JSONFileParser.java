package io;

import agents.evs.EV;
import agents.evs.EVParameters;
import agents.evs.strategy.StrategyPreferences;
import agents.station.Station;
import agents.station.optimize.OptimizerFactory;
import main.experiments.parameters.SystemParameters;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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

    public ArrayList<Station> readStations (String path) {
        ArrayList<Station> stations = new ArrayList<>();
        int chargersNumber, x, y;

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader("files/" + path));
            JSONObject jsonObject = (JSONObject) obj;
            System.out.println(jsonObject);
            JSONObject stationsRootJSON = (JSONObject)jsonObject.get("stations");

            ArrayList<JSONObject> stationsJSON = new ArrayList<>();
            for (Object key: stationsRootJSON.keySet()) {
                JSONObject stationJSON = (JSONObject) stationsRootJSON.get(key);
                stationsJSON.add(stationJSON);
            }
            stationsJSON.sort((o1, o2) -> {
                Long id1 = (Long) o1.get("id");
                Long id2 = (Long) o2.get("id");
                return (int) (id1 - id2);
            });

            for (JSONObject stationJSON: stationsJSON) {
                int id = toIntExact((long) stationJSON.get("id"));
                chargersNumber = toIntExact((long) stationJSON.get("chargers"));

                JSONObject location = (JSONObject) stationJSON.get("location");
                x = toIntExact((long) location.get("x"));
                y = toIntExact((long) location.get("y"));
                String pricePath = stationJSON.get("price_file").toString();

                JSONObject flagsObject = (JSONObject) stationJSON.get("flags");
                HashMap<String, Integer> flags = new HashMap<>();
                flags.put("window", toIntExact((long) flagsObject.get("window")));
                flags.put("suggestion", toIntExact((long) flagsObject.get("suggestion")));
                flags.put("cplex", toIntExact((long) flagsObject.get("cplex")));
                flags.put("alternatives", toIntExact((long) flagsObject.get("alternatives")));
                flags.put("virtual_demand", toIntExact((long) flagsObject.get("virtual_demand")));
                boolean alternatives = flags.get("alternatives") == 1;
                //System.out.println(pricePath);
                StationPricing pr = setPrice(pricePath);
                // setting the same optimizer to all stations - change that later
                stations.add(new Station(id, x, y, chargersNumber, OptimizerFactory.getOptimizer("service"),
                        OptimizerFactory.getOptimizer("alternatives"), alternatives, flags.get("virtual_demand") == 1, pr.getPrice(), slotsNumber));
            }
        } catch (org.json.simple.parser.ParseException | IOException e) {
            e.printStackTrace();
        }

        return stations;
    }

    public ArrayList<EV> readEVsData(String path) {

        ArrayList<EV> evs = new ArrayList<>();
        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(new FileReader("files/" + path));
            JSONObject jsonObject = (JSONObject) obj;
            JSONObject root = (JSONObject)jsonObject.get("evs");
            ArrayList<JSONObject> evsJSON = new ArrayList<>();
            for (Object key: root.keySet()) {
                JSONObject ev = (JSONObject) root.get(key);
                evsJSON.add(ev);
            }
            evsJSON.sort((o1, o2) -> {
                Long id1 = (Long) o1.get("id");
                Long id2 = (Long) o2.get("id");
                return (int) (id1 - id2);
            });

            for (JSONObject evJSON: evsJSON) {
                int id = toIntExact((long) evJSON.get("id"));
                int x = toIntExact((long) evJSON.get("x"));
                int y = toIntExact((long) evJSON.get("y"));
                int f_x = toIntExact((long) evJSON.get("f_x"));
                int f_y = toIntExact((long) evJSON.get("f_y"));

                JSONObject preferences = (JSONObject) evJSON.get("preferences");

                int energy = toIntExact((long) preferences.get("energy"));
                int bid = toIntExact((long) preferences.get("bid"));
                int inform_slot = toIntExact((long) preferences.get("inform"));
                int start_slot = toIntExact((long) preferences.get("start"));
                int end_slot = toIntExact((long) (preferences.get("end")));
                int max_distance = toIntExact((long) (preferences.get("distance")));

                JSONObject strategy = (JSONObject) evJSON.get("strategy");

                int s_energy = toIntExact((long) strategy.get("energy"));
                int s_start = toIntExact((long) strategy.get("start"));
                int s_end = toIntExact((long) (strategy.get("end")));
                int s_prob = toIntExact((long) (strategy.get("probability")));
                int s_rounds = toIntExact((long) (strategy.get("rounds")));
                double s_range = Double.parseDouble(strategy.get("range").toString());
                String s_priority = strategy.get("priority").toString();
                boolean delay = toIntExact((long) (strategy.get("delay"))) == 1;

                EVParameters evParameters = new EVParameters(id, x, y, f_x, f_y, start_slot, end_slot, energy, bid, max_distance, slotsNumber);
                StrategyPreferences strategyPreferences = new StrategyPreferences(inform_slot, s_energy, s_start, s_end, s_range, s_rounds, s_prob, s_priority, delay);
                EV ev = new EV(evParameters, strategyPreferences);
                evs.add(ev);
            }
        } catch (org.json.simple.parser.ParseException | IOException e) {
            e.printStackTrace();
        }

        return evs;
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
